package fr.insee.kraftwerk.core.metadata;

import fr.insee.bpm.metadata.model.Group;
import fr.insee.bpm.metadata.model.MetadataModel;
import fr.insee.bpm.metadata.model.VariableType;
import fr.insee.kraftwerk.core.Constants;
import fr.insee.kraftwerk.core.data.model.Mode;
import fr.insee.kraftwerk.core.data.model.SurveyMetadata;
import fr.insee.kraftwerk.core.data.model.SurveyMetadataVariable;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;

class MetadataUtilsGenesisTest {
    @Test
    void getMetadataFromGenesisSurveyMetadata_test_root_group(){
        //GIVEN
        final String VAR_1 = "var1";

        SurveyMetadata surveyMetadata = SurveyMetadata.builder()
                .campaignId("TESTCAMPAIGN")
                .questionnaireId("TESTQUEST")
                .mode(Mode.WEB)
                .variableDocumentMap(new LinkedHashMap<>())
                .build();

        SurveyMetadataVariable surveyMetadataVariable = SurveyMetadataVariable.builder()
                .name(VAR_1)
                .type(VariableType.STRING)
                .group(new Group(Constants.ROOT_GROUP_NAME))
                .isInQuestionGrid(false)
                .maxLengthData(500)
                .sasFormat("STRING")
                .questionName("QUESTIONNAME")
                .build();

        surveyMetadata.variableDocumentMap().put(VAR_1,surveyMetadataVariable);

        //WHEN
        MetadataModel metadataModel = MetadataUtilsGenesis.getMetadataFromGenesisSurveyMetadata(surveyMetadata);

        //THEN
        Assertions.assertThat(metadataModel).isNotNull();
        Assertions.assertThat(metadataModel.getGroups()).isNotNull().hasSize(1);
        Assertions.assertThat(metadataModel.getVariables()).isNotNull();
        Assertions.assertThat(metadataModel.getVariables().getVariables()).isNotNull().hasSize(1).containsKey(VAR_1);
        Assertions.assertThat(metadataModel.getVariables().getVariable(VAR_1)).isNotNull();
        Assertions.assertThat(metadataModel.getVariables().getVariable(VAR_1).getName()).isEqualTo(VAR_1);
        Assertions.assertThat(metadataModel.getVariables().getVariable(VAR_1).getType()).isEqualTo(VariableType.STRING);
        Assertions.assertThat(metadataModel.getVariables().getVariable(VAR_1).getGroupName()).isEqualTo(Constants.ROOT_GROUP_NAME);
        Assertions.assertThat(metadataModel.getVariables().getVariable(VAR_1).isInQuestionGrid()).isFalse();
        Assertions.assertThat(metadataModel.getVariables().getVariable(VAR_1).getMaxLengthData()).isEqualTo(500);
        Assertions.assertThat(metadataModel.getVariables().getVariable(VAR_1).getSasFormat()).isEqualTo("STRING");
        Assertions.assertThat(metadataModel.getVariables().getVariable(VAR_1).getQuestionName()).isEqualTo("QUESTIONNAME");
    }

    @Test
    void getMetadataFromGenesisSurveyMetadata_test_new_group(){
        //GIVEN
        final String VAR_2 = "var2";
        SurveyMetadata surveyMetadata = SurveyMetadata.builder()
                .campaignId("TESTCAMPAIGN")
                .questionnaireId("TESTQUEST")
                .mode(Mode.WEB)
                .variableDocumentMap(new LinkedHashMap<>())
                .build();

        SurveyMetadataVariable surveyMetadataVariable = SurveyMetadataVariable.builder()
                .name(VAR_2)
                .type(VariableType.STRING)
                .group(new Group("GROUP1", Constants.ROOT_GROUP_NAME))
                .isInQuestionGrid(false)
                .maxLengthData(500)
                .sasFormat("STRING")
                .questionName("QUESTIONNAME")
                .build();

        surveyMetadata.variableDocumentMap().put(VAR_2,surveyMetadataVariable);

        //WHEN
        MetadataModel metadataModel = MetadataUtilsGenesis.getMetadataFromGenesisSurveyMetadata(surveyMetadata);

        //THEN
        Assertions.assertThat(metadataModel).isNotNull();
        Assertions.assertThat(metadataModel.getGroups()).isNotNull().hasSize(2);
        Assertions.assertThat(metadataModel.getVariables()).isNotNull();
        Assertions.assertThat(metadataModel.getVariables().getVariables()).isNotNull().hasSize(1).containsKey(VAR_2);
        Assertions.assertThat(metadataModel.getVariables().getVariable(VAR_2)).isNotNull();
        Assertions.assertThat(metadataModel.getVariables().getVariable(VAR_2).getName()).isEqualTo(VAR_2);
        Assertions.assertThat(metadataModel.getVariables().getVariable(VAR_2).getType()).isEqualTo(VariableType.STRING);
        Assertions.assertThat(metadataModel.getVariables().getVariable(VAR_2).getGroupName()).isEqualTo("GROUP1");
        Assertions.assertThat(metadataModel.getVariables().getVariable(VAR_2).isInQuestionGrid()).isFalse();
        Assertions.assertThat(metadataModel.getVariables().getVariable(VAR_2).getMaxLengthData()).isEqualTo(500);
        Assertions.assertThat(metadataModel.getVariables().getVariable(VAR_2).getSasFormat()).isEqualTo("STRING");
        Assertions.assertThat(metadataModel.getVariables().getVariable(VAR_2).getQuestionName()).isEqualTo("QUESTIONNAME");
    }
}
