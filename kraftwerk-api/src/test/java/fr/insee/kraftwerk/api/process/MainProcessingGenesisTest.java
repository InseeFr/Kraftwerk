package fr.insee.kraftwerk.api.process;

import fr.insee.bpm.metadata.model.MetadataModel;
import fr.insee.kraftwerk.api.client.GenesisClient;
import fr.insee.kraftwerk.api.configuration.ConfigProperties;
import fr.insee.kraftwerk.core.data.model.InterrogationId;
import fr.insee.kraftwerk.core.data.model.Mode;
import fr.insee.kraftwerk.core.data.model.SurveyUnitUpdateLatest;
import fr.insee.kraftwerk.core.utils.files.FileSystemImpl;
import fr.insee.kraftwerk.core.utils.files.FileUtilsInterface;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ActiveProfiles("test")
@SpringBootTest
class MainProcessingGenesisTest {

    private ConfigProperties configProperties;
    private GenesisClient genesisClient;
    private FileUtilsInterface fileUtils;
    private MainProcessingGenesis mainProcessing;
    private RestTemplate restTemplate;

    @BeforeEach
    void setUp() {

        RestTemplateBuilder restTemplateBuilder = new RestTemplateBuilder();
        restTemplate = restTemplateBuilder.additionalInterceptors(Collections.emptyList()).build();
        restTemplate.setInterceptors(Collections.emptyList());

        configProperties = new ConfigProperties() {
            @Override
            public String getDefaultDirectory() {
                return "src/test/resources";
            }
        };

        genesisClient = new GenesisClient(restTemplate,configProperties) {
            @Override
            public List<Mode> getModes(String idCampaign) {
                return Collections.singletonList(Mode.WEB);
            }

            @Override
            public List<String> getQuestionnaireModelIds(String campaignId) {
                return Collections.singletonList("id");
            }

            @Override
            public List<InterrogationId> getInterrogationIds(String questionnaireId){
                return Collections.singletonList(new InterrogationId());

            }

            @Override
            public List<SurveyUnitUpdateLatest> getUEsLatestState(String questionnaireId, List<InterrogationId> interrogationIds){
                SurveyUnitUpdateLatest surveyUnitUpdateLatest = new SurveyUnitUpdateLatest();
                surveyUnitUpdateLatest.setMode(Mode.WEB);
                return Collections.singletonList(surveyUnitUpdateLatest);
            }
        };


        fileUtils = new FileSystemImpl("defaultDir") {
            @Override
            public String findFile(String directory, String fileName) {
                return "src/test/resources/ddi-SAMPLETEST-DATAONLY-v1.xml";
            }
        };

        mainProcessing = new MainProcessingGenesis(configProperties, genesisClient, fileUtils, true);
    }

    @Test
    void testInitLoadsMetadata() throws Exception {
        String idCampaign = "campaign1";

        Map<String, MetadataModel> mockMetadata = Map.of("WEB", new MetadataModel());

//        MetadataUtilsGenesis.setMetadata(mockMetadata); // Hypothèse d'une méthode setter pour injecter les données de test

        mainProcessing.init(idCampaign);

        assertNotNull(mainProcessing.getMetadataModels());
        assertTrue(mainProcessing.getMetadataModels().containsKey("WEB"));
    }

/* TODO fix me
   @Test
    void testRunMainSQLException() {
        String idCampaign = "campaign1";

        KraftwerkException thrown = assertThrows(KraftwerkException.class, () -> mainProcessing.runMain(idCampaign, 100));
        assertEquals("SQL error", thrown.getMessage());
    }
    */

}
