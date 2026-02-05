package fr.insee.kraftwerk;

import fr.insee.kraftwerk.api.configuration.ConfigProperties;import fr.insee.kraftwerk.api.configuration.MinioConfig;import fr.insee.kraftwerk.api.services.KraftwerkService;
import fr.insee.kraftwerk.api.services.MainService;
import org.junit.jupiter.api.BeforeEach;import static org.mockito.Mockito.mock;

public class KraftwerkIntegrationTests {

    private ConfigProperties configProperties;
    private MinioConfig minioConfig;

    private MainService controller;

    @BeforeEach
    void setUp() {
        configProperties = mock(ConfigProperties.class);
        //TODO mock config
        //TODO comment mocker le genesis client ?


        minioConfig = mock(MinioConfig.class);
        //TODO mock config

        controller = new MainService(
                controller
        );


    }


}
