package fr.insee.kraftwerk.api;


import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import fr.insee.kraftwerk.core.KraftwerkError;
import fr.insee.kraftwerk.core.exceptions.KraftwerkException;
import fr.insee.kraftwerk.core.exceptions.NullException;
import fr.insee.kraftwerk.core.inputs.ModeInputs;
import fr.insee.kraftwerk.core.inputs.UserInputs;
import fr.insee.kraftwerk.core.metadata.MetadataUtils;
import fr.insee.kraftwerk.core.metadata.VariablesMap;
import fr.insee.kraftwerk.core.sequence.BuildBindingsSequence;
import fr.insee.kraftwerk.core.sequence.MultimodalSequence;
import fr.insee.kraftwerk.core.sequence.UnimodalSequence;
import fr.insee.kraftwerk.core.sequence.WriterSequence;
import fr.insee.kraftwerk.core.vtl.VtlBindings;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.log4j.Log4j2;



@Log4j2
@RestController
@Tag(name = "${tag.main}")
public class MainService extends KraftwerkService {

	@PutMapping(value = "/main")
	@Operation(operationId = "main", summary = "${summary.main}", description = "${description.main}")
	public ResponseEntity<String> main(
			@Parameter(description = "${param.inDirectory}", required = true, example = INDIRECTORY_EXAMPLE) @RequestBody String inDirectoryParam,
			@Parameter(description = "${param.archiveAtEnd}", required = false) @RequestParam(defaultValue = "false") boolean archiveAtEnd
			) {
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
				buildBindingsSequence.buildVtlBindings(userInputs, dataMode, vtlBindings, metadataVariables);
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
	@Operation(operationId = "main", summary = "${summary.fileByFile}", description = "${description.fileByFile}")
	public ResponseEntity<String> mainFileByFile(
			@Parameter(description = "${param.inDirectory}", required = true, example = INDIRECTORY_EXAMPLE) @RequestBody String inDirectoryParam,
			@Parameter(description = "${param.archiveAtEnd}", required = false) @RequestParam(defaultValue = "false") boolean archiveAtEnd
	) {
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
					buildBindingsSequence.buildVtlBindings(userInputs, dataMode, vtlBindings, metadataVariables);
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