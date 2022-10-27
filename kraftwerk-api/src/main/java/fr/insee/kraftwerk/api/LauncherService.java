package fr.insee.kraftwerk.api;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import fr.insee.kraftwerk.core.Constants;
import fr.insee.kraftwerk.core.dataprocessing.CalculatedProcessing;
import fr.insee.kraftwerk.core.dataprocessing.CleanUpProcessing;
import fr.insee.kraftwerk.core.dataprocessing.DataProcessing;
import fr.insee.kraftwerk.core.dataprocessing.DataProcessingManager;
import fr.insee.kraftwerk.core.dataprocessing.GroupProcessing;
import fr.insee.kraftwerk.core.dataprocessing.InformationLevelsProcessing;
import fr.insee.kraftwerk.core.dataprocessing.MultimodeTransformations;
import fr.insee.kraftwerk.core.dataprocessing.ReconciliationProcessing;
import fr.insee.kraftwerk.core.dataprocessing.UnimodalDataProcessing;
import fr.insee.kraftwerk.core.exceptions.KraftwerkException;
import fr.insee.kraftwerk.core.exceptions.NullException;
import fr.insee.kraftwerk.core.extradata.paradata.Paradata;
import fr.insee.kraftwerk.core.extradata.paradata.ParadataParser;
import fr.insee.kraftwerk.core.extradata.reportingdata.CSVReportingDataParser;
import fr.insee.kraftwerk.core.extradata.reportingdata.ReportingData;
import fr.insee.kraftwerk.core.extradata.reportingdata.XMLReportingDataParser;
import fr.insee.kraftwerk.core.inputs.ModeInputs;
import fr.insee.kraftwerk.core.inputs.UserInputs;
import fr.insee.kraftwerk.core.metadata.CalculatedVariables;
import fr.insee.kraftwerk.core.metadata.DDIReader;
import fr.insee.kraftwerk.core.metadata.LunaticReader;
import fr.insee.kraftwerk.core.metadata.VariablesMap;
import fr.insee.kraftwerk.core.outputs.OutputFiles;
import fr.insee.kraftwerk.core.parsers.DataFormat;
import fr.insee.kraftwerk.core.parsers.DataParser;
import fr.insee.kraftwerk.core.parsers.DataParserManager;
import fr.insee.kraftwerk.core.rawdata.SurveyRawData;
import fr.insee.kraftwerk.core.utils.FileUtils;
import fr.insee.kraftwerk.core.vtl.VtlBindings;
import fr.insee.kraftwerk.core.vtl.VtlExecute;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
public class LauncherService {

	VtlExecute vtlExecute = new VtlExecute();
	
	@Value("${fr.insee.postcollecte.files}")
	private String defaultDirectory;
	
	@PutMapping(value = "/main")
	@Operation(operationId = "main", summary = "Main service : call all steps")
	public Boolean main(@Parameter(description = "directory with files", required = true) @RequestBody String inDirectoryParam) throws KraftwerkException {
		/* Step 1 : Init */
		Path inDirectory = getInDirectory(inDirectoryParam);
		String campaignName = inDirectory.getFileName().toString();
		log.info("===============================================================================================");
		log.info("Kraftwerk main service started for campaign: " + campaignName);
		log.info("===============================================================================================");

		UserInputs userInputs = getUserInputs(inDirectory);
		VtlBindings vtlBindings = new VtlBindings();

		/* Step 2 : unimodal data */
		for (String dataMode : userInputs.getModeInputsMap().keySet()) {
			buildVtlBindings(userInputs, dataMode, vtlBindings);
			unimodalProcessing(userInputs, dataMode, vtlBindings);
		}

		/* Step 3 : multimodal VTL data processing */
		multimodalProcessing(userInputs, vtlBindings);

		/* Step 4 : Write output files */
		writeOutputFiles(inDirectory, vtlBindings);

		/* Step 4.3- 4.4 : Archive */
		// archive(outputFiles, userInputs);

		log.info("===============================================================================================");
		log.info("Kraftwerk main service terminated for campaign: " + campaignName);
		log.info("===============================================================================================\n");

		return true;
	}

