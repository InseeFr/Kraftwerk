package fr.insee.kraftwerk.api.services;

import fr.insee.kraftwerk.api.process.MainProcessing;
import fr.insee.kraftwerk.core.KraftwerkError;
import fr.insee.kraftwerk.core.dataprocessing.StepEnum;
import fr.insee.kraftwerk.core.exceptions.KraftwerkException;
import fr.insee.kraftwerk.core.inputs.UserInputsFile;
import fr.insee.kraftwerk.core.metadata.MetadataModel;
import fr.insee.kraftwerk.core.metadata.MetadataUtils;
import fr.insee.kraftwerk.core.sequence.*;
import fr.insee.kraftwerk.core.utils.FileUtils;
import fr.insee.kraftwerk.core.utils.SqlUtils;
import fr.insee.kraftwerk.core.utils.TextFileWriter;
import fr.insee.kraftwerk.core.vtl.VtlBindings;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.nio.file.Path;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;



@RestController
@Tag(name = "${tag.stepbystep}")
public class StepByStepService extends KraftwerkService {

	@PutMapping(value = "/buildVtlBindings")
	@Operation(operationId = "buildVtlBindings", summary = "${summary.buildVtlBindings}", description = "${description.buildVtlBindings}")
	public ResponseEntity<String> buildVtlBindings(
			@Parameter(description = "${param.inDirectory}", required = true, example = INDIRECTORY_EXAMPLE) @RequestBody String inDirectoryParam,
			@Parameter(description = "${param.withAllReportingData}", required = false) @RequestParam(defaultValue = "true") boolean withAllReportingData
			) throws KraftwerkException {
		//Read data files
		boolean fileByFile = false;
		boolean withDDI = true;
		MainProcessing mp = new MainProcessing(inDirectoryParam, fileByFile,withAllReportingData,withDDI, defaultDirectory, limitSize);
		try {
			mp.init();
		} catch (KraftwerkException e) {
			return ResponseEntity.status(e.getStatus()).body(e.getMessage());
		}
				
		//Process
		BuildBindingsSequence buildBindingsSequence = new BuildBindingsSequence(withAllReportingData);
		VtlReaderWriterSequence vtlWriterSequence = new VtlReaderWriterSequence();

		for (String dataMode : mp.getUserInputsFile().getModeInputsMap().keySet()) {
			try (Statement database = SqlUtils.openConnection().createStatement()){
				buildBindingsSequence.buildVtlBindings(mp.getUserInputsFile(), dataMode, mp.getVtlBindings(),mp.getMetadataModels().get(dataMode), withDDI, null, database);
			} catch (KraftwerkException e) {
				return ResponseEntity.status(e.getStatus()).body(e.getMessage());
			} catch (SQLException e){
				throw new KraftwerkException(500,"SQL error");
			}

			vtlWriterSequence.writeTempBindings(mp.getInDirectory(), dataMode, mp.getVtlBindings(), StepEnum.BUILD_BINDINGS);
		}
		
		return ResponseEntity.ok(inDirectoryParam);


	}

	
	@PutMapping(value = "/buildVtlBindings/{dataMode}")
	@Operation(operationId = "buildVtlBindings", summary = "${summary.buildVtlBindings}", description = "${description.buildVtlBindings}")
	public ResponseEntity<String> buildVtlBindingsByDataMode(
			@Parameter(description = "${param.inDirectory}", required = true, example = INDIRECTORY_EXAMPLE) @RequestBody String inDirectoryParam,
			@Parameter(description = "${param.dataMode}", required = true) @PathVariable String dataMode,
			@Parameter(description = "${param.withAllReportingData}", required = false) @RequestParam(defaultValue = "true") boolean withAllReportingData
			) throws KraftwerkException {
		//Read data files
		boolean fileByFile = false;
		boolean withDDI = true;
		MainProcessing mp = new MainProcessing(inDirectoryParam, fileByFile,withAllReportingData,withDDI, defaultDirectory, limitSize);
		try {
			mp.init();
		} catch (KraftwerkException e) {
			return ResponseEntity.status(e.getStatus()).body(e.getMessage());
		}
		
		//Process
		BuildBindingsSequence buildBindingsSequence = new BuildBindingsSequence(withAllReportingData);
		try (Statement database = SqlUtils.openConnection().createStatement()){
			buildBindingsSequence.buildVtlBindings(mp.getUserInputsFile(), dataMode, mp.getVtlBindings(), mp.getMetadataModels().get(dataMode), withDDI, null,database);
		} catch (KraftwerkException e) {
			return ResponseEntity.status(e.getStatus()).body(e.getMessage());
		} catch (SQLException e){
			throw new KraftwerkException(500,"SQL error");
		}

        VtlReaderWriterSequence vtlWriterSequence = new VtlReaderWriterSequence();
		vtlWriterSequence.writeTempBindings(mp.getInDirectory(), dataMode, mp.getVtlBindings(), StepEnum.BUILD_BINDINGS);
		
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
		UserInputsFile userInputsFile;
		try {
			userInputsFile = controlInputSequence.getUserInputs(inDirectory);
		} catch (KraftwerkException e) {
			return ResponseEntity.status(e.getStatus()).body(e.getMessage());
		}
		VtlBindings vtlBindings = new VtlBindings();
		List<KraftwerkError> errors = new ArrayList<>();

		VtlReaderWriterSequence vtlReaderSequence = new VtlReaderWriterSequence();
		vtlReaderSequence.readDataset(FileUtils.transformToTemp(inDirectory).toString(),dataMode, StepEnum.BUILD_BINDINGS, vtlBindings);

		Map<String, MetadataModel> metadataModelMap = MetadataUtils.getMetadata(userInputsFile.getModeInputsMap());
		
		//Process
		UnimodalSequence unimodal = new UnimodalSequence();
		unimodal.applyUnimodalSequence(userInputsFile, dataMode, vtlBindings, errors, metadataModelMap);
		
		//Write technical outputs
		VtlReaderWriterSequence vtlWriterSequence = new VtlReaderWriterSequence();
		vtlWriterSequence.writeTempBindings(inDirectory, dataMode, vtlBindings, StepEnum.UNIMODAL_PROCESSING);
		TextFileWriter.writeErrorsFile(inDirectory, LocalDateTime.now(), errors);
		
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
		UserInputsFile userInputsFile;
		try {
			userInputsFile = controlInputSequence.getUserInputs(inDirectory);
		} catch (KraftwerkException e) {
			return ResponseEntity.status(e.getStatus()).body(e.getMessage());
		}
		List<KraftwerkError> errors = new ArrayList<>();


		VtlReaderWriterSequence vtlReaderWriterSequence = new VtlReaderWriterSequence();

		//Test
		VtlBindings vtlBindings = new VtlBindings();
		for (String dataMode : userInputsFile.getModeInputsMap().keySet()) {
			vtlReaderWriterSequence.readDataset(FileUtils.transformToTemp(inDirectory).toString(),dataMode, StepEnum.UNIMODAL_PROCESSING, vtlBindings);
		}

		Map<String, MetadataModel> metadataModelMap = MetadataUtils.getMetadata(userInputsFile.getModeInputsMap());

		//Process
		MultimodalSequence multimodalSequence = new MultimodalSequence();
			multimodalSequence.multimodalProcessing(userInputsFile, vtlBindings, errors, metadataModelMap);

		//Write technical fils
		for (String datasetName : vtlBindings.getDatasetNames()) {
			vtlReaderWriterSequence.writeTempBindings(inDirectory, datasetName, vtlBindings, StepEnum.MULTIMODAL_PROCESSING);
		}
		TextFileWriter.writeErrorsFile(inDirectory, LocalDateTime.now(), errors);
		
		return ResponseEntity.ok(inDirectoryParam);

	}
	

