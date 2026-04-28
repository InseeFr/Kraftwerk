package fr.insee.kraftwerk.api.services;

import fr.insee.kraftwerk.api.client.GenesisClient;
import fr.insee.kraftwerk.api.configuration.ConfigProperties;
import fr.insee.kraftwerk.api.configuration.MinioConfig;
import fr.insee.kraftwerk.api.dto.BatchResponseDto;
import fr.insee.kraftwerk.api.process.MainProcessing;
import fr.insee.kraftwerk.api.process.MainProcessingGenesisNew;
import fr.insee.kraftwerk.api.services.async.InMemoryExportJobStore;
import fr.insee.kraftwerk.api.services.async.MainAsyncService;
import fr.insee.kraftwerk.core.Constants;
import fr.insee.kraftwerk.core.data.model.Mode;
import fr.insee.kraftwerk.core.exceptions.KraftwerkException;
import fr.insee.kraftwerk.core.utils.KraftwerkExecutionContext;
import fr.insee.kraftwerk.core.utils.files.FileSystemImpl;
import fr.insee.kraftwerk.core.utils.files.FileUtilsInterface;
import fr.insee.kraftwerk.core.utils.files.MinioImpl;
import io.minio.MinioClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Service
@Slf4j
public class BatchExportService extends KraftwerkService {

    private final MainAsyncService mainAsyncService;
    private final ConfigProperties configProperties;
    private final MinioConfig minioConfig;
    private final InMemoryExportJobStore exportJobStore;

    private MinioClient minioClient;
    private boolean useMinio;

    @Autowired
    public BatchExportService(
            MainAsyncService mainAsyncService,
            ConfigProperties configProperties,
            MinioConfig minioConfig,
            InMemoryExportJobStore exportJobStore
    ) {
        super(configProperties, minioConfig);
        this.mainAsyncService = mainAsyncService;
        this.configProperties = configProperties;
        this.minioConfig = minioConfig;
        this.exportJobStore = exportJobStore;

        this.useMinio = false;
        if (minioConfig != null && minioConfig.isEnable()) {
            this.minioClient = MinioClient.builder()
                    .endpoint(minioConfig.getEndpoint())
                    .credentials(minioConfig.getAccessKey(), minioConfig.getSecretKey())
                    .build();
            this.useMinio = true;
        }
    }


    private String normalizePath(Path path) {
        return path.toString().replace("\\", "/");
    }

    FileUtilsInterface getFileUtilsInterface() {
        if (useMinio) {
            return new MinioImpl(minioClient, minioConfig.getBucketName());
        }
        return new FileSystemImpl(configProperties.getDefaultDirectory());
    }

    private MainProcessing getMainProcessing(
            String inDirectoryParam,
            boolean fileByFile,
            boolean withDDI,
            boolean withEncryption,
            FileUtilsInterface fileUtilsInterface,
            boolean addStates
    ) {
        KraftwerkExecutionContext kraftwerkExecutionContext = new KraftwerkExecutionContext(
                inDirectoryParam,
                fileByFile,
                withDDI,
                withEncryption,
                limitSize,
                addStates
        );

        return new MainProcessing(kraftwerkExecutionContext, defaultDirectory, fileUtilsInterface);
    }

    MainProcessingGenesisNew getMainProcessingGenesisByQuestionnaire(
            boolean withDDI,
            boolean withEncryption,
            FileUtilsInterface fileUtilsInterface,
            boolean addStates
    ) {
        KraftwerkExecutionContext kraftwerkExecutionContext = new KraftwerkExecutionContext(
                null,
                false,
                withDDI,
                withEncryption,
                limitSize,
                addStates
        );

        return new MainProcessingGenesisNew(
                configProperties,
                new GenesisClient(new RestTemplateBuilder(), configProperties),
                fileUtilsInterface,
                kraftwerkExecutionContext
        );
    }

    private Path buildBatchOutDirectoryForMain(String inDirectoryParam, LocalDateTime executionDateTime) {
        return FileUtilsInterface.transformToOut(Paths.get(inDirectoryParam), executionDateTime);
    }

    private Path buildBatchOutDirectoryForGenesis(String collectionInstrumentId, LocalDateTime executionDateTime) {
        return Paths.get(
                "out",
                collectionInstrumentId,
                executionDateTime.format(DateTimeFormatter.ofPattern(Constants.OUTPUT_FOLDER_DATETIME_PATTERN))
        );
    }

    public BatchResponseDto mainServiceBatch(
            String inDirectoryParam,
            boolean archiveAtEnd,
            boolean withEncryption,
            boolean addStates
    ) {
        boolean fileByFile = false;
        boolean withDDI = true;

        FileUtilsInterface fileUtilsInterface = getFileUtilsInterface();
        MainProcessing mp = getMainProcessing(
                inDirectoryParam,
                fileByFile,
                withDDI,
                withEncryption,
                fileUtilsInterface,
                addStates
        );

        String jobId = UUID.randomUUID().toString();
        LocalDateTime executionDateTime = mp.getKraftwerkExecutionContext().getExecutionDateTime();

        Path outputPath = buildBatchOutDirectoryForMain(inDirectoryParam, executionDateTime);

        mainAsyncService.runWithoutGenesis(
                jobId,
                fileUtilsInterface,
                mp,
                inDirectoryParam,
                archiveAtEnd,
                fileByFile,
                withDDI,
                withEncryption
        );

        BatchResponseDto response = new BatchResponseDto(jobId, normalizePath(outputPath));
        log.info("Batch response: {}", response);
        return response;
    }

