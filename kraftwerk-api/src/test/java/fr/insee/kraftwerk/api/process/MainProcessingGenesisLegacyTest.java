package fr.insee.kraftwerk.api.process;

import com.fasterxml.jackson.core.JsonProcessingException;
import fr.insee.bpm.metadata.model.MetadataModel;
import fr.insee.kraftwerk.api.client.GenesisClient;
import fr.insee.kraftwerk.api.configuration.ConfigProperties;
import fr.insee.kraftwerk.core.data.model.InterrogationId;
import fr.insee.kraftwerk.core.data.model.Mode;
import fr.insee.kraftwerk.core.data.model.SurveyUnitUpdateLatest;
import fr.insee.kraftwerk.core.exceptions.KraftwerkException;
import fr.insee.kraftwerk.core.utils.KraftwerkExecutionContext;
import fr.insee.kraftwerk.core.utils.files.FileSystemImpl;
import fr.insee.kraftwerk.core.utils.files.FileUtilsInterface;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MainProcessingGenesisLegacyTest {

    @Mock
    private GenesisClient genesisClient;

    private MainProcessingGenesisLegacy mainProcessing;

    @BeforeEach
    void setUp() throws KraftwerkException, JsonProcessingException {
        FileUtilsInterface fileUtils = new FileSystemImpl("defaultDir");
        ConfigProperties configProperties = new ConfigProperties() {
            @Override
            public String getDefaultDirectory() {
                return "src/test/resources";
            }
        };

        KraftwerkExecutionContext kraftwerkExecutionContext = new KraftwerkExecutionContext(
                null,
                false,
                true,
                false,
                419430400L
        );
        mainProcessing = new MainProcessingGenesisLegacy(configProperties, genesisClient, fileUtils, kraftwerkExecutionContext);

    }

    @Test
    void testInitLoadsMetadata() throws Exception {
        String idCampaign = "campaign1";
        mainProcessing.init(idCampaign, List.of(Mode.WEB));

        assertNotNull(mainProcessing.getMetadataModelsByMode());
        assertTrue(mainProcessing.getMetadataModelsByMode().containsKey("WEB"));
    }

   @Test
    void testRunMainOk() throws KraftwerkException, JsonProcessingException {
       when(genesisClient.getModes(any()))
               .thenReturn(List.of(Mode.WEB));

       when(genesisClient.getQuestionnaireModelIds(any()))
               .thenReturn(List.of("id"));

       when(genesisClient.getInterrogationIds(any()))
               .thenReturn(List.of(new InterrogationId()));

       when(genesisClient.getUEsLatestState(any(), any()))
               .thenReturn(mockSurveyUnit());

       when(genesisClient.getMetadataByQuestionnaireIdAndMode(any(), any()))
               .thenReturn(new MetadataModel()); // 🔑 clé du problème

        String idCampaign = "campaign1";
        assertDoesNotThrow(() -> mainProcessing.runMain(idCampaign, 100));
    }

    private List<SurveyUnitUpdateLatest>  mockSurveyUnit(){
        SurveyUnitUpdateLatest surveyUnitUpdateLatest = new SurveyUnitUpdateLatest();
        surveyUnitUpdateLatest.setCollectedVariables(new ArrayList<>());
        surveyUnitUpdateLatest.setExternalVariables(new ArrayList<>());
        surveyUnitUpdateLatest.setMode(Mode.WEB);
        return Collections.singletonList(surveyUnitUpdateLatest);
    }


}
