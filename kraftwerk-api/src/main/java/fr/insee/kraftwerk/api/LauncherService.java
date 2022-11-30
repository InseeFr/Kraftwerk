package fr.insee.kraftwerk.api;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
import fr.insee.kraftwerk.core.dataprocessing.StepEnum;
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
import fr.insee.kraftwerk.core.utils.TextFileWriter;
import fr.insee.kraftwerk.core.vtl.ErrorVtlTransformation;
import fr.insee.kraftwerk.core.vtl.VtlBindings;
import fr.insee.kraftwerk.core.vtl.VtlExecute;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.extern.slf4j.Slf4j;



@Slf4j
@RestController
public class LauncherService {

	private static final String INDIRECTORY_EXAMPLE = "LOG-2021-x12-web";
	private static final String JSON = ".json";
	VtlExecute vtlExecute = new VtlExecute();
	List<ErrorVtlTransformation> errors = new ArrayList<>();
	
	@Value("${fr.insee.postcollecte.csv.output.quote}")
	private String csvOutputsQuoteChar;
	
	@Value("${fr.insee.postcollecte.files}")
	private String defaultDirectory;
	
	@PostConstruct
	public void initializeWithProperties() {
		if (StringUtils.isNotEmpty(csvOutputsQuoteChar)) {
			Constants.setCsvOutputQuoteChar(csvOutputsQuoteChar.trim().charAt(0));
		}

	}

	@PutMapping(value = "/main")
	@Operation(operationId = "main", summary = "${summary.main}", description = "${description.main}")
	public ResponseEntity<String> main(
			@Parameter(description = "${param.inDirectory}", required = true, example = INDIRECTORY_EXAMPLE) @RequestBody String inDirectoryParam,
			@Parameter(description = "${param.archiveAtEnd}", required = false) @RequestParam(defaultValue = "false") boolean archiveAtEnd
			) {
		/* Step 1 : Init */
		Path inDirectory;
		try {
			inDirectory = getInDirectory(inDirectoryParam);
		} catch (KraftwerkException e) {
			return ResponseEntity.status(e.getStatus()).body(e.getMessage());
		}
		String campaignName = inDirectory.getFileName().toString();
		log.info("Kraftwerk main service started for campaign: " + campaignName);

		UserInputs userInputs = getUserInputs(inDirectory);
		VtlBindings vtlBindings = new VtlBindings();

		/* Step 2 : unimodal data */
		for (String dataMode : userInputs.getModeInputsMap().keySet()) {
			try {
				buildVtlBindings(userInputs, dataMode, vtlBindings);
			} catch (NullException e) {
				return ResponseEntity.status(e.getStatus()).body(e.getMessage());
			}
			unimodalProcessing(userInputs, dataMode, vtlBindings, errors);
		}

		/* Step 3 : multimodal VTL data processing */
		multimodalProcessing(userInputs, vtlBindings, errors);

		/* Step 4 : Write output files */
		writeOutputFiles(inDirectory, vtlBindings);
		writeErrorsFile(inDirectory);

		/* Step 4.3- 4.4 : Archive */
		if (Boolean.TRUE.equals(archiveAtEnd)) archive(inDirectoryParam);

		return ResponseEntity.ok(campaignName);
	}

	private void writeErrorsFile(Path inDirectory) {
		Path tempOutputPath = FileUtils.transformToOut(inDirectory).resolve("errors.txt");
		// Create folder if doesn't exist
		try {
			Files.createDirectories(tempOutputPath.getParent());
			log.info(String.format("Created folder: %s", tempOutputPath.getParent().toFile().getAbsolutePath()));
		} catch (IOException e) {
			log.error("Permission refused to create output folder: " + tempOutputPath.getParent(), e);
		}

		//Write errors file
		if (!errors.isEmpty()) {
			try (FileWriter myWriter = new FileWriter(tempOutputPath.toFile(),true)){
				for (ErrorVtlTransformation error : errors){
					myWriter.write(error.toString());
				}
				log.info(String.format("Text file: %s successfully written", tempOutputPath));
			} catch (IOException e) {
				log.warn(String.format("Error occurred when trying to write text file: %s", tempOutputPath), e);
			}
		} else {
			log.debug("No error found during VTL transformations");
		}
	}

