package fr.insee.kraftwerk.core.metadata;

import fr.insee.bpm.metadata.model.Group;
import fr.insee.bpm.metadata.model.McqVariable;
import fr.insee.bpm.metadata.model.MetadataModel;
import fr.insee.bpm.metadata.model.Variable;
import fr.insee.bpm.metadata.model.VariableType;
import fr.insee.kraftwerk.core.Constants;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MetadataUtilsTest {

    @Test
    void addLunaticVariableTest() {

        MetadataModel metadata = new MetadataModel();
        metadata.putGroup(new Group("LOOP1"));
        McqVariable mcqVar = new McqVariable("QUESTION_GRID_VAR_11",metadata.getGroup("LOOP1"),"QUESTION_GRID1","Label 1 question grid");
        mcqVar.setInQuestionGrid(true);
        Variable varRoot = new Variable("TYPE_QUEST", metadata.getRootGroup(), VariableType.STRING);
        metadata.getVariables().putVariable(varRoot);
        metadata.getVariables().putVariable(mcqVar);

        MetadataUtils.addLunaticVariable(metadata,"FILTER_RESULT_QUESTION_GRID1", Constants.FILTER_RESULT_PREFIX, VariableType.BOOLEAN);
        MetadataUtils.addLunaticVariable(metadata,"FILTER_RESULT_TYPE_QUEST", Constants.FILTER_RESULT_PREFIX, VariableType.BOOLEAN);
        MetadataUtils.addLunaticVariable(metadata,"QUESTION_GRID1_MISSING", Constants.MISSING_SUFFIX, VariableType.STRING);
        MetadataUtils.addLunaticVariable(metadata,"TYPE_QUEST_MISSING", Constants.MISSING_SUFFIX, VariableType.STRING);
        MetadataUtils.addLunaticVariable(metadata,"COMMENT_QE", Constants.MISSING_SUFFIX, VariableType.STRING);

        assertEquals("LOOP1",metadata.getVariables().getVariable("FILTER_RESULT_QUESTION_GRID1").getGroup().getName());
        assertEquals(Constants.ROOT_GROUP_NAME,metadata.getVariables().getVariable("FILTER_RESULT_TYPE_QUEST").getGroup().getName());

        assertEquals("LOOP1",metadata.getVariables().getVariable("QUESTION_GRID1_MISSING").getGroup().getName());
        assertEquals(Constants.ROOT_GROUP_NAME,metadata.getVariables().getVariable("TYPE_QUEST_MISSING").getGroup().getName());

        assertEquals(Constants.ROOT_GROUP_NAME,metadata.getVariables().getVariable("COMMENT_QE").getGroup().getName());

    }
}
