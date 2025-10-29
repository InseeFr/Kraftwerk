package fr.insee.kraftwerk.api.process;

import fr.insee.kraftwerk.TestConfig;
import fr.insee.kraftwerk.api.TestConstants;
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

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

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
                return TestConstants.TEST_RESOURCES_DIRECTORY;
            }
        };

        GenesisClient genesisClient = new GenesisClient(restTemplate, configProperties) {
            @Override
            public List<Mode> getModes(String idCampaign) {
                return Collections.singletonList(Mode.WEB);
            }

            @Override
            public List<InterrogationId> getPaginatedInterrogationIds(String questionnaireId, long totalSize, long blockSize, long page) {
                return Collections.singletonList(new InterrogationId());
            }

            @Override
            public Long countInterrogationIds(String questionnaireId) {
                return 1L;
            }

            @Override
            public List<String> getDistinctModesByQuestionnaireId(String questionnaireId) {
                return Collections.singletonList(Mode.WEB.toString());
            }

            @Override
            public List<SurveyUnitUpdateLatest> getUEsLatestState(String questionnaireId, List<InterrogationId> interrogationIds, List<String> modes) {
                SurveyUnitUpdateLatest surveyUnitUpdateLatest = new SurveyUnitUpdateLatest();
                surveyUnitUpdateLatest.setCollectedVariables(new ArrayList<>());
                surveyUnitUpdateLatest.setExternalVariables(new ArrayList<>());
                surveyUnitUpdateLatest.setMode(Mode.WEB);
                //Note : Cannot set "partitionId"...
                surveyUnitUpdateLatest.setSurveyUnitId("surveyUnitId_aaa");
                surveyUnitUpdateLatest.setContextualId("contextualId_bbb");
                surveyUnitUpdateLatest.setIsCapturedIndirectly(Boolean.FALSE);
                surveyUnitUpdateLatest.setValidationDate(LocalDateTime.of(2021, 04, 24, 14, 33, 48, 123456789));
                return Collections.singletonList(surveyUnitUpdateLatest);
            }

            @Override
            public List<String> getQuestionnaireModelIds(String campaignId) {
                return Collections.singletonList("id");
            }

            @Override
            public String getQuestionnaireModelIdByInterrogationId(String interrogationId) {
                //Get the questionnaireId corresponding to a provided interrogationId
                return "questionnaire123456";
            }
        };


        FileUtilsInterface fileUtils = new FileSystemImpl("defaultDir");

        KraftwerkExecutionContext kraftwerkExecutionContext = new KraftwerkExecutionContext(
                null,
                false,
                true,
                false,
                419430400L
        );

        mainProcessing = new MainProcessingGenesis(configProperties, genesisClient, fileUtils, kraftwerkExecutionContext);
    }

    /**
     * @author Adrien Marchal
     */
    @Test
    void testInitLoadsMetadata() throws Exception {
        String idCampaign = "campaign1";

        mainProcessing.init(idCampaign);

        assertNotNull(mainProcessing.getMetadataModels());
        assertTrue(mainProcessing.getMetadataModels().containsKey("WEB"));
    }

    /**
     * @author Adrien Marchal
     */
    @Test
    void testRunMainOk() {
        String idCampaign = "campaign1";
        assertDoesNotThrow(() -> mainProcessing.runMain(idCampaign, 100, 1, 1));
    }


    /**
     * @author Adrien Marchal
     */
    @Test
    void testRunMainJsonOk() {
        //NOTE : campaignId MUST be "campaign1" as a DDI file exists in "src\test\resources\specs\campaign1\WEB" for unit tests purpose!
        assertDoesNotThrow(() -> mainProcessing.runMainJson("campaign1", "integgrogationId123"));
    }


    /**
     * @author Adrien Marchal
     */
    @Test
    void testRunMainJson_checkResult() throws Exception {
        // 1. Mock the dependencies
        MainProcessingGenesis spyMainProcessingGenesis = spy(mainProcessing);

        //2. Run test
        String idCampaign = "campaign1"; //A DDI file exists in "src\test\resources\specs\campaign1\WEB" for unit tests purpose
        String interrogationId = "integgrogationId123";
        Map<String, Object> result = spyMainProcessingGenesis.runMainJson(idCampaign, interrogationId);

        //3. Check response content
        assertEquals("integgrogationId123", result.get("interrogationId"));
        assertEquals("questionnaire123456", result.get("questionnaireModelId"));
        assertNull(result.get("partitionId")); //NOTE : Cannot set "partitionId"...
        assertEquals("surveyUnitId_aaa", result.get("surveyUnitId"));
        assertEquals("contextualId_bbb", result.get("contextualId"));
        assertEquals(Mode.WEB, result.get("mode"));
        assertEquals(Boolean.FALSE, result.get("isCapturedIndirectly"));
        assertEquals(LocalDateTime.of(2021, 04, 24, 14, 33, 48, 123456789), result.get("validationDate"));
        assertEquals(2, ((LinkedHashMap<String, Object>)result.get("data")).size());
        assertEquals(1, ((List<Object>)((LinkedHashMap<String, Object>)result.get("data")).get("RACINE")).size());
        assertEquals(0, ((List<Object>)((LinkedHashMap<String, Object>)result.get("data")).get("BOUCLE_PRENOMS")).size());
    }


}
