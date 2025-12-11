package stubs;

import fr.insee.bpm.metadata.model.MetadataModel;
import fr.insee.kraftwerk.api.client.GenesisClient;
import fr.insee.kraftwerk.api.configuration.ConfigProperties;
import fr.insee.kraftwerk.core.data.model.InterrogationId;
import fr.insee.kraftwerk.core.data.model.Mode;
import fr.insee.kraftwerk.core.data.model.SurveyUnitUpdateLatest;
import fr.insee.kraftwerk.core.exceptions.KraftwerkException;
import lombok.Getter;
import lombok.SneakyThrows;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

//This class reproduces Genesis expected behaviour for functionnal tests
@Getter
public class GenesisClientStub extends GenesisClient {

    List<SurveyUnitUpdateLatest> mongoStub;
    Map<QuestionnaireIdModeTuple, MetadataModel> metadataCollectionStub;

    public GenesisClientStub(ConfigProperties configProperties) {
        super(configProperties);
        this.mongoStub = new ArrayList<>();
        this.metadataCollectionStub = new HashMap<>();
    }

    @Override
    public String pingGenesis(){
        return "";
    }

    @Override
    public List<InterrogationId> getInterrogationIds(String questionnaireId) {
        List<InterrogationId> list = new ArrayList<>();

        List<SurveyUnitUpdateLatest> filteredMongo = mongoStub.stream().filter(
                surveyUnitUpdateLatest -> surveyUnitUpdateLatest.getCollectionInstrumentId().equals(questionnaireId)
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
    public List<Mode> getModesByQuestionnaire(String questionnaireModelId) throws KraftwerkException {
        Set<Mode> set = new HashSet<>();

        List<SurveyUnitUpdateLatest> filteredMongo = mongoStub.stream().filter(
                surveyUnitUpdateLatest -> surveyUnitUpdateLatest.getCollectionInstrumentId().equals(questionnaireModelId)
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
                        surveyUnitUpdateLatest -> surveyUnitUpdateLatest.getCollectionInstrumentId().equals(questionnaireId)
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
            set.add(doc.getCollectionInstrumentId());
        }

        return set.stream().toList();
    }

    @Override
    public String getQuestionnaireModelIdByInterrogationId(String interrogationId) throws KraftwerkException {
        List<SurveyUnitUpdateLatest> filteredMongo = mongoStub.stream().filter(
                surveyUnitUpdateLatest -> surveyUnitUpdateLatest.getInterrogationId().equals(interrogationId)
        ).toList();

        return filteredMongo.isEmpty() ? null : filteredMongo.getFirst().getCollectionInstrumentId();
    }

    @Override
    public MetadataModel getMetadataByQuestionnaireIdAndMode(String questionnaireId, Mode mode) throws KraftwerkException {
        List<Map.Entry<QuestionnaireIdModeTuple,MetadataModel>> entries =
                metadataCollectionStub.entrySet().stream().filter(
                entry -> entry.getKey().questionnaireId().equals(questionnaireId)
                && entry.getKey().mode().equals(mode)
        ).toList();
        if(entries.isEmpty()){
            throw new KraftwerkException(404, "Metadata not found in stub");
        }
        return entries.getFirst().getValue();
    }

    @Override
    @SneakyThrows
    public void saveMetadata(String questionnaireId, Mode mode, MetadataModel metadataModel){
        //Remove already existing entries
        List<Map.Entry<QuestionnaireIdModeTuple,MetadataModel>> entries =
                metadataCollectionStub.entrySet().stream().filter(
                        entry -> entry.getKey().questionnaireId().equals(questionnaireId)
                                && entry.getKey().mode().equals(mode)
                ).toList();
        for(Map.Entry<QuestionnaireIdModeTuple,MetadataModel> entry : entries){
            metadataCollectionStub.remove(entry.getKey());
        }

        metadataCollectionStub.put(new QuestionnaireIdModeTuple(questionnaireId, mode), metadataModel);
    }
}
