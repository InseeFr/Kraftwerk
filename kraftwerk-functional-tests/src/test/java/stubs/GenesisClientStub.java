package stubs;

import fr.insee.kraftwerk.api.client.GenesisClient;
import fr.insee.kraftwerk.api.configuration.ConfigProperties;
import fr.insee.kraftwerk.core.data.model.InterrogationId;
import fr.insee.kraftwerk.core.data.model.Mode;
import fr.insee.kraftwerk.core.data.model.SurveyUnitUpdateLatest;
import lombok.Getter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

//This class reproduces Genesis expected behaviour for functionnal tests
@Getter
public class GenesisClientStub extends GenesisClient {

    List<SurveyUnitUpdateLatest> mongoStub;

    public GenesisClientStub(ConfigProperties configProperties) {
        super(configProperties);
        this.mongoStub = new ArrayList<>();
    }

    @Override
    public String pingGenesis(){
        return "";
    }

    @Override
    public List<InterrogationId> getInterrogationIds(String questionnaireId) {
        List<InterrogationId> list = new ArrayList<>();

        List<SurveyUnitUpdateLatest> filteredMongo = mongoStub.stream().filter(
                surveyUnitUpdateLatest -> surveyUnitUpdateLatest.getQuestionnaireId().equals(questionnaireId)
        ).toList();


        for (SurveyUnitUpdateLatest surveyUnitUpdateLatest : filteredMongo){
            InterrogationId interrogationId = new InterrogationId();
            interrogationId.setId(surveyUnitUpdateLatest.getInterrogationId());
            list.add(interrogationId);
        }
        return list;
    }

    @Override
    public List<Mode> getModes(String campaignId) {
        Set<Mode> set = new HashSet<>();

        List<SurveyUnitUpdateLatest> filteredMongo = mongoStub.stream().filter(
                surveyUnitUpdateLatest -> surveyUnitUpdateLatest.getCampaignId().equals(campaignId)
        ).toList();

        for (SurveyUnitUpdateLatest surveyUnitUpdateLatest : filteredMongo){
            set.add(surveyUnitUpdateLatest.getMode());
        }
        return set.stream().toList();
    }

    @Override
    public List<SurveyUnitUpdateLatest> getUEsLatestState(String questionnaireId, List<InterrogationId> interrogationIds) {
        List<SurveyUnitUpdateLatest> list = new ArrayList<>();

        List<SurveyUnitUpdateLatest> mongoFiltered1 = mongoStub.stream()
                .filter(
                        surveyUnitUpdateLatest -> surveyUnitUpdateLatest.getQuestionnaireId().equals(questionnaireId)
                ).toList();

        Set<SurveyUnitUpdateLatest> mongoFiltered2 = new HashSet<>();
        for(SurveyUnitUpdateLatest surveyUnitUpdate : mongoFiltered1){
            for(InterrogationId interrogationId : interrogationIds){
                if(interrogationId.getId().equals(surveyUnitUpdate.getInterrogationId())){
                    mongoFiltered2.add(surveyUnitUpdate);
                }
            }
        }

        for(SurveyUnitUpdateLatest surveyUnitUpdate : mongoFiltered2){
            //Delete older document
            list.removeIf(surveyUnitUpdateLatest -> surveyUnitUpdateLatest.getInterrogationId().equals(surveyUnitUpdate.getInterrogationId())
                    && surveyUnitUpdateLatest.getMode().equals(surveyUnitUpdate.getMode())
            );

            list.add(surveyUnitUpdate);
        }

        return list;
    }

    @Override
    public List<String> getQuestionnaireModelIds(String campaignId) {
        Set<String> set = new HashSet<>();

        List<SurveyUnitUpdateLatest> mongoFiltered = mongoStub.stream()
                .filter(
                        surveyUnitUpdateLatest -> surveyUnitUpdateLatest.getCampaignId().equals(campaignId)
                ).toList();

        for(SurveyUnitUpdateLatest doc : mongoFiltered){
            set.add(doc.getQuestionnaireId());
        }

        return set.stream().toList();
    }


    //========= OPTIMISATIONS PERFS (START) ==========
    @Override
    public List<InterrogationId> getPaginatedInterrogationIds(String questionnaireId, long totalSize, long blockSize, long page) {
        //For first version of this stub, we Use same stub as "getInterrogationIds()"
        return getInterrogationIds(questionnaireId);
    }

    @Override
    public Long countInterrogationIds(String questionnaireId) {
        return 1L;
    }

    @Override
    public List<String> getDistinctModesByQuestionnaireIdV2(String questionnaireId) {
        Set<String> set = new HashSet<>();

        List<SurveyUnitUpdateLatest> mongoFiltered = mongoStub.stream()
                .filter(
                        surveyUnitUpdateLatest -> surveyUnitUpdateLatest.getQuestionnaireId().equals(questionnaireId)
                ).toList();

        for(SurveyUnitUpdateLatest doc : mongoFiltered){
            set.add(doc.getQuestionnaireId());
        }

        return set.stream().toList();
    }

    @Override
    public List<SurveyUnitUpdateLatest> getUEsLatestStateV2(String questionnaireId, List<InterrogationId> interrogationIds, List<String> modes) {
        //For first version of this stub, we Use same stub as "getUEsLatestState()"
        return getUEsLatestState(questionnaireId, interrogationIds);
    }
    //========= OPTIMISATIONS PERFS (END) ==========

}