    public BatchResponseDto mainFileByFileBatch(
            String inDirectoryParam,
            boolean archiveAtEnd,
            boolean withEncryption,
            boolean addStates
    ) {
        boolean fileByFile = true;
        boolean withDDI = true;

        FileUtilsInterface fileUtilsInterface = getFileUtilsInterface();
        MainProcessing mp = getMainProcessing(
                inDirectoryParam,
                fileByFile,
                withDDI,
                withEncryption,
                fileUtilsInterface,
                addStates
        );

        String jobId = UUID.randomUUID().toString();
        LocalDateTime executionDateTime = mp.getKraftwerkExecutionContext().getExecutionDateTime();

        Path outputPath = buildBatchOutDirectoryForMain(inDirectoryParam, executionDateTime);

        mainAsyncService.runWithoutGenesis(
                jobId,
                fileUtilsInterface,
                mp,
                inDirectoryParam,
                archiveAtEnd,
                fileByFile,
                withDDI,
                withEncryption
        );

        BatchResponseDto response = new BatchResponseDto(jobId, normalizePath(outputPath));
        log.info("Batch response: {}", response);
        return response;
    }

    public BatchResponseDto mainLunaticOnlyBatch(
            String inDirectoryParam,
            boolean archiveAtEnd,
            boolean withEncryption,
            boolean addStates
    ) {
        boolean fileByFile = false;
        boolean withDDI = false;

        FileUtilsInterface fileUtilsInterface = getFileUtilsInterface();
        MainProcessing mp = getMainProcessing(
                inDirectoryParam,
                fileByFile,
                withDDI,
                withEncryption,
                fileUtilsInterface,
                addStates
        );

        String jobId = UUID.randomUUID().toString();
        LocalDateTime executionDateTime = mp.getKraftwerkExecutionContext().getExecutionDateTime();

        Path outputPath = buildBatchOutDirectoryForMain(inDirectoryParam, executionDateTime);

        mainAsyncService.runWithoutGenesis(
                jobId,
                fileUtilsInterface,
                mp,
                inDirectoryParam,
                archiveAtEnd,
                fileByFile,
                withDDI,
                withEncryption
        );

        BatchResponseDto response = new BatchResponseDto(jobId, normalizePath(outputPath));
        log.info("Batch response: {}", response);
        return response;    }

    public BatchResponseDto mainGenesisByQuestionnaireIdBatch(
            String questionnaireModelId,
            Mode dataMode,
            int batchSize,
            boolean withEncryption,
            boolean addStates
    ) {
        boolean withDDI = true;
        FileUtilsInterface fileUtilsInterface = getFileUtilsInterface();
        String jobId = UUID.randomUUID().toString();

        MainProcessingGenesisNew mpGenesis = getMainProcessingGenesisByQuestionnaire(
                withDDI,
                withEncryption,
                fileUtilsInterface,
                addStates
        );

        LocalDateTime executionDateTime = mpGenesis.getKraftwerkExecutionContext().getExecutionDateTime();

        Path outputPath = buildBatchOutDirectoryForGenesis(questionnaireModelId, executionDateTime);

        exportJobStore.start(jobId);
        mainAsyncService.runWithGenesisByQuestionnaire(
                jobId,
                fileUtilsInterface,
                mpGenesis,
                questionnaireModelId,
                withDDI,
                withEncryption,
                batchSize,
                dataMode
        );

        BatchResponseDto response = new BatchResponseDto(jobId, normalizePath(outputPath));
        log.info("Batch response: {}", response);
        return response;
    }

    public BatchResponseDto mainGenesisLunaticOnlyByQuestionnaireBatch(
            String questionnaireModelId,
            Mode dataMode,
            int batchSize,
            boolean withEncryption,
            boolean addStates
    ) {
        boolean withDDI = false;
        FileUtilsInterface fileUtilsInterface = getFileUtilsInterface();
        String jobId = UUID.randomUUID().toString();

        MainProcessingGenesisNew mpGenesis = getMainProcessingGenesisByQuestionnaire(
                withDDI,
                withEncryption,
                fileUtilsInterface,
                addStates
        );

        LocalDateTime executionDateTime = mpGenesis.getKraftwerkExecutionContext().getExecutionDateTime();

        Path outputPath = buildBatchOutDirectoryForGenesis(questionnaireModelId, executionDateTime);

        exportJobStore.start(jobId);
        mainAsyncService.runWithGenesisByQuestionnaire(
                jobId,
                fileUtilsInterface,
                mpGenesis,
                questionnaireModelId,
                withDDI,
                withEncryption,
                batchSize,
                dataMode
        );

        BatchResponseDto response = new BatchResponseDto(jobId, normalizePath(outputPath));
        log.info("Batch response: {}", response);
        return response;
    }

    public ResponseEntity<Object> jsonExtractionBatch(
            String collectionInstrumentId,
            Mode dataMode,
            int batchSize,
            LocalDateTime since,
            boolean addStates
    ) {
        FileUtilsInterface fileUtilsInterface = getFileUtilsInterface();
        boolean withDDI = true;
        boolean withEncryption = false;

        MainProcessingGenesisNew mpGenesis = getMainProcessingGenesisByQuestionnaire(
                withDDI,
                withEncryption,
                fileUtilsInterface,
                addStates
        );

        LocalDateTime executionDateTime = mpGenesis.getKraftwerkExecutionContext().getExecutionDateTime();
        Path outputPath = buildBatchOutDirectoryForGenesis(collectionInstrumentId, executionDateTime);

        try {
            mpGenesis.runMainJson(collectionInstrumentId, batchSize, dataMode, since);
            BatchResponseDto response = new BatchResponseDto("",normalizePath(outputPath));
            log.info("Batch response: {}", response);
            return ResponseEntity.ok(response);
        } catch (KraftwerkException e) {
            return ResponseEntity.status(e.getStatus()).body(e.getMessage());
        } catch (IOException e) {
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }
}
