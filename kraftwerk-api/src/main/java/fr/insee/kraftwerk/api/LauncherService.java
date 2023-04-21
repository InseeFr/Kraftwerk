package fr.insee.kraftwerk.api;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import fr.insee.kraftwerk.core.vtl.ErrorVtlTransformation;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import fr.insee.kraftwerk.core.Constants;
import fr.insee.kraftwerk.core.KraftwerkError;
import fr.insee.kraftwerk.core.dataprocessing.StepEnum;
import fr.insee.kraftwerk.core.exceptions.KraftwerkException;
import fr.insee.kraftwerk.core.exceptions.NullException;
import fr.insee.kraftwerk.core.inputs.ModeInputs;
import fr.insee.kraftwerk.core.inputs.UserInputs;
import fr.insee.kraftwerk.core.metadata.MetadataUtils;
import fr.insee.kraftwerk.core.metadata.VariablesMap;
import fr.insee.kraftwerk.core.sequence.BuildBindingsSequence;
import fr.insee.kraftwerk.core.sequence.ControlInputSequence;
import fr.insee.kraftwerk.core.sequence.MultimodalSequence;
import fr.insee.kraftwerk.core.sequence.UnimodalSequence;
import fr.insee.kraftwerk.core.sequence.VtlReaderWriterSequence;
import fr.insee.kraftwerk.core.sequence.WriterSequence;
import fr.insee.kraftwerk.core.utils.FileUtils;
import fr.insee.kraftwerk.core.vtl.VtlBindings;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;



@Slf4j
@RestController
@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "Success"),
		@ApiResponse(responseCode = "400", description = "Bad Request"),
		@ApiResponse(responseCode = "500", description = "Internal server error") })
public class LauncherService {

	private static final String INDIRECTORY_EXAMPLE = "LOG-2021-x12-web";
	private static final String JSON = ".json";
	
	@Value("${fr.insee.postcollecte.csv.output.quote}")
	private String csvOutputsQuoteChar;
	
	@Value("${fr.insee.postcollecte.files}")
	private String defaultDirectory;
	
	private ControlInputSequence controlInputSequence;
	
	@PostConstruct
	public void initializeWithProperties() {
		if (StringUtils.isNotEmpty(csvOutputsQuoteChar)) {
			Constants.setCsvOutputQuoteChar(csvOutputsQuoteChar.trim().charAt(0));
		}
		controlInputSequence = new ControlInputSequence(defaultDirectory);
	}

	@PutMapping(value = "/main")
	@Operation(operationId = "main", summary = "${summary.main}", description = "${description.main}")
	public ResponseEntity<String> main(
			@Parameter(description = "${param.inDirectory}", required = true, example = INDIRECTORY_EXAMPLE) @RequestBody String inDirectoryParam,
			@Parameter(description = "${param.archiveAtEnd}", required = false) @RequestParam(defaultValue = "false") boolean archiveAtEnd
			) {
		boolean withDDI = true;
		/* Step 1 : Init */
		Path inDirectory;
		try {
			inDirectory = controlInputSequence.getInDirectory(inDirectoryParam);
		} catch (KraftwerkException e) {
			return ResponseEntity.status(e.getStatus()).body(e.getMessage());
		}
		String campaignName = inDirectory.getFileName().toString();
		log.info("Kraftwerk main service started for campaign: " + campaignName);

		UserInputs userInputs;
		try {
			userInputs = controlInputSequence.getUserInputs(inDirectory);
		} catch (KraftwerkException e) {
			return ResponseEntity.status(e.getStatus()).body(e.getMessage());
		}
		VtlBindings vtlBindings = new VtlBindings();
		List<KraftwerkError> errors = new ArrayList<>();

		Map<String, VariablesMap> metadataVariables = MetadataUtils.getMetadata(userInputs.getModeInputsMap());

		/* Step 2 : unimodal data */
		BuildBindingsSequence buildBindingsSequence = new BuildBindingsSequence();
		for (String dataMode : userInputs.getModeInputsMap().keySet()) {
			try {
				buildBindingsSequence.buildVtlBindings(userInputs, dataMode, vtlBindings, metadataVariables, withDDI);
			} catch (NullException e) {
				return ResponseEntity.status(e.getStatus()).body(e.getMessage());
			}
			UnimodalSequence unimodal = new UnimodalSequence();
			unimodal.unimodalProcessing(userInputs, dataMode, vtlBindings, errors,metadataVariables);
		}

		/* Step 3 : multimodal VTL data processing */
		MultimodalSequence multimodalSequence = new MultimodalSequence();
		multimodalSequence.multimodalProcessing(userInputs, vtlBindings, errors, metadataVariables);

		/* Step 4 : Write output files */
		WriterSequence writerSequence = new WriterSequence();
		writerSequence.writeOutputFiles(inDirectory, vtlBindings, userInputs.getModeInputsMap(), userInputs.getMultimodeDatasetName(),metadataVariables, errors);
		writeErrorsFile(inDirectory, errors);

		/* Step 4.3- 4.4 : Archive */
		if (Boolean.TRUE.equals(archiveAtEnd)) archive(inDirectoryParam);

		return ResponseEntity.ok(campaignName);
	}

