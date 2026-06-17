package fr.insee.kraftwerk.api.services;

import fr.insee.kraftwerk.api.client.GenesisClient;
import fr.insee.kraftwerk.api.configuration.ConfigProperties;
import fr.insee.kraftwerk.api.configuration.MinioConfig;
import fr.insee.kraftwerk.api.dto.BatchResponseDto;
import fr.insee.kraftwerk.api.dto.ExportCheckResultDto;
import fr.insee.kraftwerk.api.exceptions.BatchExecutionFailedException;
import fr.insee.kraftwerk.api.process.MainProcessing;
import fr.insee.kraftwerk.api.process.MainProcessingGenesisNew;
import fr.insee.kraftwerk.api.services.async.InMemoryExportJobStore;
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
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
public class BatchExportService extends KraftwerkService {

    private final ConfigProperties configProperties;
    private final MinioConfig minioConfig;
    private final InMemoryExportJobStore exportJobStore;
    private final OutputZipService outputZipService;

    private static final String BATCH_RESPONSE_LOG = "Batch response: {}";

    private MinioClient minioClient;
    private boolean useMinio;

    @Autowired
    public BatchExportService(
            ConfigProperties configProperties,
            MinioConfig minioConfig,
            InMemoryExportJobStore exportJobStore,
            OutputZipService outputZipService
    ) {
        super(configProperties, minioConfig);
        this.configProperties = configProperties;
        this.minioConfig = minioConfig;
        this.exportJobStore = exportJobStore;
        this.outputZipService = outputZipService;

        this.useMinio = false;
        if (minioConfig != null && minioConfig.isEnable()) {
            this.minioClient = MinioClient.builder()
                    .endpoint(minioConfig.getEndpoint())
                    .credentials(minioConfig.getAccessKey(), minioConfig.getSecretKey())
                    .build();
            this.useMinio = true;
        }
    }

    //LEGACY
    public BatchResponseDto mainServiceBatch(
            String inDirectoryParam,
            boolean archiveAtEnd,
            boolean withEncryption,
            boolean addStates
    ) {
        return runLegacyBatchExport(
                inDirectoryParam,
                archiveAtEnd,
                withEncryption,
                addStates,
                false,
                true);
    }

    public BatchResponseDto mainFileByFileBatch(
            String inDirectoryParam,
            boolean archiveAtEnd,
            boolean withEncryption,
            boolean addStates
    ) {
        return runLegacyBatchExport(
                inDirectoryParam,
                archiveAtEnd,
                withEncryption,
                addStates,
                true,
                true
        );
    }

    public BatchResponseDto mainLunaticOnlyBatch(
            String inDirectoryParam,
            boolean archiveAtEnd,
            boolean withEncryption,
            boolean addStates
    ) {
        return runLegacyBatchExport(
                inDirectoryParam,
                archiveAtEnd,
                withEncryption,
                addStates,
                false,
                false
        );
    }