	@PutMapping(value = "/buildVtlBindings")
	@Operation(operationId = "buildVtlBindings", summary = "${summary.buildVtlBindings}", description = "${description.buildVtlBindings}")
	public ResponseEntity<String> buildVtlBindings(
			@Parameter(description = "${param.inDirectory}", required = true, example = INDIRECTORY_EXAMPLE) @RequestBody String inDirectoryParam
			)  {
		//Read data files
		Path inDirectory;
		try {
			inDirectory = getInDirectory(inDirectoryParam);
		} catch (KraftwerkException e) {
			return ResponseEntity.status(e.getStatus()).body(e.getMessage());
		}
		UserInputs userInputs = getUserInputs(inDirectory);
		
		//Process
		for (String dataMode : userInputs.getModeInputsMap().keySet()) {
			VtlBindings vtlBindings = new VtlBindings();
			try {
				buildVtlBindings(userInputs, dataMode, vtlBindings);
			} catch (NullException e) {
				return ResponseEntity.status(e.getStatus()).body(e.getMessage());
			}
			
			writeTempBindings(inDirectory, dataMode, vtlBindings, StepEnum.BUILD_BINDINGS);
		}
		
		return ResponseEntity.ok(inDirectoryParam);


	}

	
	@PutMapping(value = "/buildVtlBindings/{dataMode}")
	@Operation(operationId = "buildVtlBindings", summary = "${summary.buildVtlBindings}", description = "${description.buildVtlBindings}")
	public ResponseEntity<String> buildVtlBindingsByDataMode(
			@Parameter(description = "${param.inDirectory}", required = true, example = INDIRECTORY_EXAMPLE) @RequestBody String inDirectoryParam,
			@Parameter(description = "${param.dataMode}", required = true) @PathVariable String dataMode
			)  {
		//Read data files
		Path inDirectory;
		try {
			inDirectory = getInDirectory(inDirectoryParam);
		} catch (KraftwerkException e) {
			return ResponseEntity.status(e.getStatus()).body(e.getMessage());
		}
		UserInputs userInputs = getUserInputs(inDirectory);
		VtlBindings vtlBindings = new VtlBindings();
		
		//Process
		try {
			buildVtlBindings(userInputs, dataMode, vtlBindings);
		} catch (NullException e) {
			return ResponseEntity.status(e.getStatus()).body(e.getMessage());
		}
		
		writeTempBindings(inDirectory, dataMode, vtlBindings, StepEnum.BUILD_BINDINGS);
		
		return ResponseEntity.ok(inDirectoryParam+ " - "+dataMode);

	}


	private void writeTempBindings(Path inDirectory, String dataMode, VtlBindings vtlBindings, StepEnum step)  {
		Path tempOutputPath = FileUtils.transformToTemp(inDirectory).resolve(dataMode+"_"+step.getStepLabel()+JSON);
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
		data.setDataMode(dataMode);
		vtlExecute.convertToVtlDataset(data, dataMode, vtlBindings);
	}


	@PutMapping(value = "/unimodalProcessing")
	@Operation(operationId = "unimodalProcessing", summary = "${summary.unimodalProcessing}", description = "${description.unimodalProcessing}")
	public ResponseEntity<String> unimodalProcessing(
			@Parameter(description = "${param.inDirectory}", required = true, example = INDIRECTORY_EXAMPLE) @RequestBody  String inDirectoryParam,
			@Parameter(description = "${param.dataMode}", required = true) @RequestParam  String dataMode
			)  {
		//Read data in JSON file
		Path inDirectory;
		try {
			inDirectory = getInDirectory(inDirectoryParam);
		} catch (KraftwerkException e) {
			return ResponseEntity.status(e.getStatus()).body(e.getMessage());
		}
		UserInputs userInputs = getUserInputs(inDirectory);
		VtlBindings vtlBindings = new VtlBindings();
		readDataset(FileUtils.transformToTemp(inDirectory).toString(),dataMode, StepEnum.BUILD_BINDINGS, vtlBindings);
		//Process
		unimodalProcessing(userInputs, dataMode, vtlBindings, errors);
		
		writeTempBindings(inDirectory, dataMode, vtlBindings, StepEnum.UNIMODAL_PROCESSING);

		writeErrorsFile(inDirectory);
		
		return ResponseEntity.ok(inDirectoryParam+ " - "+dataMode);


	}
	
