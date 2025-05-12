package fr.insee.kraftwerk.api.services;


import fr.insee.kraftwerk.api.configuration.ConfigProperties;
import fr.insee.kraftwerk.api.configuration.MinioConfig;
import fr.insee.kraftwerk.api.process.MainProcessing;
import fr.insee.kraftwerk.api.process.MainProcessingGenesis;
import fr.insee.kraftwerk.core.exceptions.KraftwerkException;
import fr.insee.kraftwerk.core.utils.files.FileSystemImpl;
import fr.insee.kraftwerk.core.utils.files.FileUtilsInterface;
import fr.insee.kraftwerk.core.utils.files.MinioImpl;
import io.minio.MinioClient;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;


@RestController
@Slf4j
@Tag(name = "${tag.main}")
public class MainService extends KraftwerkService {

	ConfigProperties configProperties;
	MinioClient minioClient;
	boolean useMinio;


	@Autowired
	public MainService(ConfigProperties configProperties, MinioConfig minioConfig) {
        super(configProperties, minioConfig);
        this.configProperties = configProperties;
		this.minioConfig = minioConfig;
		useMinio = false;
		if(minioConfig == null){
			log.warn("Minio config null !");
		}
		if(minioConfig != null && minioConfig.isEnable()){
			minioClient = MinioClient.builder().endpoint(minioConfig.getEndpoint()).credentials(minioConfig.getAccessKey(), minioConfig.getSecretKey()).build();
			useMinio = true;
		}
	}

	@PutMapping(value = "/main")
	@Operation(operationId = "main", summary = "${summary.main}", description = "${description.main}")
	public ResponseEntity<String> mainService(
			@Parameter(description = "${param.inDirectory}", required = true, example = INDIRECTORY_EXAMPLE) @RequestBody String inDirectoryParam,
			@Parameter(description = "${param.archiveAtEnd}", required = false) @RequestParam(defaultValue = "false") boolean archiveAtEnd,
			@Parameter(description = "${param.withAllReportingData}", required = false) @RequestParam(defaultValue = "true") boolean withAllReportingData
			) {
		boolean fileByFile = false;
		boolean withDDI = true;
		return runWithoutGenesis(inDirectoryParam, archiveAtEnd, fileByFile, withAllReportingData, withDDI);
	}

	@PutMapping(value = "/main/file-by-file")
	@Operation(operationId = "main", summary = "${summary.fileByFile}", description = "${description.fileByFile}")
	public ResponseEntity<String> mainFileByFile(
			@Parameter(description = "${param.inDirectory}", required = true, example = INDIRECTORY_EXAMPLE) @RequestBody String inDirectoryParam,
			@Parameter(description = "${param.archiveAtEnd}", required = false) @RequestParam(defaultValue = "false") boolean archiveAtEnd
	) {
		boolean fileByFile = true;
		boolean withAllReportingData = false;
		boolean withDDI = true;
		return runWithoutGenesis(inDirectoryParam, archiveAtEnd, fileByFile, withAllReportingData, withDDI);
	}

	@PutMapping(value = "/main/lunatic-only")
	@Operation(operationId = "mainLunaticOnly", summary = "${summary.mainLunaticOnly}", description = "${description.mainLunaticOnly}")
	public ResponseEntity<String> mainLunaticOnly(
			@Parameter(description = "${param.inDirectory}", required = true, example = INDIRECTORY_EXAMPLE) @RequestBody String inDirectoryParam,
			@Parameter(description = "${param.archiveAtEnd}", required = false) @RequestParam(defaultValue = "false") boolean archiveAtEnd
	) {
		boolean withDDI = false;
		boolean fileByFile = false;
		boolean withAllReportingData = false;
		return runWithoutGenesis(inDirectoryParam, archiveAtEnd, fileByFile, withAllReportingData, withDDI);
	}

	@PutMapping(value = "/main/genesis")
	@Operation(operationId = "mainGenesis", summary = "${summary.mainGenesis}", description = "${description.mainGenesis}")
	public ResponseEntity<String> mainGenesis(
			@Parameter(description = "${param.campaignId}", required = true, example = INDIRECTORY_EXAMPLE) @RequestBody String campaignId,
			@Parameter(description = "${param.batchSize}") @RequestParam(value = "batchSize", defaultValue = "1000") int batchSize) {
		boolean withDDI = true;
		return runWithGenesis(campaignId, withDDI, batchSize);
	}


	//========= OPTIMISATIONS PERFS (START) ==========
	/**
	 * @author Adrien Marchal
	 */
	@PutMapping(value = "/main/genesisV2")
	@Operation(operationId = "mainGenesis", summary = "${summary.mainGenesis}", description = "${description.mainGenesis}")
	public ResponseEntity<String> mainGenesisV2(
			@Parameter(description = "${param.campaignId}", required = true, example = INDIRECTORY_EXAMPLE) @RequestBody String campaignId,
			@Parameter(description = "${param.batchSize}") @RequestParam(value = "batchSize", defaultValue = "1000") int batchSize,
			@Parameter(description = "Workers number") @RequestParam(value = "workersNumbers", defaultValue = "1") int workersNumbers,
			@Parameter(description = "WorkerId") @RequestParam(value = "workerId", defaultValue = "1") int workerId) {
		boolean withDDI = true;
		return runWithGenesisV2(campaignId, withDDI, batchSize, workersNumbers, workerId);
	}
	//========= OPTIMISATIONS PERFS (END) ==========