    private BatchResponseDto runLegacyBatchExport(
            String inDirectoryParam,
            boolean archiveAtEnd,
            boolean withEncryption,
            boolean addStates,
            boolean fileByFile,
            boolean withDDI
    ) {
        String jobId = UUID.randomUUID().toString();

        FileUtilsInterface fileUtilsInterface = getFileUtilsInterface();
        MainProcessing mp = getMainProcessing(
                inDirectoryParam,
                fileByFile,
                withDDI,
                withEncryption,
                fileUtilsInterface,
                addStates
        );

        LocalDateTime executionDateTime =
                mp.getKraftwerkExecutionContext().getExecutionDateTime();

        Path outputPath = buildBatchOutDirectoryForMain(inDirectoryParam, executionDateTime);
        exportJobStore.start(jobId);

        try {
            mp.runMain();

            outputZipService.encryptAndArchiveOutputs(
                    mp.getKraftwerkExecutionContext(),
                    fileUtilsInterface
            );

            List<String> errors = new ArrayList<>();

            if (mp.getKraftwerkExecutionContext().getErrors() != null) {
                mp.getKraftwerkExecutionContext()
                        .getErrors()
                        .forEach(error -> errors.add(error.toString()));
            }

            ExportCheckResultDto result = new ExportCheckResultDto(
                    inDirectoryParam,
                    0L
            );

            exportJobStore.complete(jobId, result, errors);

            if (archiveAtEnd) {
                archive(inDirectoryParam, fileUtilsInterface);
            }

            BatchResponseDto response = new BatchResponseDto(jobId,normalizePath(outputPath));
            log.info(BATCH_RESPONSE_LOG, response);
            return response;

        } catch (KraftwerkException e) {
            log.error("Batch legacy export failed for input directory {}", inDirectoryParam, e);
            exportJobStore.fail(jobId, e);
            throw new BatchExecutionFailedException(e);
        }
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

    private Path buildBatchOutDirectoryForMain(String inDirectoryParam, LocalDateTime executionDateTime) {
        return FileUtilsInterface.transformToOut(Paths.get(inDirectoryParam), executionDateTime);
    }

    //GENESIS

    public BatchResponseDto mainGenesisByQuestionnaireIdBatch(
            String questionnaireModelId,
            Mode dataMode,
            int batchSize,
            boolean withEncryption,
            boolean addStates
    ) {
        return runGenesisBatchExport(questionnaireModelId, dataMode, batchSize, withEncryption, addStates, true);
    }

    public BatchResponseDto mainGenesisLunaticOnlyByQuestionnaireBatch(
            String questionnaireModelId,
            Mode dataMode,
            int batchSize,
            boolean withEncryption,
            boolean addStates
    ) {
        return runGenesisBatchExport(
                questionnaireModelId,
                dataMode,
                batchSize,
                withEncryption,
                addStates,
                false
        );
    }

    private BatchResponseDto runGenesisBatchExport(
            String questionnaireModelId,
            Mode dataMode,
            int batchSize,
            boolean withEncryption,
            boolean addStates,
            boolean withDDI
    ) {
        FileUtilsInterface fileUtilsInterface = getFileUtilsInterface();
        String jobId = UUID.randomUUID().toString();

        MainProcessingGenesisNew mpGenesis = getMainProcessingGenesisByQuestionnaire(
                withDDI,
                withEncryption,
                fileUtilsInterface,
                addStates
        );

        LocalDateTime executionDateTime =
                mpGenesis.getKraftwerkExecutionContext().getExecutionDateTime();

        Path outputPath = buildBatchOutDirectoryForGenesis(questionnaireModelId, executionDateTime);

        exportJobStore.start(jobId);

        try {
            mpGenesis.runMain(questionnaireModelId, batchSize, dataMode);

            outputZipService.encryptAndArchiveOutputs(
                    mpGenesis.getKraftwerkExecutionContext(),
                    fileUtilsInterface
            );

            List<String> errors = new ArrayList<>();

            if (mpGenesis.getKraftwerkExecutionContext().getErrors() != null) {
                mpGenesis.getKraftwerkExecutionContext()
                        .getErrors()
                        .forEach(error -> errors.add(error.toString()));
            }

            ExportCheckResultDto result = new ExportCheckResultDto(
                    questionnaireModelId,
                    0L
            );

            exportJobStore.complete(jobId, result, errors);

            BatchResponseDto response = new BatchResponseDto(jobId, normalizePath(outputPath));
            log.info(BATCH_RESPONSE_LOG, response);
            return response;

        } catch (KraftwerkException | IOException e) {
            exportJobStore.fail(jobId, e);
            throw new BatchExecutionFailedException(e);
        }
    }

    public ResponseEntity<Object> jsonExtractionBatch(
            String collectionInstrumentId,
            Mode dataMode,
            int batchSize,
            Instant since,
            boolean withEncryption,
            boolean addStates
    ) {
        FileUtilsInterface fileUtilsInterface = getFileUtilsInterface();
        boolean withDDI = true;

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
            log.info(BATCH_RESPONSE_LOG, response);
            return ResponseEntity.ok(response);
        } catch (KraftwerkException e) {
            return ResponseEntity.status(e.getStatus()).body(e.getMessage());
        } catch (IOException e) {
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
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

    private Path buildBatchOutDirectoryForGenesis(String collectionInstrumentId, LocalDateTime executionDateTime) {
        return Paths.get(
                "out",
                collectionInstrumentId,
                executionDateTime.format(DateTimeFormatter.ofPattern(Constants.OUTPUT_FOLDER_DATETIME_PATTERN))
        );
    }

    //UTILS
    private String normalizePath(Path path) {
        return path.toString().replace("\\", "/");
    }

    FileUtilsInterface getFileUtilsInterface() {
        if (useMinio) {
            return new MinioImpl(minioClient, minioConfig.getBucketName());
        }
        return new FileSystemImpl(configProperties.getDefaultDirectory());
    }
}