	@PutMapping(value = "/buildVtlBindings")
	@Operation(operationId = "buildVtlBindings", summary = "Transform data from collect, to data ready to use in Trevas")
	public void buildVtlBindings(
			@Parameter(description = "directory with input files", required = true) @RequestBody String inDirectoryParam
			) throws KraftwerkException {
		//Read data files
		Path inDirectory = getInDirectory(inDirectoryParam);
		UserInputs userInputs = getUserInputs(inDirectory);
		
		//Process
		for (String dataMode : userInputs.getModeInputsMap().keySet()) {
			VtlBindings vtlBindings = new VtlBindings();
			buildVtlBindings(userInputs, dataMode, vtlBindings);
			
			//Write data in JSON file
			Path tempOutputPath = FileUtils.transformToTemp(inDirectory).resolve("unimodalProcessing" +"_"+ dataMode+".json");
			vtlExecute.writeJsonDataset(dataMode, tempOutputPath, vtlBindings);
		}

	}
	
	
	@PutMapping(value = "/buildVtlBindings/{dataMode}")
	@Operation(operationId = "buildVtlBindings", summary = "Transform data from collect, to data ready to use in Trevas")
	public void buildVtlBindingsByDataMode(
			@Parameter(description = "directory with input files", required = true) @RequestBody String inDirectoryParam,
			@Parameter(description = "Data mode", required = true) @PathVariable String dataMode
			) throws KraftwerkException {
		//Read data files
		Path inDirectory = getInDirectory(inDirectoryParam);
		UserInputs userInputs = getUserInputs(inDirectory);
		VtlBindings vtlBindings = new VtlBindings();
		
		//Process
		buildVtlBindings(userInputs, dataMode, vtlBindings);
		
		//Write data in JSON file
		Path tempOutputPath = FileUtils.transformToTemp(inDirectory).resolve("unimodalProcessing" +"_"+ dataMode );
		vtlExecute.writeJsonDataset(dataMode, tempOutputPath, vtlBindings);
	}
	

	private void buildVtlBindings(UserInputs userInputs, String dataMode, VtlBindings vtlBindings) throws NullException {
		ModeInputs modeInputs = userInputs.getModeInputs(dataMode);
		SurveyRawData data = new SurveyRawData();

		/* Step 2.0 : Read the DDI file to get survey variables */
		data.setVariablesMap(getMetadata(userInputs, dataMode));

		/* Step 2.1 : Fill the data object with the survey answers file */
		data.setDataFilePath(modeInputs.getDataFile());
		DataParser parser = DataParserManager.getParser(modeInputs.getDataFormat(), data);
		parser.parseSurveyData(modeInputs.getDataFile());

		/* Step 2.2 : Get paradata for the survey */
		parseParadata(modeInputs, data);

		/* Step 2.3 : Get reportingData for the survey */
		parseReportingData(modeInputs, data);

		/* Step 2.4a : Convert data object to a VTL Dataset */
		data.setDataMode(dataMode); // TODO: remove useless dataMode attribute from data object
		vtlExecute.convertToVtlDataset(data, dataMode, vtlBindings);
	}


	@PutMapping(value = "/unimodalProcessing")
	@Operation(operationId = "unimodalProcessing", summary = "Apply transformation on one mode")
	public void unimodalProcessing(
			@Parameter(description = "directory with input files", required = true) @RequestBody  String inDirectoryParam,
			@Parameter(description = "Data mode", required = true) @RequestParam  String dataMode
			) throws KraftwerkException {
		//Read data in JSON file
		Path inDirectory = getInDirectory(inDirectoryParam);
		UserInputs userInputs = getUserInputs(inDirectory);
		VtlBindings vtlBindings = readDataset(FileUtils.transformToTemp(inDirectory).toString(),dataMode);
		
		//Process
		unimodalProcessing(userInputs, dataMode, vtlBindings);
		
		//Write data in JSON file
		Path tempOutputPath = FileUtils.transformToTemp(inDirectory).resolve("unimodalProcessing" +"_"+ dataMode );
		vtlExecute.writeJsonDataset(dataMode, tempOutputPath, vtlBindings);
	}
	