	@PutMapping(value = "/main/file-by-file")
	@Operation(operationId = "mainFileByFile", summary = "${summary.fileByFile}", description = "${description.fileByFile}")
	public ResponseEntity<String> mainFileByFile(
			@Parameter(description = "${param.inDirectory}", required = true, example = INDIRECTORY_EXAMPLE) @RequestBody String inDirectoryParam,
			@Parameter(description = "${param.archiveAtEnd}", required = false) @RequestParam(defaultValue = "false") boolean archiveAtEnd
	) {
		boolean withDDI = true;
		/* Step 1 : Init */
		Path inDirectory;
		try {
			inDirectory = controlInputSequence.getInDirectory(inDirectoryParam);
		} catch (KraftwerkException e) {
			return ResponseEntity.status(e.getStatus()).body(e.getMessage());
		}
		String campaignName = inDirectory.getFileName().toString();
		log.info("Kraftwerk main service started for campaign: " + campaignName);

		UserInputs userInputsSource;
		try {
			userInputsSource = controlInputSequence.getUserInputs(inDirectory);
		} catch (KraftwerkException e) {
			return ResponseEntity.status(e.getStatus()).body(e.getMessage());
		}

		Map<String, VariablesMap> metadataVariables = MetadataUtils.getMetadata(userInputsSource.getModeInputsMap());
		List<UserInputs> userInputsList = getUserInputs(userInputsSource);
		List<KraftwerkError> errors = new ArrayList<>();

		for (UserInputs userInputs : userInputsList){
			VtlBindings vtlBindings = new VtlBindings();
			/* Step 2 : unimodal data */
			BuildBindingsSequence buildBindingsSequence = new BuildBindingsSequence();
			for (String dataMode : userInputs.getModeInputsMap().keySet()) {
				try {
					buildBindingsSequence.buildVtlBindings(userInputs, dataMode, vtlBindings, metadataVariables, withDDI);
				} catch (NullException e) {
					return ResponseEntity.status(e.getStatus()).body(e.getMessage());
				}
				UnimodalSequence unimodal = new UnimodalSequence();
				unimodal.unimodalProcessing(userInputs, dataMode, vtlBindings, errors, metadataVariables);
			}

			/* Step 3 : multimodal VTL data processing */
			MultimodalSequence multimodalSequence = new MultimodalSequence();
			multimodalSequence.multimodalProcessing(userInputs, vtlBindings, errors, metadataVariables);

			/* Step 4 : Write output files */
			WriterSequence writerSequence = new WriterSequence();
			writerSequence.writeOutputFiles(inDirectory, vtlBindings, userInputs.getModeInputsMap(), userInputs.getMultimodeDatasetName(), metadataVariables, errors);

			/* Step 4.3- 4.4 : Archive */
			if (Boolean.TRUE.equals(archiveAtEnd)) archive(inDirectoryParam);
		}
		writeErrorsFile(inDirectory, errors);

		return ResponseEntity.ok(campaignName);
	}

