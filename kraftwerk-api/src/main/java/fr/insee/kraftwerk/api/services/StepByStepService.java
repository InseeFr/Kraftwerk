package fr.insee.kraftwerk.api.services;

import fr.insee.bpm.metadata.model.MetadataModel;
import fr.insee.kraftwerk.api.configuration.ConfigProperties;
import fr.insee.kraftwerk.api.configuration.MinioConfig;
import fr.insee.kraftwerk.api.process.MainProcessing;
import fr.insee.kraftwerk.core.dataprocessing.StepEnum;
import fr.insee.kraftwerk.core.exceptions.KraftwerkException;
import fr.insee.kraftwerk.core.inputs.UserInputsFile;
import fr.insee.kraftwerk.core.metadata.MetadataUtils;
import fr.insee.kraftwerk.core.sequence.BuildBindingsSequence;
import fr.insee.kraftwerk.core.sequence.MultimodalSequence;
import fr.insee.kraftwerk.core.sequence.UnimodalSequence;
import fr.insee.kraftwerk.core.sequence.VtlReaderWriterSequence;
import fr.insee.kraftwerk.core.sequence.WriterSequence;
import fr.insee.kraftwerk.core.utils.SqlUtils;
import fr.insee.kraftwerk.core.utils.TextFileWriter;
import fr.insee.kraftwerk.core.utils.files.FileSystemImpl;
import fr.insee.kraftwerk.core.utils.files.FileUtilsInterface;
import fr.insee.kraftwerk.core.utils.files.MinioImpl;
import fr.insee.kraftwerk.core.utils.log.KraftwerkExecutionContext;
import fr.insee.kraftwerk.core.vtl.VtlBindings;
import io.minio.MinioClient;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.nio.file.Path;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Map;



@RestController
@Tag(name = "${tag.stepbystep}")
@RequestMapping("/steps")
@Slf4j
public class StepByStepService extends KraftwerkService {
	MinioClient minioClient;
	boolean useMinio;

	@Autowired
	public StepByStepService(ConfigProperties configProperties, MinioConfig minioConfig) {
		super(configProperties, minioConfig);
		useMinio = false;
		if(minioConfig == null){
			log.warn("Minio config null !");
		}
		if(minioConfig != null && minioConfig.isEnable()){
			minioClient = MinioClient.builder().endpoint(minioConfig.getEndpoint()).credentials(minioConfig.getAccessKey(), minioConfig.getSecretKey()).build();
			useMinio = true;
		}
    }

