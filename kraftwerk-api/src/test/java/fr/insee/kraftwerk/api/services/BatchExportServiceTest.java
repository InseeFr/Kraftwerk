package fr.insee.kraftwerk.api.services;


import fr.insee.kraftwerk.api.configuration.ConfigProperties;
import fr.insee.kraftwerk.api.configuration.MinioConfig;
import fr.insee.kraftwerk.api.services.async.InMemoryExportJobStore;
import fr.insee.kraftwerk.api.services.async.MainAsyncService;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;


@ExtendWith(MockitoExtension.class)
class BatchExportServiceTest {

    @Mock
    private MainAsyncService mainAsyncService;
    @Mock
    private ConfigProperties configProperties;
    @Mock
    private MinioConfig minioConfig;
    @Mock
    private InMemoryExportJobStore exportJobStore;

    private BatchExportService batchExportService;

}