	private void unimodalProcessing(UserInputs userInputs, String dataMode, VtlBindings vtlBindings, List<ErrorVtlTransformation> errors) {
		ModeInputs modeInputs = userInputs.getModeInputs(dataMode);
		VariablesMap metadata = getMetadata(userInputs, dataMode);
		String vtlGenerate;
		
		/* Step 2.4b : Apply VTL expression for calculated variables (if any) */
		if (modeInputs.getLunaticFile() != null) {
			CalculatedVariables calculatedVariables = LunaticReader
					.getCalculatedFromLunatic(modeInputs.getLunaticFile());
			DataProcessing calculatedProcessing = new CalculatedProcessing(vtlBindings, calculatedVariables,
					metadata);
			vtlGenerate = calculatedProcessing.applyVtlTransformations(dataMode, null, errors);
			TextFileWriter.writeFile(getTempVtlFilePath(userInputs, "CalculatedProcessing",dataMode), vtlGenerate);

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
		vtlGenerate = new GroupProcessing(vtlBindings, metadata).applyVtlTransformations(dataMode, null, errors);
		TextFileWriter.writeFile(getTempVtlFilePath(userInputs, "GroupProcessing",dataMode), vtlGenerate);


		/* Step 2.5 : Apply mode-specific VTL transformations */
		UnimodalDataProcessing dataProcessing = DataProcessingManager
				.getProcessingClass(modeInputs.getDataFormat(), vtlBindings, metadata);
		vtlGenerate = dataProcessing.applyVtlTransformations(dataMode, modeInputs.getModeVtlFile(), errors);
		TextFileWriter.writeFile(getTempVtlFilePath(userInputs, dataProcessing.getStepName(),dataMode), vtlGenerate);
		
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
	@Operation(operationId = "multimodalProcessing", summary = "${summary.multimodalProcessing}", description = "${description.multimodalProcessing}")
	public ResponseEntity<String> multimodalProcessing(
			@Parameter(description = "${param.inDirectory}", required = true, example = INDIRECTORY_EXAMPLE) @RequestBody String inDirectoryParam
			)  {
		//Read data in JSON file
		Path inDirectory;
		try {
			inDirectory = getInDirectory(inDirectoryParam);
		} catch (KraftwerkException e) {
			return ResponseEntity.status(e.getStatus()).body(e.getMessage());
		}
		UserInputs userInputs = getUserInputs(inDirectory);

		//Test
		VtlBindings vtlBindings = new VtlBindings();
		for (String dataMode : userInputs.getModeInputsMap().keySet()) {
			readDataset(FileUtils.transformToTemp(inDirectory).toString(),dataMode, StepEnum.UNIMODAL_PROCESSING, vtlBindings);
		}

		//Process
		multimodalProcessing(userInputs, vtlBindings, errors);


		for (String datasetName : vtlBindings.getDatasetNames()) {
			writeTempBindings(inDirectory, datasetName, vtlBindings, StepEnum.MULTIMODAL_PROCESSING);
		}

		writeErrorsFile(inDirectory);
		
		return ResponseEntity.ok(inDirectoryParam);

	}
	
	private void multimodalProcessing(UserInputs userInputs, VtlBindings vtlBindings, List<ErrorVtlTransformation> errors) {
		String multimodeDatasetName = userInputs.getMultimodeDatasetName();

		/* Step 3.1 : aggregate unimodal datasets into a multimodal unique dataset */
		DataProcessing reconciliationProcessing = new ReconciliationProcessing(vtlBindings);
		String vtlGenerate = reconciliationProcessing.applyVtlTransformations(multimodeDatasetName,
				userInputs.getVtlReconciliationFile(), errors);
		TextFileWriter.writeFile(getTempVtlFilePath(userInputs, "ReconciliationProcessing",multimodeDatasetName), vtlGenerate);

		/* Step 3.1.b : clean up processing */
		CleanUpProcessing cleanUpProcessing = new CleanUpProcessing(vtlBindings, getMetadata(userInputs));
		vtlGenerate = cleanUpProcessing.applyVtlTransformations(multimodeDatasetName, null, errors);
		TextFileWriter.writeFile(getTempVtlFilePath(userInputs, "CleanUpProcessing",multimodeDatasetName), vtlGenerate);

		/* Step 3.2 : treatments on the multimodal dataset */
		DataProcessing multimodeTransformations = new MultimodeTransformations(vtlBindings);
		vtlGenerate = multimodeTransformations.applyVtlTransformations(multimodeDatasetName,
				userInputs.getVtlTransformationsFile(), errors);
		TextFileWriter.writeFile(getTempVtlFilePath(userInputs, "MultimodeTransformations",multimodeDatasetName), vtlGenerate);

		/* Step 3.3 : Create datasets on each information level (i.e. each group) */
		DataProcessing informationLevelsProcessing = new InformationLevelsProcessing(vtlBindings);
		vtlGenerate = informationLevelsProcessing.applyVtlTransformations(multimodeDatasetName,
				userInputs.getVtlInformationLevelsFile(), errors);
		TextFileWriter.writeFile(getTempVtlFilePath(userInputs, "InformationLevelsProcessing",multimodeDatasetName), vtlGenerate);

	}

	private Path getTempVtlFilePath(UserInputs userInputs, String step, String dataset) {
    	// Create folder if doesn't exist
		try {
			Files.createDirectories(FileUtils.transformToTemp(userInputs.getInputDirectory()));
			log.info(String.format("Created folder: %s", FileUtils.transformToTemp(userInputs.getInputDirectory()).toFile().getAbsolutePath()));
		} catch (IOException e) {
			log.error("Permission refused to create output folder: " + FileUtils.transformToTemp(userInputs.getInputDirectory()).getParent(), e);
		}
		return FileUtils.transformToTemp(userInputs.getInputDirectory()).resolve(step+ dataset+".vtl");
	}

	@PutMapping(value = "/writeOutputFiles")
	@Operation(operationId = "writeOutputFiles", summary = "${summary.writeOutputFiles}", description = "${description.writeOutputFiles}")
	public ResponseEntity<String> writeOutputFiles(
			@Parameter(description = "${param.inDirectory}", required = true, example = INDIRECTORY_EXAMPLE) @RequestBody  String inDirectoryParam
			) {
		Path inDirectory;
		try {
			inDirectory = getInDirectory(inDirectoryParam);
		} catch (KraftwerkException e) {
			return ResponseEntity.status(e.getStatus()).body(e.getMessage());
		}
		VtlBindings vtlBindings = new VtlBindings();
		// Read all bindings necessary to produce output
		String path = FileUtils.transformToTemp(inDirectory).toString();
		List<String> fileNames = FileUtils.listFiles(path);
		fileNames = fileNames.stream().filter(name -> name.endsWith(StepEnum.MULTIMODAL_PROCESSING.getStepLabel()+JSON)).collect(Collectors.toList());
		for (String name : fileNames){
			String pathBindings = path + File.separator + name;
			String bindingName =  name.substring(0, name.indexOf("_"+StepEnum.MULTIMODAL_PROCESSING.getStepLabel()));
			vtlExecute.putVtlDataset(pathBindings, bindingName, vtlBindings);
		}

		writeOutputFiles(inDirectory, vtlBindings);
		return ResponseEntity.ok(inDirectoryParam);

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
	@Operation(operationId = "archive", summary = "${summary.archive}", description = "${description.archive}")
	public ResponseEntity<String> archive(
			@Parameter(description = "${param.inDirectory}", required = true, example = INDIRECTORY_EXAMPLE) @RequestBody  String inDirectoryParam) 
			{
		Path inDirectory;
		try {
			inDirectory = getInDirectory(inDirectoryParam);
		} catch (KraftwerkException e) {
			return ResponseEntity.status(e.getStatus()).body(e.getMessage());
		}
		
		/* Step 4.3 : move kraftwerk.json to a secondary folder */
		FileUtils.renameInputFile(inDirectory);

		/* Step 4.4 : move differential data to a secondary folder */
		try {
			FileUtils.moveInputFiles(getUserInputs(inDirectory));
		} catch (KraftwerkException e) {
			return ResponseEntity.status(e.getStatus()).body(e.getMessage());
		}
		
		//delete temp directory
		Path tempOutputPath = FileUtils.transformToTemp(inDirectory);
		try {
			FileUtils.deleteDirectory(tempOutputPath);
		} catch (KraftwerkException e) {
			return ResponseEntity.status(e.getStatus()).body(e.getMessage());
		}
		return ResponseEntity.ok(inDirectoryParam);

	}
	
	private UserInputs getUserInputs(Path inDirectory) {
		return new UserInputs(inDirectory.resolve(Constants.USER_INPUT_FILE), inDirectory);
	}
	
	private Path getInDirectory(String inDirectoryParam) throws KraftwerkException {
		Path inDirectory = Paths.get(inDirectoryParam);
		if (!verifyInDirectory(inDirectory)) inDirectory = Paths.get(defaultDirectory, "in", inDirectoryParam);
		if (!verifyInDirectory(inDirectory)) throw new KraftwerkException(HttpStatus.BAD_REQUEST.value(), "Configuration file not found");
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

	private void readDataset(String path,String bindingName, StepEnum previousStep, VtlBindings vtlBindings) {
		String pathBinding = path + File.separator + bindingName + "_" + previousStep.getStepLabel() +JSON;
		vtlExecute.putVtlDataset(pathBinding, bindingName, vtlBindings);
	}

}