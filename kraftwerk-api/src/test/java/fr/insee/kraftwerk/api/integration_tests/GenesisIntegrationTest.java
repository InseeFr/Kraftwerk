package fr.insee.kraftwerk.api.integration_tests;

import com.fasterxml.jackson.core.JsonProcessingException;
import fr.insee.kraftwerk.KraftwerkApi;
import fr.insee.kraftwerk.api.client.GenesisClient;
import fr.insee.kraftwerk.core.data.model.InterrogationId;
import fr.insee.kraftwerk.core.data.model.Mode;
import fr.insee.kraftwerk.core.data.model.SurveyUnitUpdateLatest;
import fr.insee.kraftwerk.core.exceptions.KraftwerkException;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.ArrayList;
import java.util.List;

@SpringBootTest(classes = KraftwerkApi.class)
@ActiveProfiles("local_integration_test")
@Slf4j
class GenesisIntegrationTest {

    @Autowired
    private GenesisClient genesisClient;

    //Constants
    private static final String CAMPAIGN_ID = "INTEGRATIONTEST_CAMPAIGN";
    private static final String QUESTIONNAIRE_ID = "INTEGRATIONTEST_QUESTIONNAIRE";
    private static final String[] INTERROGATION_IDS = new String[]{"UE1", "UE2"};

    @Test
    void ping_genesis_test(){
        //When
        String response = genesisClient.pingGenesis();
        log.debug("Ping genesis returned :\n{}",response);

        //Then
        Assertions.assertThat(response).contains("OK");
    }

    @Test
    void getInterrogationIds_test() throws KraftwerkException {
        //When
        List<InterrogationId> response = genesisClient.getInterrogationIds(QUESTIONNAIRE_ID);
        log.debug("Get interrogationIds returned :\n{}", response);

        //Then
        Assertions.assertThat(response).hasSize(3);
    }

    @Test
    void getModes_test() throws KraftwerkException {
        //When
        List<Mode> response = genesisClient.getModes(CAMPAIGN_ID);
        log.debug("Get modes returned :\n{}", response);

        //Then
        Assertions.assertThat(response).containsExactly(Mode.F2F);
    }

    @Test
    void getUEsLatestState_test() throws KraftwerkException {
        //Given
        List<InterrogationId> interrogationIds = new ArrayList<>();
        for(String interrogationIdString : INTERROGATION_IDS){
            InterrogationId interrogationId = new InterrogationId();
            interrogationId.setId(interrogationIdString);
            interrogationIds.add(interrogationId);
        }

        //When
        List<SurveyUnitUpdateLatest> response = genesisClient.getUEsLatestState(QUESTIONNAIRE_ID, interrogationIds);
        log.debug("Get UEs latest states returned :\n{}", response);

        //Then
        Assertions.assertThat(response).hasSize(2);
    }

    @Test
    void getQuestionnaireModelIds_test() throws KraftwerkException, JsonProcessingException {
        //When
        List<String> response = genesisClient.getQuestionnaireModelIds(CAMPAIGN_ID);
        Assertions.assertThat(response).containsExactly(QUESTIONNAIRE_ID);
    }
}
