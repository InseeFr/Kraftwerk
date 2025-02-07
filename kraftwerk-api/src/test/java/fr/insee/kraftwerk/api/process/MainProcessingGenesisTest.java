package fr.insee.kraftwerk.api.process;

import fr.insee.bpm.metadata.model.MetadataModel;
import fr.insee.kraftwerk.api.client.GenesisClient;
import fr.insee.kraftwerk.api.configuration.ConfigProperties;
import fr.insee.kraftwerk.core.data.model.Mode;
import fr.insee.kraftwerk.core.exceptions.KraftwerkException;
import fr.insee.kraftwerk.core.metadata.MetadataUtilsGenesis;
import fr.insee.kraftwerk.core.utils.SqlUtils;
import fr.insee.kraftwerk.core.utils.files.FileUtilsInterface;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.RestTemplate;

import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@ActiveProfiles("test")
@SpringBootTest
class MainProcessingGenesisTest {

    private ConfigProperties configProperties ;
    private GenesisClient mockClient;
    private FileUtilsInterface mockFileUtils;
    private MainProcessingGenesis mainProcessing;

    @BeforeEach
    void setUp() {

        RestTemplate mockRestTemplate = mock(RestTemplate.class);
        RestTemplateBuilder mockRestTemplateBuilder = mock(RestTemplateBuilder.class);
        doReturn(mockRestTemplate).when(mockRestTemplateBuilder).build();

        configProperties = mock(ConfigProperties.class);
        doReturn("src/test/resources").when(configProperties).getDefaultDirectory();

        mockClient = mock(GenesisClient.class);
        doReturn(configProperties).when(mockClient).getConfigProperties();

        mockFileUtils = mock(FileUtilsInterface.class);
        mainProcessing = Mockito.spy(new MainProcessingGenesis(mockClient, mockFileUtils));

    }

    @Test
    void testInitLoadsMetadata() throws Exception {
        // Arrange
        String idCampaign = "campaign1";
        List<Mode> modesList = Collections.singletonList(Mode.WEB);

        // Mock MetadataUtilsGenesis
        doReturn(modesList).when(mockClient).getModes(anyString());
        doReturn("myFile").when(mockFileUtils).findFile(anyString(),anyString());

        Map<String, MetadataModel> mockMetadata = Map.of("WEB", mock(MetadataModel.class));
        Mockito.mockStatic(MetadataUtilsGenesis.class)
                .when(() -> MetadataUtilsGenesis.getMetadata(any(), eq(mockFileUtils)))
                .thenReturn(mockMetadata);

        // Act
        mainProcessing.init(idCampaign);

        // Assert
        assertNotNull(mainProcessing.getMetadataModels());
        verify(mockClient).getModes(idCampaign);
    }

    @Test
    void testRunMainSQLException() {
        // Arrange
        String idCampaign = "campaign1";

        // Mocking SQLUtils.openConnection to throw an exception
        Mockito.mockStatic(SqlUtils.class)
                .when(SqlUtils::openConnection)
                .thenThrow(new SQLException("Test SQL Exception"));

        // Act & Assert
        KraftwerkException thrown = assertThrows(KraftwerkException.class, () -> mainProcessing.runMain(idCampaign));
        assertEquals("SQL error", thrown.getMessage());
    }


}