	@PutMapping(value = "/main/lunatic-only")
	@Operation(operationId = "mainLunaticOnly", summary = "${summary.mainLunaticOnly}", description = "${description.mainLunaticOnly}")
	public ResponseEntity<String> mainLunaticOnly(
			@Parameter(description = "${param.inDirectory}", required = true, example = INDIRECTORY_EXAMPLE) @RequestBody String inDirectoryParam,
			@Parameter(description = "${param.archiveAtEnd}", required = false) @RequestParam(defaultValue = "false") boolean archiveAtEnd
	) {
		boolean withDDI = false;
		/* Step 1 : Init */
		Path inDirectory;
		try {
			inDirectory = controlInputSequence.getInDirectory(inDirectoryParam);
		} catch (KraftwerkException e) {
			return ResponseEntity.status(e.getStatus()).body(e.getMessage());
		}
		String campaignName = inDirectory.getFileName().toString();
		log.info("Kraftwerk main service started for campaign: " + campaignName);

		UserInputs userInputs;
		try {
			userInputs = controlInputSequence.getUserInputs(inDirectory);
		} catch (KraftwerkException e) {
			return ResponseEntity.status(e.getStatus()).body(e.getMessage());
		}

		VtlBindings vtlBindings = new VtlBindings();
		List<KraftwerkError> errors = new ArrayList<>();
;
		Map<String, VariablesMap> metadataVariables = MetadataUtils.getMetadataFromLunatic(userInputs.getModeInputsMap());

		/* Step 2 : unimodal data */
		BuildBindingsSequence buildBindingsSequence = new BuildBindingsSequence();
		for (String dataMode : userInputs.getModeInputsMap().keySet()) {
			try {
				buildBindingsSequence.buildVtlBindings(userInputs, dataMode, vtlBindings, metadataVariables,withDDI);
			} catch (NullException e) {
				return ResponseEntity.status(e.getStatus()).body(e.getMessage());
			}
			UnimodalSequence unimodal = new UnimodalSequence();
			unimodal.unimodalProcessing(userInputs, dataMode, vtlBindings, errors,metadataVariables);
		}

		/* Step 3 : multimodal VTL data processing */
		MultimodalSequence multimodalSequence = new MultimodalSequence();
		multimodalSequence.multimodalProcessing(userInputs, vtlBindings, errors, metadataVariables);

		/* Step 4 : Write output files */
		WriterSequence writerSequence = new WriterSequence();
		writerSequence.writeOutputFiles(inDirectory, vtlBindings, userInputs.getModeInputsMap(), userInputs.getMultimodeDatasetName(),metadataVariables, errors);
		writeErrorsFile(inDirectory, errors);

		/* Step 4.3- 4.4 : Archive */
		if (Boolean.TRUE.equals(archiveAtEnd)) archive(inDirectoryParam);

		return ResponseEntity.ok(campaignName);
	}