	private void unimodalProcessing(UserInputs userInputs, String dataMode, VtlBindings vtlBindings) {
		ModeInputs modeInputs = userInputs.getModeInputs(dataMode);
		VariablesMap metadata = getMetadata(userInputs, dataMode);

		/* Step 2.4b : Apply VTL expression for calculated variables (if any) */
		if (modeInputs.getLunaticFile() != null) {
			CalculatedVariables calculatedVariables = LunaticReader
					.getCalculatedFromLunatic(modeInputs.getLunaticFile());
			DataProcessing calculatedProcessing = new CalculatedProcessing(vtlBindings);
			calculatedProcessing.applyVtlTransformations(dataMode, null, calculatedVariables,
					metadata);
		} else {
			log.info(String.format("No Lunatic questionnaire file for mode \"%s\"", dataMode));
			if (modeInputs.getDataFormat() == DataFormat.LUNATIC_XML
					|| modeInputs.getDataFormat() == DataFormat.LUNATIC_JSON) {
				log.warn(String.format(
						"Calculated variables for lunatic data of mode \"%s\" will not be evaluated.",
						dataMode));
			}
		}

		/* Step 2.4c : Prefix variable names with their belonging group names */
		new GroupProcessing(vtlBindings).applyVtlTransformations(dataMode, null, metadata);

		/* Step 2.5 : Apply mode-specific VTL transformations */
		UnimodalDataProcessing dataProcessing = DataProcessingManager
				.getProcessingClass(modeInputs.getDataFormat(), vtlBindings);
		dataProcessing.applyVtlTransformations(dataMode, modeInputs.getModeVtlFile(),
				metadata);
	}
	
	private VariablesMap getMetadata(UserInputs userInputs, String dataMode){
		Map<String, VariablesMap> metadataVariables = getMetadata(userInputs);
		return metadataVariables.get(dataMode);
	}

	
	private Map<String, VariablesMap> getMetadata(UserInputs userInputs){
		Map<String, VariablesMap> metadataVariables = new LinkedHashMap<>();
		userInputs.getModeInputsMap().forEach((k, v) -> putToMetadataVariable(k,v,metadataVariables));
		return metadataVariables;
	}
	
	private void putToMetadataVariable(String dataMode, ModeInputs modeInputs, Map<String, VariablesMap> metadataVariables ) {
		metadataVariables.put(dataMode, DDIReader.getVariablesFromDDI(modeInputs.getDdiUrl()));
	}

	private void parseParadata(ModeInputs modeInputs, SurveyRawData data) throws NullException {
		Path paraDataFolder = modeInputs.getParadataFolder();
		if (paraDataFolder != null) {
			ParadataParser paraDataParser = new ParadataParser();
			Paradata paraData = new Paradata(paraDataFolder);
			paraDataParser.parseParadata(paraData, data);
		}
	}

	private void parseReportingData(ModeInputs modeInputs, SurveyRawData data) throws NullException {
		Path reportingDataFile = modeInputs.getReportingDataFile();
		if (reportingDataFile != null) {
			ReportingData reportingData = new ReportingData(reportingDataFile);
			if (reportingDataFile.toString().contains(".xml")) {
				XMLReportingDataParser xMLReportingDataParser = new XMLReportingDataParser();
				xMLReportingDataParser.parseReportingData(reportingData, data);

			} else if (reportingDataFile.toString().contains(".csv")) {
					CSVReportingDataParser cSVReportingDataParser = new CSVReportingDataParser();
					cSVReportingDataParser.parseReportingData(reportingData, data);
			}
		}
	}

	@PutMapping(value = "/multimodalProcessing")
	@Operation(operationId = "multimodalProcessing", summary = "Write output files in outDirectory")
	public void multimodalProcessing(
			@Parameter(description = "directory with input files", required = true) @RequestBody String inDirectoryParam
			) throws KraftwerkException {
		//Read data in JSON file
		Path inDirectory = getInDirectory(inDirectoryParam);
		UserInputs userInputs = getUserInputs(inDirectory);
		String multimodeDatasetName = userInputs.getMultimodeDatasetName();
		VtlBindings vtlBindings = readDataset(FileUtils.transformToTemp(inDirectory).toString(),multimodeDatasetName);
		
		//Process
		multimodalProcessing(userInputs, vtlBindings);
		
		//Write data in JSON file
		Path tempOutputPath = FileUtils.transformToTemp(inDirectory).resolve("multimodalProcessing" +"_"+ multimodeDatasetName );
		vtlExecute.writeJsonDataset(multimodeDatasetName, tempOutputPath, vtlBindings);
	}
	