	@PutMapping(value = "/writeOutputFiles")
	@Operation(operationId = "writeOutputFiles", summary = "${summary.writeOutputFiles}", description = "${description.writeOutputFiles}")
	public ResponseEntity<String> writeOutputFiles(
			@Parameter(description = "${param.inDirectory}", required = true, example = INDIRECTORY_EXAMPLE) @RequestBody  String inDirectoryParam
			) throws KraftwerkException, SQLException {
		Path inDirectory;
		try {
			inDirectory = controlInputSequence.getInDirectory(inDirectoryParam);
		} catch (KraftwerkException e) {
			return ResponseEntity.status(e.getStatus()).body(e.getMessage());
		}
		LocalDateTime executionDateTime = LocalDateTime.now();
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
		UserInputsFile userInputsFile;
		try {
			userInputsFile = controlInputSequence.getUserInputs(inDirectory);
		} catch (KraftwerkException e) {
			return ResponseEntity.status(e.getStatus()).body(e.getMessage());
		}
		Map<String, MetadataModel> metadataModelMap = MetadataUtils.getMetadata(userInputsFile.getModeInputsMap());
		try (Statement database = SqlUtils.openConnection().createStatement()) {
			writerSequence.writeOutputFiles(inDirectory, executionDateTime, vtlBindings, userInputsFile.getModeInputsMap(), metadataModelMap, errors, database);
		}
		return ResponseEntity.ok(inDirectoryParam);

	}


	
	@PutMapping(value = "/archive")
	@Operation(operationId = "archive", summary = "${summary.archive}", description = "${description.archive}")
	public ResponseEntity<String> archiveService(
			@Parameter(description = "${param.inDirectory}", required = true, example = INDIRECTORY_EXAMPLE) @RequestBody  String inDirectoryParam) 
			{
		return archive(inDirectoryParam);

	}





}