	private void writeErrorsFile(Path inDirectory, List<KraftwerkError> errors) {
		Path tempOutputPath = FileUtils.transformToOut(inDirectory).resolve("errors.txt");
		FileUtils.createDirectoryIfNotExist(tempOutputPath.getParent());

		//Write errors file
		if (!errors.isEmpty()) {
			try (FileWriter myWriter = new FileWriter(tempOutputPath.toFile(),true)){
				for (KraftwerkError error : errors){
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
		boolean withDDI = true;
		//Read data files
		Path inDirectory;
		try {
			inDirectory = controlInputSequence.getInDirectory(inDirectoryParam);
		} catch (KraftwerkException e) {
			return ResponseEntity.status(e.getStatus()).body(e.getMessage());
		}
		UserInputs userInputs;
		try {
			userInputs = controlInputSequence.getUserInputs(inDirectory);
		} catch (KraftwerkException e) {
			return ResponseEntity.status(e.getStatus()).body(e.getMessage());
		}

		Map<String, VariablesMap> metadataVariables = MetadataUtils.getMetadata(userInputs.getModeInputsMap());
		
		//Process
		BuildBindingsSequence buildBindingsSequence = new BuildBindingsSequence();
		VtlReaderWriterSequence vtlWriterSequence = new VtlReaderWriterSequence();

		for (String dataMode : userInputs.getModeInputsMap().keySet()) {
			VtlBindings vtlBindings = new VtlBindings();
			try {
				buildBindingsSequence.buildVtlBindings(userInputs, dataMode, vtlBindings,metadataVariables,withDDI);
			} catch (NullException e) {
				return ResponseEntity.status(e.getStatus()).body(e.getMessage());
			}
			
			vtlWriterSequence.writeTempBindings(inDirectory, dataMode, vtlBindings, StepEnum.BUILD_BINDINGS);
		}
		
		return ResponseEntity.ok(inDirectoryParam);


	}

	
	@PutMapping(value = "/buildVtlBindings/{dataMode}")
	@Operation(operationId = "buildVtlBindings", summary = "${summary.buildVtlBindings}", description = "${description.buildVtlBindings}")
	public ResponseEntity<String> buildVtlBindingsByDataMode(
			@Parameter(description = "${param.inDirectory}", required = true, example = INDIRECTORY_EXAMPLE) @RequestBody String inDirectoryParam,
			@Parameter(description = "${param.dataMode}", required = true) @PathVariable String dataMode
			)  {
		boolean withDDI = true;
		//Read data files
		Path inDirectory;
		try {
			inDirectory = controlInputSequence.getInDirectory(inDirectoryParam);
		} catch (KraftwerkException e) {
			return ResponseEntity.status(e.getStatus()).body(e.getMessage());
		}
		UserInputs userInputs;
		try {
			userInputs = controlInputSequence.getUserInputs(inDirectory);
		} catch (KraftwerkException e) {
			return ResponseEntity.status(e.getStatus()).body(e.getMessage());
		}
		VtlBindings vtlBindings = new VtlBindings();

		Map<String, VariablesMap> metadataVariables = MetadataUtils.getMetadata(userInputs.getModeInputsMap());
		
		//Process
		BuildBindingsSequence buildBindingsSequence = new BuildBindingsSequence();
		try {
			buildBindingsSequence.buildVtlBindings(userInputs, dataMode, vtlBindings, metadataVariables,withDDI);
		} catch (NullException e) {
			return ResponseEntity.status(e.getStatus()).body(e.getMessage());
		}
		
		VtlReaderWriterSequence vtlWriterSequence = new VtlReaderWriterSequence();
		vtlWriterSequence.writeTempBindings(inDirectory, dataMode, vtlBindings, StepEnum.BUILD_BINDINGS);
		
		return ResponseEntity.ok(inDirectoryParam+ " - "+dataMode);

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
			inDirectory = controlInputSequence.getInDirectory(inDirectoryParam);
		} catch (KraftwerkException e) {
			return ResponseEntity.status(e.getStatus()).body(e.getMessage());
		}
		UserInputs userInputs;
		try {
			userInputs = controlInputSequence.getUserInputs(inDirectory);
		} catch (KraftwerkException e) {
			return ResponseEntity.status(e.getStatus()).body(e.getMessage());
		}
		VtlBindings vtlBindings = new VtlBindings();
		List<KraftwerkError> errors = new ArrayList<>();

		VtlReaderWriterSequence vtlReaderSequence = new VtlReaderWriterSequence();
		vtlReaderSequence.readDataset(FileUtils.transformToTemp(inDirectory).toString(),dataMode, StepEnum.BUILD_BINDINGS, vtlBindings);

		Map<String, VariablesMap> metadataVariables = MetadataUtils.getMetadata(userInputs.getModeInputsMap());
		
		//Process
		UnimodalSequence unimodal = new UnimodalSequence();
		unimodal.unimodalProcessing(userInputs, dataMode, vtlBindings, errors, metadataVariables);
		
		//Write technical outputs
		VtlReaderWriterSequence vtlWriterSequence = new VtlReaderWriterSequence();
		vtlWriterSequence.writeTempBindings(inDirectory, dataMode, vtlBindings, StepEnum.UNIMODAL_PROCESSING);
		writeErrorsFile(inDirectory, errors);
		
		return ResponseEntity.ok(inDirectoryParam+ " - "+dataMode);

	}
	
	

	@PutMapping(value = "/multimodalProcessing")
	@Operation(operationId = "multimodalProcessing", summary = "${summary.multimodalProcessing}", description = "${description.multimodalProcessing}")
	public ResponseEntity<String> multimodalProcessing(
			@Parameter(description = "${param.inDirectory}", required = true, example = INDIRECTORY_EXAMPLE) @RequestBody String inDirectoryParam
			)  {
		//Read data in JSON file
		Path inDirectory;
		try {
			inDirectory = controlInputSequence.getInDirectory(inDirectoryParam);
		} catch (KraftwerkException e) {
			return ResponseEntity.status(e.getStatus()).body(e.getMessage());
		}
		UserInputs userInputs;
		try {
			userInputs = controlInputSequence.getUserInputs(inDirectory);
		} catch (KraftwerkException e) {
			return ResponseEntity.status(e.getStatus()).body(e.getMessage());
		}
		List<KraftwerkError> errors = new ArrayList<>();


		VtlReaderWriterSequence vtlReaderWriterSequence = new VtlReaderWriterSequence();

		//Test
		VtlBindings vtlBindings = new VtlBindings();
		for (String dataMode : userInputs.getModeInputsMap().keySet()) {
			vtlReaderWriterSequence.readDataset(FileUtils.transformToTemp(inDirectory).toString(),dataMode, StepEnum.UNIMODAL_PROCESSING, vtlBindings);
		}

		Map<String, VariablesMap> metadataVariables = MetadataUtils.getMetadata(userInputs.getModeInputsMap());

		//Process
		MultimodalSequence multimodalSequence = new MultimodalSequence();
		multimodalSequence.multimodalProcessing(userInputs, vtlBindings, errors, metadataVariables);

		//Write technical fils
		for (String datasetName : vtlBindings.getDatasetNames()) {
			vtlReaderWriterSequence.writeTempBindings(inDirectory, datasetName, vtlBindings, StepEnum.MULTIMODAL_PROCESSING);
		}
		writeErrorsFile(inDirectory, errors);
		
		return ResponseEntity.ok(inDirectoryParam);

	}
	

	@PutMapping(value = "/writeOutputFiles")
	@Operation(operationId = "writeOutputFiles", summary = "${summary.writeOutputFiles}", description = "${description.writeOutputFiles}")
	public ResponseEntity<String> writeOutputFiles(
			@Parameter(description = "${param.inDirectory}", required = true, example = INDIRECTORY_EXAMPLE) @RequestBody  String inDirectoryParam
			) {
		Path inDirectory;
		try {
			inDirectory = controlInputSequence.getInDirectory(inDirectoryParam);
		} catch (KraftwerkException e) {
			return ResponseEntity.status(e.getStatus()).body(e.getMessage());
		}
		VtlBindings vtlBindings = new VtlBindings();
		List<KraftwerkError> errors = new ArrayList<>();
		// Read all bindings necessary to produce output
		String path = FileUtils.transformToTemp(inDirectory).toString();
		List<String> fileNames = FileUtils.listFiles(path);
		fileNames = fileNames.stream().filter(name -> name.endsWith(StepEnum.MULTIMODAL_PROCESSING.getStepLabel()+JSON)).toList();
		for (String name : fileNames){
			String pathBindings = path + File.separator + name;
			String bindingName =  name.substring(0, name.indexOf("_"+StepEnum.MULTIMODAL_PROCESSING.getStepLabel()));
			VtlReaderWriterSequence vtlReaderSequence = new VtlReaderWriterSequence();
			vtlReaderSequence.readDataset(pathBindings, bindingName, vtlBindings);
		}
		WriterSequence writerSequence = new WriterSequence();
		UserInputs userInputs;
		try {
			userInputs = controlInputSequence.getUserInputs(inDirectory);
		} catch (KraftwerkException e) {
			return ResponseEntity.status(e.getStatus()).body(e.getMessage());
		}
		Map<String, VariablesMap> metadataVariables = MetadataUtils.getMetadata(userInputs.getModeInputsMap());
		writerSequence.writeOutputFiles(inDirectory, vtlBindings, userInputs.getModeInputsMap(), userInputs.getMultimodeDatasetName(), metadataVariables, errors);
		return ResponseEntity.ok(inDirectoryParam);

	}


	
	@PutMapping(value = "/archive")
	@Operation(operationId = "archive", summary = "${summary.archive}", description = "${description.archive}")
	public ResponseEntity<String> archive(
			@Parameter(description = "${param.inDirectory}", required = true, example = INDIRECTORY_EXAMPLE) @RequestBody  String inDirectoryParam) 
			{
		Path inDirectory;
		try {
			inDirectory = controlInputSequence.getInDirectory(inDirectoryParam);
		} catch (KraftwerkException e) {
			return ResponseEntity.status(e.getStatus()).body(e.getMessage());
		}
		
		/* Step 4.3 : move kraftwerk.json to a secondary folder */
		FileUtils.renameInputFile(inDirectory);

		/* Step 4.4 : move differential data to a secondary folder */
		try {
			FileUtils.moveInputFiles(controlInputSequence.getUserInputs(inDirectory));
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

	private static List<Path> getFilesToProcess(UserInputs userInputs, String dataMode){
		List<Path> files = new ArrayList<>();
		ModeInputs modeInputs = userInputs.getModeInputs(dataMode);
		Path dataPath = modeInputs.getDataFile();
		if (dataPath == null) log.error("Datapath is null");
		else {
			if (Files.isRegularFile(dataPath)) {
				files.add(dataPath);
			}
			else if (Files.isDirectory(dataPath)) {
				try (Stream<Path> stream = Files.list(dataPath)){
					stream.forEach(files::add);
				} catch (IOException e) {
					log.error(String.format("IOException occurred when trying to list data files of folder: %s", dataPath));
				}
			}
			else {
				log.warn(String.format("Data path given could not be identified as a file or folder: %s", dataPath));
			}
		}
		return files;
	}

	private static List<UserInputs> getUserInputs(UserInputs source){
		List<UserInputs> userInputsList = new ArrayList<>();
		for (String dataMode : source.getModeInputsMap().keySet()) {
			List<Path> dataFiles = getFilesToProcess(source, dataMode);
			for (Path dataFile : dataFiles) {
				UserInputs currentFileInputs = new UserInputs();
				currentFileInputs.setUserInputFile(source.getUserInputFile());
				currentFileInputs.setInputDirectory(source.getInputDirectory());
				currentFileInputs.setVtlReconciliationFile(source.getVtlReconciliationFile());
				currentFileInputs.setVtlInformationLevelsFile(source.getVtlInformationLevelsFile());
				currentFileInputs.setVtlTransformationsFile(source.getVtlTransformationsFile());
				currentFileInputs.setMultimodeDatasetName(source.getMultimodeDatasetName());
				ModeInputs sourceModeInputs = source.getModeInputs(dataMode);
				ModeInputs currentFileModeInputs = new ModeInputs();
				currentFileModeInputs.setDataFile(dataFile);
				currentFileModeInputs.setDdiUrl(sourceModeInputs.getDdiUrl());
				currentFileModeInputs.setLunaticFile(sourceModeInputs.getLunaticFile());
				currentFileModeInputs.setDataFormat(sourceModeInputs.getDataFormat().toString());
				currentFileModeInputs.setDataMode(sourceModeInputs.getDataMode());
				currentFileModeInputs.setModeVtlFile(sourceModeInputs.getModeVtlFile());
				currentFileModeInputs.setParadataFolder(sourceModeInputs.getParadataFolder());
				currentFileModeInputs.setReportingDataFile(sourceModeInputs.getReportingDataFile());
				currentFileInputs.getModeInputsMap().put(dataMode,currentFileModeInputs);
				userInputsList.add(currentFileInputs);
			}
		}
		return userInputsList;
	}

}