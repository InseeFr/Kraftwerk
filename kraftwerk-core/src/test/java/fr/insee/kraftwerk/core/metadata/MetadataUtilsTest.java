package fr.insee.kraftwerk.core.metadata;

import fr.insee.bpm.metadata.model.Group;
import fr.insee.bpm.metadata.model.McqVariable;
import fr.insee.bpm.metadata.model.MetadataModel;
import fr.insee.bpm.metadata.model.Variable;
import fr.insee.bpm.metadata.model.VariableType;
import fr.insee.kraftwerk.core.Constants;
import fr.insee.kraftwerk.core.TestConstants;
import fr.insee.kraftwerk.core.inputs.ModeInputs;
import fr.insee.kraftwerk.core.utils.files.FileSystemImpl;
import fr.insee.kraftwerk.core.utils.files.FileUtilsInterface;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

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


    @Test
    void getMetadataFromLunatic() {
        Map<String, ModeInputs> modeInputsMap = new HashMap<>();
        //PREDICATE FOR THAT TEST : "modeInputs.getLunaticFile()" must not be null & the file must exist!
        ModeInputs modeInputs1 = new ModeInputs();
        modeInputs1.setLunaticFile(Path.of(TestConstants.TEST_RESOURCES_DIRECTORY, "unit_tests", "lunatic", "log2021x21_web.json"));
        modeInputsMap.put("aaa", modeInputs1);
        ModeInputs modeInputs2 = new ModeInputs();
        modeInputs2.setLunaticFile(Path.of(TestConstants.TEST_RESOURCES_DIRECTORY, "unit_tests", "lunatic", "log2021x21_web.json")); //same file provided twice
        modeInputsMap.put("bbb", modeInputs2);

        FileUtilsInterface fileUtilsInterface = new FileSystemImpl(TestConstants.TEST_RESOURCES_DIRECTORY);

        Map<String, MetadataModel> metadataModels = MetadataUtils.getMetadataFromLunatic(modeInputsMap, fileUtilsInterface);

        //There must be same number of metadataModels than modeInputsMap.size() provided in input parameter
        assertEquals(2, metadataModels.size());
    }


    @Test
    void getMetadata() {
        Map<String, ModeInputs> modeInputsMap = new HashMap<>();
        //PREDICATE FOR THAT TEST : "modeInputs.getDdiUrl()" must not be null & the DDI file must exist!
        ModeInputs modeInputs1 = new ModeInputs();
        modeInputs1.setDdiUrl(Path.of(TestConstants.TEST_RESOURCES_DIRECTORY, "unit_tests", "ddi", "vqs-2021-x00-fo-ddi.xml").toString());
        modeInputsMap.put("aaa", modeInputs1);

        FileUtilsInterface fileUtilsInterface = new FileSystemImpl(TestConstants.TEST_RESOURCES_DIRECTORY);

        Map<String, MetadataModel> metadataModels = MetadataUtils.getMetadata(modeInputsMap, fileUtilsInterface);

        //There must be same number of metadataModels than modeInputsMap.size() provided in input parameter
        assertEquals(1, metadataModels.size());
        assertEquals(32, metadataModels.get("aaa").getVariables().getVariables().size());
        assertNull(metadataModels.get("aaa").getGroup("REPORTINGDATA"));
    }


    @Test
    void getMetadata_with_lunaticAndReportingdataFiles() {
        Map<String, ModeInputs> modeInputsMap = new HashMap<>();
        //PREDICATE FOR THAT TEST : "modeInputs.getDdiUrl()" must not be null & the DDI file must exist!
        ModeInputs modeInputs1 = new ModeInputs();
        modeInputs1.setDdiUrl(Path.of(TestConstants.TEST_RESOURCES_DIRECTORY, "unit_tests", "ddi", "vqs-2021-x00-fo-ddi.xml").toString());
        modeInputs1.setLunaticFile(Path.of(TestConstants.TEST_RESOURCES_DIRECTORY, "unit_tests", "lunatic", "log2021x21_web.json"));
        modeInputs1.setReportingDataFile(Path.of(TestConstants.TEST_RESOURCES_DIRECTORY, "unit_tests", "reportingdata", "reportingdata.xml"));
        modeInputsMap.put("aaa", modeInputs1);

        FileUtilsInterface fileUtilsInterface = new FileSystemImpl(TestConstants.TEST_RESOURCES_DIRECTORY);

        Map<String, MetadataModel> metadataModels = MetadataUtils.getMetadata(modeInputsMap, fileUtilsInterface);

        //There must be same number of metadataModels than modeInputsMap.size() provided in input parameter
        assertEquals(1, metadataModels.size());
        assertEquals(269, metadataModels.get("aaa").getVariables().getVariables().size());
        assertNotNull(metadataModels.get("aaa").getGroup("REPORTINGDATA"));
    }


}
