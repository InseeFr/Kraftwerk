package fr.insee.kraftwerk.api;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import fr.insee.kraftwerk.core.KraftwerkError;
import fr.insee.kraftwerk.core.dataprocessing.StepEnum;
import fr.insee.kraftwerk.core.exceptions.KraftwerkException;
import fr.insee.kraftwerk.core.exceptions.NullException;
import fr.insee.kraftwerk.core.inputs.UserInputs;
import fr.insee.kraftwerk.core.metadata.MetadataUtils;
import fr.insee.kraftwerk.core.metadata.VariablesMap;
import fr.insee.kraftwerk.core.sequence.BuildBindingsSequence;
import fr.insee.kraftwerk.core.sequence.MultimodalSequence;
import fr.insee.kraftwerk.core.sequence.UnimodalSequence;
import fr.insee.kraftwerk.core.sequence.VtlReaderWriterSequence;
import fr.insee.kraftwerk.core.sequence.WriterSequence;
import fr.insee.kraftwerk.core.utils.FileUtils;
import fr.insee.kraftwerk.core.vtl.VtlBindings;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;



@RestController
@Tag(name = "${tag.stepbystep}")
public class StepByStepService extends KraftwerkService {

	@PutMapping(value = "/buildVtlBindings")
	@Operation(operationId = "buildVtlBindings", summary = "${summary.buildVtlBindings}", description = "${description.buildVtlBindings}")
	public ResponseEntity<String> buildVtlBindings(
			@Parameter(description = "${param.inDirectory}", required = true, example = INDIRECTORY_EXAMPLE) @RequestBody String inDirectoryParam
			)  {
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
				buildBindingsSequence.buildVtlBindings(userInputs, dataMode, vtlBindings,metadataVariables);
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
			buildBindingsSequence.buildVtlBindings(userInputs, dataMode, vtlBindings, metadataVariables);
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
		return archive(inDirectoryParam);

	}





}