	@PutMapping(value = "/buildVtlBindings")
	@Operation(operationId = "buildVtlBindings", summary = "${summary.buildVtlBindings}", description = "${description.buildVtlBindings}")
	public ResponseEntity<String> buildVtlBindings(
			@Parameter(description = "${param.inDirectory}", required = true, example = INDIRECTORY_EXAMPLE) @RequestBody String inDirectoryParam,
			@Parameter(description = "${param.withAllReportingData}", required = false) @RequestParam(defaultValue = "true") boolean withAllReportingData
			) {
		//Read data files
		boolean fileByFile = false;
		boolean withDDI = true;
		FileUtilsInterface fileUtilsInterface = getFileUtilsInterface();

		MainProcessing mp = new MainProcessing(inDirectoryParam, fileByFile,withAllReportingData,withDDI, defaultDirectory, limitSize, fileUtilsInterface);
		try {
			mp.init();
		} catch (KraftwerkException e) {
			return ResponseEntity.status(e.getStatus()).body(e.getMessage());
		}
				
		//Process
		BuildBindingsSequence buildBindingsSequence = new BuildBindingsSequence(withAllReportingData, fileUtilsInterface);
		VtlReaderWriterSequence vtlWriterSequence = new VtlReaderWriterSequence(fileUtilsInterface);
		KraftwerkExecutionContext kraftwerkExecutionContext = new KraftwerkExecutionContext();

		for (String dataMode : mp.getUserInputsFile().getModeInputsMap().keySet()) {
			try{
				buildBindingsSequence.buildVtlBindings(mp.getUserInputsFile(), dataMode, mp.getVtlBindings(),mp.getMetadataModels().get(dataMode), withDDI, kraftwerkExecutionContext);
			} catch (KraftwerkException e){
				return ResponseEntity.status(e.getStatus()).body(e.getMessage());
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
			) {
		//Read data files
		boolean fileByFile = false;
		boolean withDDI = true;
		FileUtilsInterface fileUtilsInterface = getFileUtilsInterface();

		KraftwerkExecutionContext kraftwerkExecutionContext = new KraftwerkExecutionContext();
		MainProcessing mp = new MainProcessing(inDirectoryParam, fileByFile,withAllReportingData,withDDI, defaultDirectory, limitSize, fileUtilsInterface);
		try {
			mp.init();
		} catch (KraftwerkException e) {
			return ResponseEntity.status(e.getStatus()).body(e.getMessage());
		}
		
		//Process
		BuildBindingsSequence buildBindingsSequence = new BuildBindingsSequence(withAllReportingData, fileUtilsInterface);
		try{
			buildBindingsSequence.buildVtlBindings(mp.getUserInputsFile(), dataMode, mp.getVtlBindings(), mp.getMetadataModels().get(dataMode), withDDI, kraftwerkExecutionContext);
		} catch (KraftwerkException e) {
			return ResponseEntity.status(e.getStatus()).body(e.getMessage());
		}

        VtlReaderWriterSequence vtlWriterSequence = new VtlReaderWriterSequence(fileUtilsInterface);
		vtlWriterSequence.writeTempBindings(mp.getInDirectory(), dataMode, mp.getVtlBindings(), StepEnum.BUILD_BINDINGS);
		
		return ResponseEntity.ok(inDirectoryParam+ " - "+dataMode);

	}



	@PutMapping(value = "/unimodalProcessing")
	@Operation(operationId = "unimodalProcessing", summary = "${summary.unimodalProcessing}", description = "${description.unimodalProcessing}")
	public ResponseEntity<String> unimodalProcessing(
			@Parameter(description = "${param.inDirectory}", required = true, example = INDIRECTORY_EXAMPLE) @RequestBody  String inDirectoryParam,
			@Parameter(description = "${param.dataMode}", required = true) @RequestParam  String dataMode
			) throws KraftwerkException {
		FileUtilsInterface fileUtilsInterface = getFileUtilsInterface();
		KraftwerkExecutionContext kraftwerkExecutionContext = new KraftwerkExecutionContext();

		//Read data in JSON file
		Path inDirectory;
		try {
			inDirectory = controlInputSequence.getInDirectory(inDirectoryParam);
		} catch (KraftwerkException e) {
			return ResponseEntity.status(e.getStatus()).body(e.getMessage());
		}
		UserInputsFile userInputsFile;
		try {
			userInputsFile = controlInputSequence.getUserInputs(inDirectory, fileUtilsInterface);
		} catch (KraftwerkException e) {
			return ResponseEntity.status(e.getStatus()).body(e.getMessage());
		}
		VtlBindings vtlBindings = new VtlBindings();

		VtlReaderWriterSequence vtlReaderSequence = new VtlReaderWriterSequence(fileUtilsInterface);
		vtlReaderSequence.readDataset(FileUtilsInterface.transformToTemp(inDirectory).toString(),dataMode, StepEnum.BUILD_BINDINGS, vtlBindings);

		Map<String, MetadataModel> metadataModelMap = MetadataUtils.getMetadata(userInputsFile.getModeInputsMap(), fileUtilsInterface);
		
		//Process
		UnimodalSequence unimodal = new UnimodalSequence();
		unimodal.applyUnimodalSequence(userInputsFile, dataMode, vtlBindings, kraftwerkExecutionContext, metadataModelMap, fileUtilsInterface);
		
		//Write technical outputs
		VtlReaderWriterSequence vtlWriterSequence = new VtlReaderWriterSequence(fileUtilsInterface);
		vtlWriterSequence.writeTempBindings(inDirectory, dataMode, vtlBindings, StepEnum.UNIMODAL_PROCESSING);
		TextFileWriter.writeErrorsFile(inDirectory, kraftwerkExecutionContext, fileUtilsInterface);
		
		return ResponseEntity.ok(inDirectoryParam+ " - "+dataMode);

	}
	
	

	@PutMapping(value = "/multimodalProcessing")
	@Operation(operationId = "multimodalProcessing", summary = "${summary.multimodalProcessing}", description = "${description.multimodalProcessing}")
	public ResponseEntity<String> multimodalProcessing(
			@Parameter(description = "${param.inDirectory}", required = true, example = INDIRECTORY_EXAMPLE) @RequestBody String inDirectoryParam
			) throws KraftwerkException {
		FileUtilsInterface fileUtilsInterface = getFileUtilsInterface();

		//Read data in JSON file
		Path inDirectory;
		try {
			inDirectory = controlInputSequence.getInDirectory(inDirectoryParam);
		} catch (KraftwerkException e) {
			return ResponseEntity.status(e.getStatus()).body(e.getMessage());
		}
		UserInputsFile userInputsFile;
		try {
			userInputsFile = controlInputSequence.getUserInputs(inDirectory, fileUtilsInterface);
		} catch (KraftwerkException e) {
			return ResponseEntity.status(e.getStatus()).body(e.getMessage());
		}
		KraftwerkExecutionContext kraftwerkExecutionContext = new KraftwerkExecutionContext();


		VtlReaderWriterSequence vtlReaderWriterSequence = new VtlReaderWriterSequence(fileUtilsInterface);

		//Test
		VtlBindings vtlBindings = new VtlBindings();
		for (String dataMode : userInputsFile.getModeInputsMap().keySet()) {
			vtlReaderWriterSequence.readDataset(FileUtilsInterface.transformToTemp(inDirectory).toString(),dataMode, StepEnum.UNIMODAL_PROCESSING, vtlBindings);
		}

		Map<String, MetadataModel> metadataModelMap = MetadataUtils.getMetadata(userInputsFile.getModeInputsMap(), fileUtilsInterface);

		//Process
		MultimodalSequence multimodalSequence = new MultimodalSequence();
		multimodalSequence.multimodalProcessing(userInputsFile, vtlBindings, kraftwerkExecutionContext, metadataModelMap, fileUtilsInterface);

		//Write technical fils
		for (String datasetName : vtlBindings.getDatasetNames()) {
			vtlReaderWriterSequence.writeTempBindings(inDirectory, datasetName, vtlBindings, StepEnum.MULTIMODAL_PROCESSING);
		}
		TextFileWriter.writeErrorsFile(inDirectory, kraftwerkExecutionContext, fileUtilsInterface);
		
		return ResponseEntity.ok(inDirectoryParam);

	}
	

	@PutMapping(value = "/writeOutputFiles")
	@Operation(operationId = "writeOutputFiles", summary = "${summary.writeOutputFiles}", description = "${description.writeOutputFiles}")
	public ResponseEntity<String> writeOutputFiles(
			@Parameter(description = "${param.inDirectory}", required = true, example = INDIRECTORY_EXAMPLE) @RequestBody  String inDirectoryParam
			) throws KraftwerkException, SQLException {
		FileUtilsInterface fileUtilsInterface = getFileUtilsInterface();

		Path inDirectory;
		try {
			inDirectory = controlInputSequence.getInDirectory(inDirectoryParam);
		} catch (KraftwerkException e) {
			return ResponseEntity.status(e.getStatus()).body(e.getMessage());
		}
		VtlBindings vtlBindings = new VtlBindings();
		KraftwerkExecutionContext kraftwerkExecutionContext = new KraftwerkExecutionContext();

		// Read all bindings necessary to produce output
		String path = FileUtilsInterface.transformToTemp(inDirectory).toString();
		List<String> fileNames = fileUtilsInterface.listFileNames(path);
		fileNames = fileNames.stream().filter(name -> name.endsWith(StepEnum.MULTIMODAL_PROCESSING.getStepLabel()+JSON)).toList();
		for (String name : fileNames){
			String pathBindings = path + File.separator + name;
			String bindingName =  name.substring(0, name.indexOf("_"+StepEnum.MULTIMODAL_PROCESSING.getStepLabel()));
			VtlReaderWriterSequence vtlReaderSequence = new VtlReaderWriterSequence(fileUtilsInterface);
			vtlReaderSequence.readDataset(pathBindings, bindingName, vtlBindings);
		}
		WriterSequence writerSequence = new WriterSequence();
		UserInputsFile userInputsFile;
		try {
			userInputsFile = controlInputSequence.getUserInputs(inDirectory, fileUtilsInterface);
		} catch (KraftwerkException e) {
			return ResponseEntity.status(e.getStatus()).body(e.getMessage());
		}
		Map<String, MetadataModel> metadataModelMap = MetadataUtils.getMetadata(userInputsFile.getModeInputsMap(), fileUtilsInterface);
		try (Statement database = SqlUtils.openConnection().createStatement()) {
			writerSequence.writeOutputFiles(inDirectory, vtlBindings, userInputsFile.getModeInputsMap(), metadataModelMap, kraftwerkExecutionContext, database, fileUtilsInterface);
		}
		return ResponseEntity.ok(inDirectoryParam);

	}

	private @NotNull FileUtilsInterface getFileUtilsInterface() {
		FileUtilsInterface fileUtilsInterface;
		if(Boolean.TRUE.equals(useMinio)){
			fileUtilsInterface = new MinioImpl(minioClient, minioConfig.getBucketName());
		}else{
			fileUtilsInterface = new FileSystemImpl(defaultDirectory);
		}
		return fileUtilsInterface;
	}


	@PutMapping(value = "/archive")
	@Operation(operationId = "archive", summary = "${summary.archive}", description = "${description.archive}")
	public ResponseEntity<String> archiveService(
			@Parameter(description = "${param.inDirectory}", required = true, example = INDIRECTORY_EXAMPLE) @RequestBody String inDirectoryParam) {
		FileUtilsInterface fileUtilsInterface = getFileUtilsInterface();

		return archive(inDirectoryParam, fileUtilsInterface);
	}
}