package fr.insee.kraftwerk.api.services;


import fr.insee.kraftwerk.api.client.GenesisClient;
import fr.insee.kraftwerk.api.configuration.ConfigProperties;
import fr.insee.kraftwerk.api.configuration.MinioConfig;
import fr.insee.kraftwerk.api.configuration.VaultConfig;
import fr.insee.kraftwerk.api.process.MainProcessing;
import fr.insee.kraftwerk.api.process.MainProcessingGenesisLegacy;
import fr.insee.kraftwerk.api.process.MainProcessingGenesisNew;
import fr.insee.kraftwerk.core.data.model.Mode;
import fr.insee.kraftwerk.core.exceptions.KraftwerkException;
import fr.insee.kraftwerk.core.utils.KraftwerkExecutionContext;
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
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
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
	VaultConfig vaultConfig;
	boolean useMinio;


	@Autowired
	public MainService(ConfigProperties configProperties, MinioConfig minioConfig, VaultConfig vaultConfig, Environment env) {
        super(configProperties, minioConfig);
        this.configProperties = configProperties;
		this.minioConfig = minioConfig;
		this.vaultConfig = vaultConfig;

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
			@Parameter(description = "${param.withEncryption}") @RequestParam(value = "withEncryption", defaultValue = "false") boolean withEncryption
			) {
		boolean fileByFile = false;
		boolean withDDI = true;
		return runWithoutGenesis(inDirectoryParam, archiveAtEnd, fileByFile, withDDI, withEncryption);
	}

	@PutMapping(value = "/main/file-by-file")
	@Operation(operationId = "main", summary = "${summary.fileByFile}", description = "${description.fileByFile}")
	public ResponseEntity<String> mainFileByFile(
			@Parameter(description = "${param.inDirectory}", required = true, example = INDIRECTORY_EXAMPLE) @RequestBody String inDirectoryParam,
			@Parameter(description = "${param.archiveAtEnd}", required = false) @RequestParam(defaultValue = "false") boolean archiveAtEnd,
			@Parameter(description = "${param.withEncryption}") @RequestParam(value = "withEncryption", defaultValue = "false") boolean withEncryption
	) {
		boolean fileByFile = true;
		boolean withDDI = true;
		return runWithoutGenesis(inDirectoryParam, archiveAtEnd, fileByFile, withDDI, withEncryption);
	}

	@PutMapping(value = "/main/lunatic-only")
	@Operation(operationId = "mainLunaticOnly", summary = "${summary.mainLunaticOnly}", description = "${description.mainLunaticOnly}")
	public ResponseEntity<String> mainLunaticOnly(
			@Parameter(description = "${param.inDirectory}", required = true, example = INDIRECTORY_EXAMPLE) @RequestBody String inDirectoryParam,
			@Parameter(description = "${param.archiveAtEnd}", required = false) @RequestParam(defaultValue = "false") boolean archiveAtEnd,
			@Parameter(description = "${param.withEncryption}") @RequestParam(value = "withEncryption", defaultValue = "false") boolean withEncryption
	) {
		boolean withDDI = false;
		boolean fileByFile = false;
		return runWithoutGenesis(inDirectoryParam, archiveAtEnd, fileByFile, withDDI, withEncryption);
	}

	/**
	 * @deprecated
	 * We decided to switch from campaignId to questionnaireModelId to select data that are extracted.
	 * One of the reasons is the possibility to face surveys that have multiple questionnaires for one campaign.
	 * Since 3.4.1
	 */
	@Deprecated (since = "3.4.1", forRemoval = true)
	@PutMapping(value = "/main/genesis")
	@Operation(operationId = "mainGenesis", summary = "${summary.mainGenesis.deprecated}", description = "${description.mainGenesis.deprecated}")
	public ResponseEntity<String> mainGenesis(
			@Parameter(description = "${param.campaignId}", required = true, example = INDIRECTORY_EXAMPLE) @RequestBody String campaignId,
			@Parameter(description = "${param.batchSize}") @RequestParam(value = "batchSize", defaultValue = "1000") int batchSize,
			@Parameter(description = "${param.withEncryption}") @RequestParam(value = "withEncryption", defaultValue = "false") boolean withEncryption) {
		boolean withDDI = true;
		return runWithGenesis(campaignId, withDDI, withEncryption, batchSize);
	}

	@PutMapping(value = "/main/genesis/by-questionnaire")
	@Operation(operationId = "mainGenesisByQuestionnaireId", summary = "${summary.mainGenesis}", description = "${description.mainGenesis}")
	public ResponseEntity<String> mainGenesisByQuestionnaireId(
			@Parameter(description = "${param.questionnaireModelId}") @RequestParam(required = true) String questionnaireModelId,
			@Parameter(description = "${param.dataMode}") @RequestParam(required = false) Mode dataMode,
			@Parameter(description = "${param.batchSize}") @RequestParam(value = "batchSize", defaultValue = "1000") int batchSize,
			@Parameter(description = "${param.withEncryption}") @RequestParam(value = "withEncryption", defaultValue = "false") boolean withEncryption) {
		boolean withDDI = true;
		return runWithGenesisByQuestionnaire(questionnaireModelId, withDDI, withEncryption, batchSize, dataMode);
	}

	/**
	 * @deprecated
	 * We decided to switch from campaignId to questionnaireModelId to select data that are extracted.
	 * One of the reasons is the possibility to face surveys that have multiple questionnaires for one campaign.
	 * Since 3.4.1
	 */
	@Deprecated (since = "3.4.1", forRemoval = true)
	@PutMapping(value = "/main/genesis/lunatic-only")
	@Operation(operationId = "mainGenesisLunaticOnly", summary = "${summary.mainGenesis.lunatic.deprecated}", description = "${description.mainGenesis.lunatic.deprecated}")
	public ResponseEntity<String> mainGenesisLunaticOnly(
			@Parameter(description = "${param.campaignId}", required = true, example = INDIRECTORY_EXAMPLE) @RequestBody String campaignId,
			@Parameter(description = "${param.batchSize}") @RequestParam(value = "batchSize", defaultValue = "1000") int batchSize,
			@Parameter(description = "${param.withEncryption}") @RequestParam(value = "withEncryption", defaultValue = "false") boolean withEncryption
	) {
		boolean withDDI = false;
		return runWithGenesis(campaignId, withDDI, withEncryption, batchSize);
	}

	@PutMapping(value = "/main/genesis/by-questionnaire/lunatic-only")
	@Operation(operationId = "mainGenesisLunaticOnlyByQuestionnaire", summary = "${summary.mainGenesis}", description = "${description.mainGenesis}")
	public ResponseEntity<String> mainGenesisLunaticOnlyByQuestionnaire(
			@Parameter(description = "${param.questionnaireModelId}") @RequestParam String questionnaireModelId,
			@Parameter(description = "${param.dataMode}") @RequestParam(required = false) Mode dataMode,
			@Parameter(description = "${param.batchSize}") @RequestParam(value = "batchSize", defaultValue = "1000") int batchSize,
			@Parameter(description = "${param.withEncryption}") @RequestParam(value = "withEncryption", defaultValue = "false") boolean withEncryption
	) {
		boolean withDDI = false;
		return runWithGenesisByQuestionnaire(questionnaireModelId, withDDI, withEncryption, batchSize, dataMode);
	}

	//WIP : poc one interrogation
	//Objective : give the json with all the data
	@GetMapping(value ="/json")
	@Operation(operationId = "jsonExtraction", summary = "", description ="")
	public ResponseEntity<Object> jsonExtraction(
			@Parameter(description = "${param.questionnaireModelId}", required = true, example = INDIRECTORY_EXAMPLE) @RequestParam String questionnaireModelId,
			@Parameter(description = "${param.dataMode}") @RequestParam(required = false) Mode dataMode,
			@Parameter(description = "${param.batchSize}") @RequestParam(value = "batchSize", defaultValue = "1000") int batchSize
	){
		FileUtilsInterface fileUtilsInterface = getFileUtilsInterface();
		boolean withDDI = true;
		boolean withEncryption = false;

		MainProcessingGenesisNew mpGenesis = getMainProcessingGenesisByQuestionnaire(withDDI, withEncryption, fileUtilsInterface);
		try {
			mpGenesis.runMainJson(questionnaireModelId, batchSize, dataMode);
			log.info("Data extracted");
		} catch (KraftwerkException e) {
			return ResponseEntity.status(e.getStatus()).body(e.getMessage());
		} catch (IOException e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
		}
		return ResponseEntity.ok(String.format("Data extracted for questionnaireModelId %s",questionnaireModelId));
	}

	@NotNull
	private ResponseEntity<String> runWithoutGenesis(String inDirectoryParam, boolean archiveAtEnd, boolean fileByFile, boolean withDDI, boolean withEncryption) {
		FileUtilsInterface fileUtilsInterface = getFileUtilsInterface();

		MainProcessing mp = getMainProcessing(inDirectoryParam, fileByFile, withDDI, withEncryption, fileUtilsInterface);
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
	private ResponseEntity<String> runWithGenesis(String campaignId, boolean withDDI, boolean withEncryption, int batchSize) {
		FileUtilsInterface fileUtilsInterface = getFileUtilsInterface();

		MainProcessingGenesisLegacy mpGenesis = getMainProcessingGenesis(withDDI, withEncryption, fileUtilsInterface);

		try {
			mpGenesis.runMain(campaignId, batchSize);
		} catch (KraftwerkException e) {
			return ResponseEntity.status(e.getStatus()).body(e.getMessage());
		} catch (IOException e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
		}
		return ResponseEntity.ok(campaignId);
	}

	@NotNull
	private ResponseEntity<String> runWithGenesisByQuestionnaire(String questionnaireModelId,  boolean withDDI, boolean withEncryption, int batchSize, Mode dataMode) {
		FileUtilsInterface fileUtilsInterface = getFileUtilsInterface();

		MainProcessingGenesisNew mpGenesis = getMainProcessingGenesisByQuestionnaire(withDDI, withEncryption, fileUtilsInterface);

		try {
			mpGenesis.runMain(questionnaireModelId, batchSize, dataMode);
		} catch (KraftwerkException e) {
			return ResponseEntity.status(e.getStatus()).body(e.getMessage());
		} catch (IOException e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
		}
		return ResponseEntity.ok(questionnaireModelId);
	}




	@NotNull
	MainProcessingGenesisLegacy getMainProcessingGenesis(boolean withDDI, boolean withEncryption, FileUtilsInterface fileUtilsInterface) {

		KraftwerkExecutionContext kraftwerkExecutionContext = new KraftwerkExecutionContext(
				null,
				false,
				withDDI,
				withEncryption,
				limitSize
		);

		return new MainProcessingGenesisLegacy(
				configProperties,
				new GenesisClient(new RestTemplateBuilder(), configProperties),
				fileUtilsInterface,
				kraftwerkExecutionContext
		);
	}

	@NotNull
	MainProcessingGenesisNew getMainProcessingGenesisByQuestionnaire(boolean withDDI, boolean withEncryption, FileUtilsInterface fileUtilsInterface) {

		KraftwerkExecutionContext kraftwerkExecutionContext = new KraftwerkExecutionContext(
				null,
				false,
				withDDI,
				withEncryption,
				limitSize
		);

		return new MainProcessingGenesisNew(
				configProperties,
				new GenesisClient(new RestTemplateBuilder(), configProperties),
				fileUtilsInterface,
				kraftwerkExecutionContext
		);
	}


	@NotNull MainProcessing getMainProcessing(String inDirectoryParam, boolean fileByFile, boolean withDDI, boolean withEncryption, FileUtilsInterface fileUtilsInterface) {
		KraftwerkExecutionContext kraftwerkExecutionContext = new KraftwerkExecutionContext(
				inDirectoryParam,
				fileByFile,
				withDDI,
				withEncryption,
				limitSize
		);

		return new MainProcessing(kraftwerkExecutionContext, defaultDirectory, fileUtilsInterface);
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