	private void multimodalProcessing(UserInputs userInputs, VtlBindings vtlBindings) {
		String multimodeDatasetName = userInputs.getMultimodeDatasetName();

		/* Step 3.1 : aggregate unimodal datasets into a multimodal unique dataset */
		DataProcessing reconciliationProcessing = new ReconciliationProcessing(vtlBindings);
		reconciliationProcessing.applyVtlTransformations(multimodeDatasetName,
				userInputs.getVtlReconciliationFile());

		/* Step 3.1.b : clean up processing */
		CleanUpProcessing cleanUpProcessing = new CleanUpProcessing(vtlBindings);
		cleanUpProcessing.applyVtlTransformations(multimodeDatasetName, null, getMetadata(userInputs));

		/* Step 3.2 : treatments on the multimodal dataset */
		DataProcessing multimodeTransformations = new MultimodeTransformations(vtlBindings);
		multimodeTransformations.applyVtlTransformations(multimodeDatasetName,
				userInputs.getVtlTransformationsFile());

		/* Step 3.3 : Create datasets on each information level (i.e. each group) */
		DataProcessing informationLevelsProcessing = new InformationLevelsProcessing(vtlBindings);
		informationLevelsProcessing.applyVtlTransformations(multimodeDatasetName,
				userInputs.getVtlInformationLevelsFile());
	}

	@PutMapping(value = "/writeOutputFiles")
	@Operation(operationId = "writeOutputFiles", summary = "Write output files in outDirectory")
	public void writeOutputFiles(
			@Parameter(description = "directory with input files", required = true) @RequestBody  String inDirectoryParam, 
			@Parameter(description = "Bindings file name in temp directory", required = true) @RequestParam  String bindingFilename,
			@Parameter(description = "Data mode") @RequestBody String datamode
			) throws KraftwerkException {
		Path inDirectory = getInDirectory(inDirectoryParam);
		VtlBindings vtlBindings = readDataset(FileUtils.transformToTemp(inDirectory).toString(),datamode);
		writeOutputFiles(inDirectory, vtlBindings);
	}

	private void writeOutputFiles(Path inDirectory, VtlBindings vtlBindings) {
		Path outDirectory = FileUtils.transformToOut(inDirectory);
		UserInputs userInputs = getUserInputs(inDirectory);
		/* Step 4.1 : write csv output tables */
		OutputFiles outputFiles = new OutputFiles(outDirectory, vtlBindings, userInputs);
		outputFiles.writeOutputCsvTables();

		/* Step 4.2 : write scripts to import csv tables in several languages */
		outputFiles.writeImportScripts(getMetadata(userInputs));
	}
	
	@PutMapping(value = "/archive")
	@Operation(operationId = "archive", summary = "Archive files")
	public void archive(
			@Parameter(description = "directory with files", required = true) @RequestBody  String inDirectoryParam) 
			throws KraftwerkException {
		Path inDirectory = getInDirectory(inDirectoryParam);
		
		/* Step 4.3 : move kraftwerk.json to a secondary folder */
		FileUtils.renameInputFile(inDirectory);

		/* Step 4.4 : move differential data to a secondary folder */
		FileUtils.moveInputFiles(getUserInputs(inDirectory));
		
		//TODO delete temp directory
	}
	
	private UserInputs getUserInputs(Path inDirectory) {
		return new UserInputs(inDirectory.resolve(Constants.USER_INPUT_FILE), inDirectory);
	}
	
	private Path getInDirectory(String inDirectoryParam) throws KraftwerkException {
		Path inDirectory = Paths.get(inDirectoryParam);
		if (!verifyInDirectory(inDirectory)) inDirectory = Paths.get(defaultDirectory, "in", inDirectoryParam);
		if (!verifyInDirectory(inDirectory)) throw new KraftwerkException(400, "Configuration file not found");
		return inDirectory;
	}
	
	private boolean verifyInDirectory(Path inDirectory) {
		Path userInputFile = inDirectory.resolve(Constants.USER_INPUT_FILE);
		if (Files.exists(userInputFile)) {
			log.info(String.format("Found configuration file in campaign folder: %s", userInputFile));
		} else {
			log.info("No configuration file found in campaign folder: " + inDirectory);
			return false;
		}
		return true;
	}
	
	private VtlBindings readDataset(String path, String bindingName) {
		VtlBindings vtlBindings = new VtlBindings();
		vtlExecute.putVtlDataset(path, bindingName, vtlBindings);
		return vtlBindings;

	}
	
   // WRITE vtlExecute.writeJsonDataset(dataMode, vtlOutputDir.resolve("1_" + dataMode + JSON), vtlBindings);
	//	READ	vtlExecute.putVtlDataset(tempDatasetPath, "OUTPUT_TEST_EXPORT", vtlBindings);




}