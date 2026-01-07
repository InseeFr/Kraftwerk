package fr.insee.kraftwerk.api.services.async;


import fr.insee.kraftwerk.api.configuration.ConfigProperties;
import fr.insee.kraftwerk.api.configuration.MinioConfig;
import fr.insee.kraftwerk.api.process.MainProcessing;
import fr.insee.kraftwerk.api.process.MainProcessingGenesisLegacy;
import fr.insee.kraftwerk.api.process.MainProcessingGenesisNew;
import fr.insee.kraftwerk.api.services.KraftwerkService;
import fr.insee.kraftwerk.core.data.model.Mode;
import fr.insee.kraftwerk.core.exceptions.KraftwerkException;
import fr.insee.kraftwerk.core.utils.files.FileUtilsInterface;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.io.IOException;

@Service
@Slf4j
public class MainAsyncService extends KraftwerkService {

	private final InMemoryJobStore jobStore;

	public MainAsyncService(ConfigProperties configProperties, MinioConfig minioConfig, InMemoryJobStore jobStore) {
		super(configProperties, minioConfig);
		this.jobStore = jobStore;
	}

	@GetMapping("/status/{jobId}")
	public ResponseEntity<JobExecution> getStatus(@PathVariable String jobId) {
		return jobStore.get(jobId)
				.map(ResponseEntity::ok)
				.orElse(ResponseEntity.notFound().build());
	}


	@Async("kraftwerkExecutor")
	public void runWithoutGenesis(String jobId, FileUtilsInterface fileUtilsInterface, MainProcessing mp, String inDirectoryParam, boolean archiveAtEnd, boolean fileByFile, boolean withDDI, boolean withEncryption) {
		jobStore.start(jobId);
		try {
			mp.runMain();
			jobStore.success(jobId);

		} catch (KraftwerkException e) {
			log.error(e.getMessage());
			jobStore.fail(jobId, e);
		}
		/* Step 4.3- 4.4 : Archive */
		if (archiveAtEnd) archive(inDirectoryParam, fileUtilsInterface);

	}

	@Async("kraftwerkExecutor")
	@Deprecated(since = "3.4.1", forRemoval = true)
	public void runWithGenesis(String jobId, FileUtilsInterface fileUtilsInterface, MainProcessingGenesisLegacy mpGenesis,String campaignId, boolean withDDI, boolean withEncryption, int batchSize) {
		jobStore.start(jobId);
		try {
			mpGenesis.runMain(campaignId, batchSize);
			jobStore.success(jobId);

		} catch (KraftwerkException e) {
			log.error("KRAFTWERK EXCEPTION for campaign {}: {}", campaignId, e.getMessage());
			jobStore.fail(jobId, e);

		} catch (IOException e) {
			log.error("INTERNAL ERROR for campaign {}: {}",campaignId, e.getMessage());
			jobStore.fail(jobId, e);

		}
	}

	@Async("kraftwerkExecutor")
	public void runWithGenesisByQuestionnaire(String jobId, FileUtilsInterface fileUtilsInterface, MainProcessingGenesisNew mpGenesis,String questionnaireModelId,  boolean withDDI, boolean withEncryption, int batchSize, Mode dataMode) {
		jobStore.start(jobId);
		try {
			mpGenesis.runMain(questionnaireModelId, batchSize, dataMode);
			jobStore.success(jobId);

		} catch (KraftwerkException e) {
			log.error("KRAFTWERK EXCEPTION for questionnaireModelId {}: {}", questionnaireModelId, e.getMessage());
			jobStore.fail(jobId, e);

		} catch (IOException e) {
			log.error("INTERNAL ERROR for questionnaireModelId {}: {}", questionnaireModelId, e.getMessage());
			jobStore.fail(jobId, e);

		}
	}




	
}