	@PutMapping(value = "/main/genesis/lunatic-only")
	@Operation(operationId = "mainGenesisLunaticOnly", summary = "${summary.mainGenesis}", description = "${description.mainGenesis}")
	public ResponseEntity<String> mainGenesisLunaticOnly(
			@Parameter(description = "${param.campaignId}", required = true, example = INDIRECTORY_EXAMPLE) @RequestBody String campaignId,
			@Parameter(description = "${param.batchSize}") @RequestParam(value = "batchSize", defaultValue = "1000") int batchSize) {
		boolean withDDI = false;
		return runWithGenesis(campaignId, withDDI, batchSize);
	}

	@NotNull
	private ResponseEntity<String> runWithoutGenesis(String inDirectoryParam, boolean archiveAtEnd, boolean fileByFile, boolean withAllReportingData, boolean withDDI) {
		FileUtilsInterface fileUtilsInterface = getFileUtilsInterface();

		MainProcessing mp = getMainProcessing(inDirectoryParam, fileByFile, withAllReportingData, withDDI, fileUtilsInterface);
		try {
			mp.runMain();
		} catch (KraftwerkException e) {
			return ResponseEntity.status(e.getStatus()).body(e.getMessage());
		}
		/* Step 4.3- 4.4 : Archive */
		if (Boolean.TRUE.equals(archiveAtEnd)) archive(inDirectoryParam, fileUtilsInterface);

		return ResponseEntity.ok(inDirectoryParam);
	}


	@NotNull
	private ResponseEntity<String> runWithGenesis(String campaignId, boolean withDDI, int batchSize) {
		long totalDurationStartTimeStamp = System.currentTimeMillis();
		FileUtilsInterface fileUtilsInterface = getFileUtilsInterface();

		MainProcessingGenesis mpGenesis = getMainProcessingGenesis(withDDI, fileUtilsInterface);

		try {
			mpGenesis.runMain(campaignId, batchSize);
		} catch (KraftwerkException e) {
			return ResponseEntity.status(e.getStatus()).body(e.getMessage());
		} catch (IOException e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
		}
		long totalDurationEndTimeStamp = System.currentTimeMillis();
		long totalDurationDeltaTimeStamp = totalDurationEndTimeStamp - totalDurationStartTimeStamp;
		log.info("=================== TOTAL DURATION (runWithGenesis) =================== : {}", totalDurationDeltaTimeStamp);
		return ResponseEntity.ok(campaignId);
	}


	//========= OPTIMISATIONS PERFS (START) ==========
	/**
	 * @author Adrien Marchal
	 */
	@NotNull
	private ResponseEntity<String> runWithGenesisV2(String campaignId, boolean withDDI, int batchSize, int workersNumbers, int workerId) {
		long totalDurationV2StartTimeStamp = System.currentTimeMillis();
		FileUtilsInterface fileUtilsInterface = getFileUtilsInterface();

		MainProcessingGenesis mpGenesis = getMainProcessingGenesis(withDDI, fileUtilsInterface);

		try {
			mpGenesis.runMainV2(campaignId, batchSize, workersNumbers, workerId);
		} catch (KraftwerkException e) {
			return ResponseEntity.status(e.getStatus()).body(e.getMessage());
		} catch (IOException e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
		}
		long totalDurationV2EndTimeStamp = System.currentTimeMillis();
		long totalDurationV2DeltaTimeStamp = totalDurationV2EndTimeStamp - totalDurationV2StartTimeStamp;
		log.info("=================== TOTAL DURATION (runWithGenesisV2) ================= : {}", totalDurationV2DeltaTimeStamp);
		return ResponseEntity.ok(campaignId);
	}
	//========= OPTIMISATIONS PERFS (END) ==========




	@NotNull MainProcessingGenesis getMainProcessingGenesis(boolean withDDI, FileUtilsInterface fileUtilsInterface) {
		return new MainProcessingGenesis(configProperties, fileUtilsInterface, withDDI);
	}


	@NotNull MainProcessing getMainProcessing(String inDirectoryParam, boolean fileByFile, boolean withAllReportingData, boolean withDDI, FileUtilsInterface fileUtilsInterface) {
		return new MainProcessing(inDirectoryParam, fileByFile, withAllReportingData, withDDI, defaultDirectory, limitSize, fileUtilsInterface);
	}

	@NotNull FileUtilsInterface getFileUtilsInterface() {
		FileUtilsInterface fileUtilsInterface;
		if(Boolean.TRUE.equals(useMinio)){
			fileUtilsInterface = new MinioImpl(minioClient, minioConfig.getBucketName());
		}else{
			fileUtilsInterface = new FileSystemImpl(configProperties.getDefaultDirectory());
		}
		return fileUtilsInterface;
	}

	
}