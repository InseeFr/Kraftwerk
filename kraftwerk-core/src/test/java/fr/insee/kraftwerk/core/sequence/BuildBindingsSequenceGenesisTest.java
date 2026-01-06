package fr.insee.kraftwerk.core.sequence;

import fr.insee.kraftwerk.core.Constants;
import fr.insee.kraftwerk.core.TestConstants;
import fr.insee.kraftwerk.core.data.model.Mode;
import fr.insee.kraftwerk.core.data.model.SurveyUnitUpdateLatest;
import fr.insee.kraftwerk.core.data.model.VariableModel;
import fr.insee.bpm.metadata.model.Group;
import fr.insee.bpm.metadata.model.MetadataModel;
import fr.insee.bpm.metadata.model.Variable;
import fr.insee.bpm.metadata.model.VariableType;
import fr.insee.kraftwerk.core.utils.files.FileSystemImpl;
import fr.insee.kraftwerk.core.vtl.VtlBindings;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertThrows;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class BuildBindingsSequenceGenesisTest {
    private static final List<SurveyUnitUpdateLatest> surveyUnits = new ArrayList<>();

    private static LocalDateTime validationDate;

    @BeforeAll
    static void init(){
        String dataMode = "WEB";
        validationDate = LocalDateTime.now();
        //Create one document
        surveyUnits.clear();
        SurveyUnitUpdateLatest surveyUnitUpdateLatest = new SurveyUnitUpdateLatest();
        surveyUnitUpdateLatest.setInterrogationId("0000001");
        surveyUnitUpdateLatest.setCampaignId("TESTCAMPAIGN");
        surveyUnitUpdateLatest.setCollectionInstrumentId("TESTQUEST");
        surveyUnitUpdateLatest.setUsualSurveyUnitId("TESTIDUE");
        surveyUnitUpdateLatest.setValidationDate(validationDate);
        surveyUnitUpdateLatest.setQuestionnaireState("FINISHED");
        surveyUnitUpdateLatest.setMode(Mode.valueOf(dataMode));

        surveyUnitUpdateLatest.setCollectedVariables(new ArrayList<>());
        VariableModel variableModel = new VariableModel();
        variableModel.setVarId("COLLVAR1");
        variableModel.setScope(Constants.ROOT_GROUP_NAME);
        variableModel.setIteration(1);
        variableModel.setValue("TEST1");
        surveyUnitUpdateLatest.getCollectedVariables().add(variableModel);
        variableModel = new VariableModel();
        variableModel.setVarId("COLLVAR2");
        variableModel.setScope("TESTLOOP_1");
        variableModel.setIteration(1);
        variableModel.setIdParent(Constants.ROOT_GROUP_NAME);
        variableModel.setValue("TEST3");
        surveyUnitUpdateLatest.getCollectedVariables().add(variableModel);

        surveyUnitUpdateLatest.setExternalVariables(new ArrayList<>());
        VariableModel externalVariable = new VariableModel();
        externalVariable.setVarId("EXTVAR1");
        externalVariable.setScope(Constants.ROOT_GROUP_NAME);
        externalVariable.setIteration(1);
        externalVariable.setValue("TEST2");
        surveyUnitUpdateLatest.getExternalVariables().add(externalVariable);
        VariableModel externalVariableInLoop1 = new VariableModel();
        externalVariableInLoop1.setVarId("EXTVAR2");
        externalVariableInLoop1.setScope("TESTLOOP_1");
        externalVariableInLoop1.setIteration(1);
        externalVariableInLoop1.setIdParent(Constants.ROOT_GROUP_NAME);
        externalVariableInLoop1.setValue("VALUE_1");
        surveyUnitUpdateLatest.getExternalVariables().add(externalVariableInLoop1);
        VariableModel externalVariableInLoop2 = new VariableModel();
        externalVariableInLoop2.setVarId("EXTVAR2");
        externalVariableInLoop2.setScope("TESTLOOP_2");
        externalVariableInLoop2.setIteration(1);
        externalVariableInLoop2.setIdParent(Constants.ROOT_GROUP_NAME);
        externalVariableInLoop2.setValue("VALUE_2");
        surveyUnitUpdateLatest.getExternalVariables().add(externalVariableInLoop2);

        surveyUnits.add(surveyUnitUpdateLatest);
    }

    @Test
    void buildVtlBindings_errorWithoutMetadata(){
        //GIVEN
        String dataMode = "WEB";

        VtlBindings vtlBindings = new VtlBindings();
        BuildBindingsSequenceGenesis bbsg = new BuildBindingsSequenceGenesis(new FileSystemImpl(TestConstants.TEST_RESOURCES_DIRECTORY));
        //WHEN + THEN
        assertThrows(NullPointerException.class, () -> bbsg.buildVtlBindings(dataMode, vtlBindings, null, surveyUnits, null));

    }

    @Test
    void buildVtlBindings_success(){
        //GIVEN
        String dataMode = "WEB";

        VtlBindings vtlBindings = new VtlBindings();
        BuildBindingsSequenceGenesis bbsg = new BuildBindingsSequenceGenesis(new FileSystemImpl(TestConstants.TEST_RESOURCES_DIRECTORY));
        MetadataModel metadata = new MetadataModel();
        metadata.getVariables().putVariable(new Variable("COLLVAR1", metadata.getRootGroup(), VariableType.STRING));
        Group group = new Group("TESTLOOP",Constants.ROOT_GROUP_NAME);
        metadata.getVariables().putVariable(new Variable("COLLVAR2", group, VariableType.STRING));
        metadata.getVariables().putVariable(new Variable("EXTVAR1", metadata.getRootGroup(), VariableType.STRING));
        metadata.getVariables().putVariable(new Variable("EXTVAR2", group, VariableType.STRING));
        metadata.getGroups().put("TESTLOOP", group);
        Map<String, MetadataModel> modeMetadataMap = new HashMap<>();
        modeMetadataMap.put(dataMode,metadata);

        //WHEN + THEN
        assertDoesNotThrow(() -> bbsg.buildVtlBindings(dataMode, vtlBindings, modeMetadataMap, surveyUnits,
                Path.of(TestConstants.UNIT_TESTS_DIRECTORY, "genesis")));
        Assertions.assertThat(vtlBindings.getDataset("WEB").getDataAsMap().getFirst()).containsKey(Constants.SURVEY_UNIT_IDENTIFIER_NAME);
        Assertions.assertThat(vtlBindings.getDataset("WEB").getDataAsMap().getFirst()).containsEntry(Constants.SURVEY_UNIT_IDENTIFIER_NAME, "TESTIDUE");
        Assertions.assertThat(vtlBindings.getDataset("WEB").getDataAsMap().getFirst()).containsEntry(Constants.QUESTIONNAIRE_STATE_NAME, "FINISHED");
        Assertions.assertThat(vtlBindings.getDataset("WEB").getDataAsMap().getFirst()).containsEntry(Constants.VALIDATION_DATE_NAME, validationDate
                .format(DateTimeFormatter.ofPattern(Constants.VALIDATION_DATE_FORMAT)));
    }
}
