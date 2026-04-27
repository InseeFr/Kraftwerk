package fr.insee.kraftwerk.api.batch;

import fr.insee.kraftwerk.api.configuration.ConfigProperties;
import fr.insee.kraftwerk.api.configuration.MinioConfig;
import fr.insee.kraftwerk.api.configuration.VaultConfig;
import fr.insee.kraftwerk.api.dto.BatchResponseDto;
import fr.insee.kraftwerk.api.services.BatchExportService;
import fr.insee.kraftwerk.api.services.MainService;
import fr.insee.kraftwerk.api.services.ReportingDataService;
import fr.insee.kraftwerk.core.data.model.Mode;
import fr.insee.kraftwerk.core.utils.files.MinioImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.ApplicationArguments;
import org.springframework.core.env.Environment;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.anyBoolean;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class KraftwerkBatchTest {

    ConfigProperties configProperties;
    MinioConfig minioConfig;
    VaultConfig vaultConfig;
    ReportingDataService reportingDataService;
    MainService mainService;

    KraftwerkBatch kraftwerkBatch;

    Environment environment;

    BatchExportService batchExportService;

    @BeforeEach
    void setup() {
        configProperties = mock(ConfigProperties.class);
        minioConfig = mock(MinioConfig.class);
        vaultConfig = mock(VaultConfig.class);
        reportingDataService = mock(ReportingDataService.class);
        mainService = mock(MainService.class);
        environment = mock(Environment.class);
        batchExportService = mock(BatchExportService.class);

        when(minioConfig.isEnable()).thenReturn(false);
        when(configProperties.getDefaultDirectory()).thenReturn("/tmp");
        when(environment.getActiveProfiles()).thenReturn(new String[]{"test"});

        kraftwerkBatch = new KraftwerkBatch(
                configProperties,
                minioConfig,
                vaultConfig,
                reportingDataService,
                mainService,
                environment, batchExportService
        );
    }

    @Test
    void reporting_data_service_main_test() {
        ApplicationArguments args = mock(ApplicationArguments.class);
        when(args.getOptionNames()).thenReturn(Set.of("service", "reporting-data", "reporting-data-file-path",
                "questionnaireId"));
        when(args.getOptionValues("service")).thenReturn(List.of("MAIN"));
        when(args.getOptionValues("reporting-data")).thenReturn(List.of("true"));
        when(args.getOptionValues("reporting-data-file-path")).thenReturn(List.of("/test/reporting.csv"));
        when(args.getOptionValues("questionnaireId")).thenReturn(List.of("TESTCAMPAIGN1"));

        kraftwerkBatch.run(args);

        verify(reportingDataService, times(1)).processReportingData(any(), any());
        verifyNoInteractions(mainService);
    }

    @Test
    void reporting_data_service_genesis_test() {
        ApplicationArguments args = mock(ApplicationArguments.class);
        when(args.getOptionNames()).thenReturn(Set.of("service", "reporting-data", "reporting-data-file-path",
                "questionnaireId"));
        when(args.getOptionValues("service")).thenReturn(List.of("GENESIS"));
        when(args.getOptionValues("reporting-data")).thenReturn(List.of("true"));
        when(args.getOptionValues("reporting-data-file-path")).thenReturn(List.of("/test/reporting.csv"));
        when(args.getOptionValues("questionnaireId")).thenReturn(List.of("TESTCAMPAIGN1"));

        kraftwerkBatch.run(args);

        verify(reportingDataService, times(1)).processReportingDataGenesis(any(), any(), any());
        verifyNoInteractions(mainService);
    }

    @Test
    void reporting_data_service_no_file_path_error_test() {
        ApplicationArguments args = mock(ApplicationArguments.class);
        when(args.getOptionNames()).thenReturn(Set.of("service", "reporting-data",
                "questionnaireId"));
        when(args.getOptionValues("service")).thenReturn(List.of("MAIN"));
        when(args.getOptionValues("reporting-data")).thenReturn(List.of("true"));
        when(args.getOptionValues("questionnaireId")).thenReturn(List.of("TESTCAMPAIGN1"));

        assertThrows(IllegalArgumentException.class, () -> kraftwerkBatch.run(args));

        verifyNoInteractions(reportingDataService);
        verifyNoInteractions(mainService);
    }

    @Test
    void main_service_test() {
        ApplicationArguments args = mock(ApplicationArguments.class);
        when(args.getOptionNames()).thenReturn(Set.of("service", "questionnaireId", "with-ddi", "addState"));
        when(args.getOptionValues("service")).thenReturn(List.of("MAIN"));
        when(args.getOptionValues("questionnaireId")).thenReturn(List.of("TESTCAMPAIGN2"));
        when(args.getOptionValues("with-ddi")).thenReturn(List.of("true"));
        when(args.getOptionValues("addState")).thenReturn(List.of("false"));

        kraftwerkBatch.run(args);

        verify(batchExportService, times(1)).mainServiceBatch(
                any(),
                anyBoolean(),
                eq(false),
                eq(false)
        );
        verifyNoInteractions(reportingDataService);
    }

    @Test
    void main_service_genesis_test() {
        ApplicationArguments args = mock(ApplicationArguments.class);
        when(args.getOptionNames()).thenReturn(Set.of("service", "questionnaireId", "with-ddi", "add-states"));
        when(args.getOptionValues("service")).thenReturn(List.of("GENESIS"));
        when(args.getOptionValues("questionnaireId")).thenReturn(List.of("TESTCAMPAIGN2"));
        when(args.getOptionValues("with-ddi")).thenReturn(List.of("true"));
        when(args.getOptionValues("add-states")).thenReturn(List.of("true"));

        kraftwerkBatch.run(args);

        verify(batchExportService, times(1)).mainGenesisByQuestionnaireIdBatch(
                any(),
                any(),
                eq(KraftwerkBatch.DEFAULT_BATCH_SIZE),
                eq(false),
                eq(true)
        );
        verifyNoInteractions(reportingDataService);
    }

    @Test
    void main_service_genesis_with_batchSize_test() {
        ApplicationArguments args = mock(ApplicationArguments.class);
        when(args.getOptionNames()).thenReturn(Set.of("service", "questionnaireId", "with-ddi", "add-states", "batch-size"));
        when(args.getOptionValues("service")).thenReturn(List.of("GENESIS"));
        when(args.getOptionValues("questionnaireId")).thenReturn(List.of("TESTCAMPAIGN2"));
        when(args.getOptionValues("with-ddi")).thenReturn(List.of("true"));
        when(args.getOptionValues("add-states")).thenReturn(List.of("true"));
        when(args.getOptionValues("batch-size")).thenReturn(List.of("100"));

        kraftwerkBatch.run(args);

        verify(batchExportService, times(1)).mainGenesisByQuestionnaireIdBatch(
                any(),
                any(),
                eq(100),
                eq(false),
                eq(true)
        );
        verifyNoInteractions(reportingDataService);
    }

    @Test
    void main_service_genesis_with_mode_test() {
        ApplicationArguments args = mock(ApplicationArguments.class);
        when(args.getOptionNames()).thenReturn(Set.of("service", "questionnaireId", "with-ddi", "add-states", "mode"));
        when(args.getOptionValues("service")).thenReturn(List.of("GENESIS"));
        when(args.getOptionValues("questionnaireId")).thenReturn(List.of("TESTCAMPAIGN2"));
        when(args.getOptionValues("with-ddi")).thenReturn(List.of("true"));
        when(args.getOptionValues("add-states")).thenReturn(List.of("true"));
        when(args.getOptionValues("mode")).thenReturn(List.of("WEB"));

        kraftwerkBatch.run(args);

        verify(batchExportService, times(1)).mainGenesisByQuestionnaireIdBatch(
                any(),
                eq(Mode.WEB),
                eq(KraftwerkBatch.DEFAULT_BATCH_SIZE),
                eq(false),
                eq(true)
        );
        verifyNoInteractions(reportingDataService);
    }

    @Test
    void main_service_genesis_with_empty_mode_test() {
        ApplicationArguments args = mock(ApplicationArguments.class);
        when(args.getOptionNames()).thenReturn(Set.of("service", "questionnaireId", "with-ddi", "add-states", "mode"));
        when(args.getOptionValues("service")).thenReturn(List.of("GENESIS"));
        when(args.getOptionValues("questionnaireId")).thenReturn(List.of("TESTCAMPAIGN2"));
        when(args.getOptionValues("with-ddi")).thenReturn(List.of("true"));
        when(args.getOptionValues("add-states")).thenReturn(List.of("true"));
        when(args.getOptionValues("mode")).thenReturn(List.of(""));

        kraftwerkBatch.run(args);

        verify(batchExportService, times(1)).mainGenesisByQuestionnaireIdBatch(
                any(),
                eq(null),
                eq(KraftwerkBatch.DEFAULT_BATCH_SIZE),
                eq(false),
                eq(true)
        );
        verifyNoInteractions(reportingDataService);
    }

    @Test
    void main_service_with_encryption_test() {
        ApplicationArguments args = mock(ApplicationArguments.class);
        when(args.getOptionNames()).thenReturn(Set.of("service", "questionnaireId", "with-ddi", "with-encryption"));
        when(args.getOptionValues("service")).thenReturn(List.of("MAIN"));
        when(args.getOptionValues("questionnaireId")).thenReturn(List.of("TESTCAMPAIGN2"));
        when(args.getOptionValues("with-ddi")).thenReturn(List.of("true"));
        when(args.getOptionValues("with-encryption")).thenReturn(List.of("true"));

        kraftwerkBatch.run(args);

        verify(batchExportService, times(1)).mainServiceBatch(
                any(),
                anyBoolean(),
                eq(true),
                eq(false)

        );
        verifyNoInteractions(reportingDataService);
    }

    @Test
    void main_service_genesis_with_encryption_test() {
        ApplicationArguments args = mock(ApplicationArguments.class);
        when(args.getOptionNames()).thenReturn(Set.of("service", "questionnaireId", "with-ddi", "with-encryption"));
        when(args.getOptionValues("service")).thenReturn(List.of("GENESIS"));
        when(args.getOptionValues("questionnaireId")).thenReturn(List.of("TESTCAMPAIGN2"));
        when(args.getOptionValues("with-ddi")).thenReturn(List.of("true"));
        when(args.getOptionValues("with-encryption")).thenReturn(List.of("true"));

        kraftwerkBatch.run(args);

        verify(batchExportService, times(1)).mainGenesisByQuestionnaireIdBatch(
                any(),
                any(),
                eq(KraftwerkBatch.DEFAULT_BATCH_SIZE),
                eq(true),
                eq(false)
        );
        verifyNoInteractions(reportingDataService);
    }

    @Test
    void json_service_test() {
        ApplicationArguments args = mock(ApplicationArguments.class);
        when(args.getOptionNames()).thenReturn(Set.of("service", "questionnaireId", "with-ddi", "add-states"));
        when(args.getOptionValues("service")).thenReturn(List.of("JSON"));
        when(args.getOptionValues("questionnaireId")).thenReturn(List.of("TESTCAMPAIGN2"));
        when(args.getOptionValues("with-ddi")).thenReturn(List.of("true"));
        when(args.getOptionValues("add-states")).thenReturn(List.of("true"));

        kraftwerkBatch.run(args);

        verify(batchExportService, times(1)).jsonExtractionBatch(
                any(),
                any(),
                eq(KraftwerkBatch.DEFAULT_BATCH_SIZE),
                eq(null),
                eq(true)
        );
        verifyNoInteractions(reportingDataService);
    }

    @Test
    void json_service_with_batchSize_test() {
        ApplicationArguments args = mock(ApplicationArguments.class);
        when(args.getOptionNames()).thenReturn(Set.of("service", "questionnaireId", "with-ddi", "add-states", "batch-size"));
        when(args.getOptionValues("service")).thenReturn(List.of("JSON"));
        when(args.getOptionValues("questionnaireId")).thenReturn(List.of("TESTCAMPAIGN2"));
        when(args.getOptionValues("with-ddi")).thenReturn(List.of("true"));
        when(args.getOptionValues("add-states")).thenReturn(List.of("true"));
        when(args.getOptionValues("batch-size")).thenReturn(List.of("100"));

        kraftwerkBatch.run(args);

        verify(batchExportService, times(1)).jsonExtractionBatch(
                any(),
                any(),
                eq(100),
                eq(null),
                eq(true)
        );
        verifyNoInteractions(reportingDataService);
    }

    @Test
    void json_service_with_mode_test() {
        ApplicationArguments args = mock(ApplicationArguments.class);
        when(args.getOptionNames()).thenReturn(Set.of("service", "questionnaireId", "with-ddi", "add-states", "mode"));
        when(args.getOptionValues("service")).thenReturn(List.of("JSON"));
        when(args.getOptionValues("questionnaireId")).thenReturn(List.of("TESTCAMPAIGN2"));
        when(args.getOptionValues("with-ddi")).thenReturn(List.of("true"));
        when(args.getOptionValues("add-states")).thenReturn(List.of("true"));
        when(args.getOptionValues("mode")).thenReturn(List.of("WEB"));

        kraftwerkBatch.run(args);

        verify(batchExportService, times(1)).jsonExtractionBatch(
                any(),
                eq(Mode.WEB),
                eq(KraftwerkBatch.DEFAULT_BATCH_SIZE),
                eq(null),
                eq(true)
        );
        verifyNoInteractions(reportingDataService);
    }

    @Test
    void json_service_with_since_test() {
        ApplicationArguments args = mock(ApplicationArguments.class);
        when(args.getOptionNames()).thenReturn(Set.of("service", "questionnaireId", "with-ddi", "extract-json-since"));
        when(args.getOptionValues("service")).thenReturn(List.of("JSON"));
        when(args.getOptionValues("questionnaireId")).thenReturn(List.of("TESTCAMPAIGN2"));
        when(args.getOptionValues("with-ddi")).thenReturn(List.of("true"));
        when(args.getOptionValues("extract-json-since")).thenReturn(List.of("2022-12-01T12:00:00Z"));

        kraftwerkBatch.run(args);

        verify(batchExportService, times(1)).jsonExtractionBatch(
                any(),
                any(),
                eq(KraftwerkBatch.DEFAULT_BATCH_SIZE),
                eq(LocalDateTime.of(2022, 12, 1, 12, 0 ,0).toInstant(ZoneOffset.UTC)),
                eq(false)
        );
        verifyNoInteractions(reportingDataService);
    }

    @Test
    void json_service_with_since_error_test() {
        ApplicationArguments args = mock(ApplicationArguments.class);
        when(args.getOptionNames()).thenReturn(Set.of("service", "questionnaireId", "with-ddi", "extract-json-since"));
        when(args.getOptionValues("service")).thenReturn(List.of("JSON"));
        when(args.getOptionValues("questionnaireId")).thenReturn(List.of("TESTCAMPAIGN2"));
        when(args.getOptionValues("with-ddi")).thenReturn(List.of("true"));
        when(args.getOptionValues("extract-json-since")).thenReturn(List.of("ERROR"));

        assertThrows(IllegalArgumentException.class, () -> kraftwerkBatch.run(args));

        verifyNoInteractions(mainService);
        verifyNoInteractions(reportingDataService);
    }

    @Test
    void file_by_file_service_test() {
        ApplicationArguments args = mock(ApplicationArguments.class);
        when(args.getOptionNames()).thenReturn(Set.of("service", "questionnaireId", "with-ddi"));
        when(args.getOptionValues("service")).thenReturn(List.of("FILE_BY_FILE"));
        when(args.getOptionValues("questionnaireId")).thenReturn(List.of("TESTCAMPAIGN2"));
        when(args.getOptionValues("with-ddi")).thenReturn(List.of("true"));

        kraftwerkBatch.run(args);

        verify(batchExportService, times(1)).mainFileByFileBatch(
                any(),
                anyBoolean(),
                eq(false),
                eq(false)
        );
        verifyNoInteractions(reportingDataService);
    }


    @Test
    void file_by_file_service_with_encryption_test() {
        ApplicationArguments args = mock(ApplicationArguments.class);
        when(args.getOptionNames()).thenReturn(Set.of("service", "questionnaireId", "with-ddi", "with-encryption"));
        when(args.getOptionValues("service")).thenReturn(List.of("FILE_BY_FILE"));
        when(args.getOptionValues("questionnaireId")).thenReturn(List.of("TESTCAMPAIGN2"));
        when(args.getOptionValues("with-ddi")).thenReturn(List.of("true"));
        when(args.getOptionValues("with-encryption")).thenReturn(List.of("true"));

        kraftwerkBatch.run(args);

        verify(batchExportService, times(1)).mainFileByFileBatch(
                any(),
                anyBoolean(),
                eq(true),
                eq(false)
        );
        verifyNoInteractions(reportingDataService);
    }

    @Test
    void minio_enabled_test() {
        // GIVEN
        when(minioConfig.isEnable()).thenReturn(true);
        when(minioConfig.getEndpoint()).thenReturn("http://localhost:9000");
        when(minioConfig.getAccessKey()).thenReturn("minio");
        when(minioConfig.getSecretKey()).thenReturn("minio123");
        when(minioConfig.getBucketName()).thenReturn("kraftwerk-bucket");

        // WHEN
        kraftwerkBatch = new KraftwerkBatch(
                configProperties,
                minioConfig,
                vaultConfig,
                reportingDataService,
                mainService,
                environment,
                batchExportService
        );

        // THEN
        assertNotNull(kraftwerkBatch.minioClient, "Minio client must be initialized");
        assertNotNull(kraftwerkBatch.fileSystem, "filesystem must be initialized");
        assertInstanceOf(MinioImpl.class, kraftwerkBatch.fileSystem, "filesystem is not MinioImpl");

        // Verify minio getters got called
        verify(minioConfig).getEndpoint();
        verify(minioConfig).getAccessKey();
        verify(minioConfig).getSecretKey();
        verify(minioConfig).getBucketName();
    }

    @Test
    void invalid_service_test() {
        ApplicationArguments args = mock(ApplicationArguments.class);
        when(args.getOptionNames()).thenReturn(Set.of("service"));
        when(args.getOptionValues("service")).thenReturn(List.of("ERROR"));

        assertThrows(IllegalArgumentException.class, () -> kraftwerkBatch.run(args));

        verifyNoInteractions(mainService);
        verifyNoInteractions(reportingDataService);
    }

    @Test
    void no_questionnaireId_test() {
        ApplicationArguments args = mock(ApplicationArguments.class);
        when(args.getOptionNames()).thenReturn(Set.of("service"));
        when(args.getOptionValues("service")).thenReturn(List.of("GENESIS"));

        assertThrows(IllegalArgumentException.class, () -> kraftwerkBatch.run(args));

        verifyNoInteractions(mainService);
        verifyNoInteractions(reportingDataService);
    }

    @ParameterizedTest
    @ValueSource(strings = {"reporting-data","with-encryption","with-ddi"})
    void invalid_boolean_arg_test(String booleanArgName){
        ApplicationArguments args = mock(ApplicationArguments.class);
        when(args.getOptionNames()).thenReturn(Set.of("service", booleanArgName));
        when(args.getOptionValues("service")).thenReturn(List.of("MAIN"));
        when(args.getOptionValues(booleanArgName)).thenReturn(List.of("ERROR"));

        assertThrows(IllegalArgumentException.class, () -> kraftwerkBatch.run(args));

        verifyNoInteractions(mainService);
        verifyNoInteractions(reportingDataService);
    }

    @Test
    void main_service_genesis_batch_shouldReturnJobIdAndOutputPath() {
        ApplicationArguments args = mock(ApplicationArguments.class);
        when(args.getOptionNames()).thenReturn(Set.of("service", "questionnaireId", "with-ddi", "add-states"));
        when(args.getOptionValues("service")).thenReturn(List.of("GENESIS"));
        when(args.getOptionValues("questionnaireId")).thenReturn(List.of("TESTCAMPAIGN2"));
        when(args.getOptionValues("with-ddi")).thenReturn(List.of("true"));
        when(args.getOptionValues("add-states")).thenReturn(List.of("true"));

        BatchResponseDto dto = new BatchResponseDto("job-123", "out/TESTCAMPAIGN2/2026_04_22_10_00_00");
        when(batchExportService.mainGenesisByQuestionnaireIdBatch(
                any(),
                any(),
                anyInt(),
                anyBoolean(),
                anyBoolean()
        )).thenReturn(dto);

        ResponseEntity<Object> response = ReflectionTestUtils.invokeMethod(kraftwerkBatch, "runBatchMode", args);

        assertNotNull(response);
        assertEquals(202, response.getStatusCode().value());
        assertEquals(dto, response.getBody());

        verify(batchExportService, times(1)).mainGenesisByQuestionnaireIdBatch(
                eq("TESTCAMPAIGN2"),
                eq(null),
                eq(KraftwerkBatch.DEFAULT_BATCH_SIZE),
                eq(false),
                eq(true)
        );
    }
}
