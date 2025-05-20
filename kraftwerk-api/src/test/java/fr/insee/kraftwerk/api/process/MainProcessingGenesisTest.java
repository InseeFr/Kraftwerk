package fr.insee.kraftwerk.api.process;

import fr.insee.kraftwerk.TestConfig;
import fr.insee.kraftwerk.api.client.GenesisClient;
import fr.insee.kraftwerk.api.configuration.ConfigProperties;
import fr.insee.kraftwerk.core.data.model.InterrogationId;
import fr.insee.kraftwerk.core.data.model.Mode;
import fr.insee.kraftwerk.core.data.model.SurveyUnitUpdateLatest;
import fr.insee.kraftwerk.core.utils.KraftwerkExecutionContext;
import fr.insee.kraftwerk.core.utils.files.FileSystemImpl;
import fr.insee.kraftwerk.core.utils.files.FileUtilsInterface;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ActiveProfiles({"test", "ci-public"})
@SpringBootTest
@AutoConfigureMockMvc
@Import(TestConfig.class)
@ComponentScan(basePackages = {"fr.insee.kraftwerk.core.encryption", "fr.insee.kraftwerk.core.outputs"})
class MainProcessingGenesisTest {

    private ConfigProperties configProperties;
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

        GenesisClient genesisClient = new GenesisClient(restTemplate, configProperties) {
            @Override
            public List<Mode> getModes(String idCampaign) {
                return Collections.singletonList(Mode.WEB);
            }

            @Override
            public List<String> getQuestionnaireModelIds(String campaignId) {
                return Collections.singletonList("id");
            }

            @Override
            public List<InterrogationId> getInterrogationIds(String questionnaireId) {
                return Collections.singletonList(new InterrogationId());

            }

            @Override
            public List<SurveyUnitUpdateLatest> getUEsLatestState(String questionnaireId, List<InterrogationId> interrogationIds) {
                SurveyUnitUpdateLatest surveyUnitUpdateLatest = new SurveyUnitUpdateLatest();
                surveyUnitUpdateLatest.setCollectedVariables(new ArrayList<>());
                surveyUnitUpdateLatest.setExternalVariables(new ArrayList<>());
                surveyUnitUpdateLatest.setMode(Mode.WEB);
                return Collections.singletonList(surveyUnitUpdateLatest);
            }

            //========= OPTIMISATIONS PERFS (START) ==========
            @Override
            public List<InterrogationId> getPaginatedInterrogationIds(String questionnaireId, long totalSize, int workersNumbers, int workerId,
                                                                      long blockSize, long page) {
                return Collections.singletonList(new InterrogationId());
            }

            @Override
            public Long countInterrogationIds(String questionnaireId) {
                return 1L;
            }

            @Override
            public List<String> getDistinctModesByQuestionnaire(String questionnaireId) {
                return Collections.singletonList(Mode.WEB.toString());
            }

            @Override
            public List<SurveyUnitUpdateLatest> getUEsLatestStateV2(String questionnaireId, List<InterrogationId> interrogationIds, List<String> modes) {
                SurveyUnitUpdateLatest surveyUnitUpdateLatest = new SurveyUnitUpdateLatest();
                surveyUnitUpdateLatest.setCollectedVariables(new ArrayList<>());
                surveyUnitUpdateLatest.setExternalVariables(new ArrayList<>());
                surveyUnitUpdateLatest.setMode(Mode.WEB);
                return Collections.singletonList(surveyUnitUpdateLatest);
            }
            //========= OPTIMISATIONS PERFS (END) ==========
        };


        FileUtilsInterface fileUtils = new FileSystemImpl("defaultDir") {
            @Override
            public String findFile(String directory, String fileName) {
                return "src/test/resources/ddi-SAMPLETEST-DATAONLY-v1.xml";
            }
        };

        KraftwerkExecutionContext kraftwerkExecutionContext = new KraftwerkExecutionContext(
                null,
                false,
                true,
                false,
                419430400L
        );

        mainProcessing = new MainProcessingGenesis(configProperties, genesisClient, fileUtils, kraftwerkExecutionContext);
    }

    @Test
    void testInitLoadsMetadata() throws Exception {
        String idCampaign = "campaign1";

        mainProcessing.init(idCampaign);

        assertNotNull(mainProcessing.getMetadataModels());
        assertTrue(mainProcessing.getMetadataModels().containsKey("WEB"));
    }

   @Test
    void testRunMainOk() {
        String idCampaign = "campaign1";
        assertDoesNotThrow(() -> mainProcessing.runMain(idCampaign, 100));
    }

    //========= OPTIMISATIONS PERFS (START) ==========
    /**
     * @author Adrien Marchal
     */
    @Test
    void testRunMainV2Ok() {
        String idCampaign = "campaign1";
        assertDoesNotThrow(() -> mainProcessing.runMainV2(idCampaign, 100, 1, 1));
    }
    //========= OPTIMISATIONS PERFS (END) ==========


}
