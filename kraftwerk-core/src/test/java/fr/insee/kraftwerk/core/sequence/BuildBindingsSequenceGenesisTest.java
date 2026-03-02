package fr.insee.kraftwerk.core.sequence;

import fr.insee.bpm.metadata.model.Group;
import fr.insee.bpm.metadata.model.MetadataModel;
import fr.insee.bpm.metadata.model.Variable;
import fr.insee.bpm.metadata.model.VariableType;
import fr.insee.kraftwerk.core.Constants;
import fr.insee.kraftwerk.core.TestConstants;
import fr.insee.kraftwerk.core.data.model.DataState;
import fr.insee.kraftwerk.core.data.model.Mode;
import fr.insee.kraftwerk.core.data.model.SurveyUnitUpdateLatest;
import fr.insee.kraftwerk.core.data.model.VariableModel;
import fr.insee.kraftwerk.core.utils.KraftwerkExecutionContext;
import fr.insee.kraftwerk.core.utils.files.FileSystemImpl;
import fr.insee.kraftwerk.core.vtl.VtlBindings;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
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

    private static final String ROOT_COLLECTED_VARIABLE_NAME = "COLLVAR1";
    private static final String GROUP_COLLECTED_VARIABLE_NAME = "COLLVAR2";
    private static final String ROOT_EXTERNAL_VARIABLE_NAME = "EXTVAR1";
    private static final String GROUP_EXTERNAL_VARIABLE_NAME = "EXTVAR2";

    @BeforeEach
    void init(){
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

        //ROOT COLLECTED
        surveyUnitUpdateLatest.setCollectedVariables(new ArrayList<>());
        VariableModel variableModel = new VariableModel();
        variableModel.setVarId(ROOT_COLLECTED_VARIABLE_NAME);
        variableModel.setScope(Constants.ROOT_GROUP_NAME);
        variableModel.setIteration(1);
        variableModel.setValue("TEST1");
        variableModel.setState(DataState.COLLECTED);
        surveyUnitUpdateLatest.getCollectedVariables().add(variableModel);

        //GROUP COLLECTED
        variableModel = new VariableModel();
        variableModel.setVarId(GROUP_COLLECTED_VARIABLE_NAME);
        variableModel.setScope("TESTLOOP_1");
        variableModel.setIteration(1);
        variableModel.setIdParent(Constants.ROOT_GROUP_NAME);
        variableModel.setValue("TEST3");
        variableModel.setState(DataState.EDITED);
        surveyUnitUpdateLatest.getCollectedVariables().add(variableModel);

        //ROOT EXTERNAL
        surveyUnitUpdateLatest.setExternalVariables(new ArrayList<>());
        VariableModel externalVariable = new VariableModel();
        externalVariable.setVarId(ROOT_EXTERNAL_VARIABLE_NAME);
        externalVariable.setScope(Constants.ROOT_GROUP_NAME);
        externalVariable.setIteration(1);
        externalVariable.setValue("TEST2");
        externalVariable.setState(DataState.FORMATTED);
        surveyUnitUpdateLatest.getExternalVariables().add(externalVariable);

        //ROOT EXTERNALS
        VariableModel externalVariableInLoop1 = new VariableModel();
        externalVariableInLoop1.setVarId(GROUP_EXTERNAL_VARIABLE_NAME);
        externalVariableInLoop1.setScope("TESTLOOP_1");
        externalVariableInLoop1.setIteration(1);
        externalVariableInLoop1.setIdParent(Constants.ROOT_GROUP_NAME);
        externalVariableInLoop1.setValue("VALUE_1");
        externalVariableInLoop1.setState(DataState.COLLECTED);
        surveyUnitUpdateLatest.getExternalVariables().add(externalVariableInLoop1);

        VariableModel externalVariableInLoop2 = new VariableModel();
        externalVariableInLoop2.setVarId(GROUP_EXTERNAL_VARIABLE_NAME);
        externalVariableInLoop2.setScope("TESTLOOP_2");
        externalVariableInLoop2.setIteration(1);
        externalVariableInLoop2.setIdParent(Constants.ROOT_GROUP_NAME);
        externalVariableInLoop2.setValue("VALUE_2");
        externalVariableInLoop2.setState(DataState.EDITED);
        surveyUnitUpdateLatest.getExternalVariables().add(externalVariableInLoop2);

        surveyUnits.add(surveyUnitUpdateLatest);
    }

    @Test
    void buildVtlBindings_errorWithoutMetadata(){
        //GIVEN
        String dataMode = "WEB";

        VtlBindings vtlBindings = new VtlBindings();
        BuildBindingsSequenceGenesis bbsg = new BuildBindingsSequenceGenesis(
                new FileSystemImpl(TestConstants.TEST_RESOURCES_DIRECTORY),
                TestConstants.getKraftwerkExecutionContext()
        );
        //WHEN + THEN
        assertThrows(NullPointerException.class, () -> bbsg.buildVtlBindings(dataMode, vtlBindings, null, surveyUnits, null));

    }

    @Test
    void buildVtlBindings_success(){
        //GIVEN
        String dataMode = "WEB";

        VtlBindings vtlBindings = new VtlBindings();
        BuildBindingsSequenceGenesis bbsg = new BuildBindingsSequenceGenesis(
                new FileSystemImpl(TestConstants.TEST_RESOURCES_DIRECTORY),
                TestConstants.getKraftwerkExecutionContext()
        );
        MetadataModel metadata = new MetadataModel();
        metadata.getVariables().putVariable(new Variable(ROOT_COLLECTED_VARIABLE_NAME, metadata.getRootGroup(), VariableType.STRING));
        Group group = new Group("TESTLOOP",Constants.ROOT_GROUP_NAME);
        metadata.getVariables().putVariable(new Variable(GROUP_COLLECTED_VARIABLE_NAME, group, VariableType.STRING));
        metadata.getVariables().putVariable(new Variable(ROOT_EXTERNAL_VARIABLE_NAME, metadata.getRootGroup(), VariableType.STRING));
        metadata.getVariables().putVariable(new Variable(GROUP_EXTERNAL_VARIABLE_NAME, group, VariableType.STRING));
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

    @Test
    @DisplayName("The VTL dataset should have variable states in it")
    void buildVtlBindings_states(){
        //GIVEN
        String dataMode = "WEB";

        VtlBindings vtlBindings = new VtlBindings();
        KraftwerkExecutionContext kraftwerkExecutionContext = TestConstants.getKraftwerkExecutionContext();
        kraftwerkExecutionContext.setAddStates(true);
        BuildBindingsSequenceGenesis bbsg = new BuildBindingsSequenceGenesis(
                new FileSystemImpl(TestConstants.TEST_RESOURCES_DIRECTORY),
                kraftwerkExecutionContext
        );

        MetadataModel metadata = new MetadataModel();

        metadata.getVariables().putVariable(new Variable(ROOT_COLLECTED_VARIABLE_NAME, metadata.getRootGroup(), VariableType.STRING));
        Group group = new Group("TESTLOOP",Constants.ROOT_GROUP_NAME);
        metadata.getVariables().putVariable(new Variable(GROUP_COLLECTED_VARIABLE_NAME, group, VariableType.STRING));

        metadata.getVariables().putVariable(new Variable(ROOT_EXTERNAL_VARIABLE_NAME, metadata.getRootGroup(), VariableType.STRING));
        metadata.getVariables().putVariable(new Variable(GROUP_EXTERNAL_VARIABLE_NAME, group, VariableType.STRING));
        metadata.getGroups().put("TESTLOOP", group);
        Map<String, MetadataModel> modeMetadataMap = new HashMap<>();
        modeMetadataMap.put(dataMode,metadata);

        //WHEN + THEN
        assertDoesNotThrow(() -> bbsg.buildVtlBindings(dataMode, vtlBindings, modeMetadataMap, surveyUnits,
                Path.of(TestConstants.UNIT_TESTS_DIRECTORY, "genesis")));
        Assertions.assertThat(vtlBindings.getDataset("WEB").getDataAsMap()).hasSize(1);
        Map<String, Object> surveyUnitData = vtlBindings.getDataset("WEB").getDataAsMap().getFirst();
        Assertions.assertThat(surveyUnitData)
                .containsKey(ROOT_COLLECTED_VARIABLE_NAME + Constants.VARIABLE_STATE_SUFFIX_NAME)
                .containsEntry(
                        ROOT_COLLECTED_VARIABLE_NAME + Constants.VARIABLE_STATE_SUFFIX_NAME,
                        DataState.COLLECTED.toString()
                )
                .containsEntry(
                        GROUP_COLLECTED_VARIABLE_NAME + Constants.VARIABLE_STATE_SUFFIX_NAME,
                        DataState.EDITED.toString()
                )
                .containsEntry(
                        ROOT_EXTERNAL_VARIABLE_NAME + Constants.VARIABLE_STATE_SUFFIX_NAME,
                        DataState.FORMATTED.toString()
                )
                .containsEntry(
                        GROUP_EXTERNAL_VARIABLE_NAME + Constants.VARIABLE_STATE_SUFFIX_NAME,
                        DataState.EDITED.toString()
                );
    }

    @Test
    @DisplayName("State field should be not null and empty if no data (root variables)")
    void buildVtlBindings_handle_do_data_state_root(){
        //GIVEN
        String dataMode = "WEB";

        //Remove ROOT COLLECTED variable
        surveyUnits.getFirst().getCollectedVariables().removeIf(variableModel ->
                variableModel.getVarId().equals(ROOT_COLLECTED_VARIABLE_NAME));
        //Remove ROOT EXTERNAL variable
        surveyUnits.getFirst().getExternalVariables().removeIf(variableModel ->
                variableModel.getVarId().equals(ROOT_EXTERNAL_VARIABLE_NAME));

        VtlBindings vtlBindings = new VtlBindings();
        KraftwerkExecutionContext kraftwerkExecutionContext = TestConstants.getKraftwerkExecutionContext();
        kraftwerkExecutionContext.setAddStates(true);
        BuildBindingsSequenceGenesis bbsg = new BuildBindingsSequenceGenesis(
                new FileSystemImpl(TestConstants.TEST_RESOURCES_DIRECTORY),
                kraftwerkExecutionContext
        );

        MetadataModel metadata = new MetadataModel();

        metadata.getVariables().putVariable(new Variable(ROOT_COLLECTED_VARIABLE_NAME, metadata.getRootGroup(), VariableType.STRING));
        Group group = new Group("TESTLOOP",Constants.ROOT_GROUP_NAME);
        metadata.getVariables().putVariable(new Variable(GROUP_COLLECTED_VARIABLE_NAME, group, VariableType.STRING));

        metadata.getVariables().putVariable(new Variable(ROOT_EXTERNAL_VARIABLE_NAME, metadata.getRootGroup(), VariableType.STRING));
        metadata.getVariables().putVariable(new Variable(GROUP_EXTERNAL_VARIABLE_NAME, group, VariableType.STRING));
        metadata.getGroups().put("TESTLOOP", group);
        Map<String, MetadataModel> modeMetadataMap = new HashMap<>();
        modeMetadataMap.put(dataMode,metadata);

        //WHEN + THEN
        assertDoesNotThrow(() -> bbsg.buildVtlBindings(dataMode, vtlBindings, modeMetadataMap, surveyUnits,
                Path.of(TestConstants.UNIT_TESTS_DIRECTORY, "genesis")));
        Assertions.assertThat(vtlBindings.getDataset("WEB").getDataAsMap()).hasSize(1);
        Map<String, Object> surveyUnitData = vtlBindings.getDataset("WEB").getDataAsMap().getFirst();
        Assertions.assertThat(surveyUnitData)
                .containsEntry(
                        ROOT_COLLECTED_VARIABLE_NAME + Constants.VARIABLE_STATE_SUFFIX_NAME,
                        ""
                )
                .containsEntry(
                        ROOT_EXTERNAL_VARIABLE_NAME + Constants.VARIABLE_STATE_SUFFIX_NAME,
                        ""
                );
    }

    @Test
    @DisplayName("State field should be not null and empty if no data (group variables)")
    void buildVtlBindings_handle_do_data_state_group(){
        //GIVEN
        String dataMode = "WEB";

        //Remove GROUP COLLECTED variable
        surveyUnits.getFirst().getCollectedVariables().removeIf(variableModel ->
                variableModel.getVarId().equals(GROUP_COLLECTED_VARIABLE_NAME));
        //Remove GROUP EXTERNAL variable
        surveyUnits.getFirst().getExternalVariables().removeIf(variableModel ->
                variableModel.getVarId().equals(GROUP_EXTERNAL_VARIABLE_NAME));

        VtlBindings vtlBindings = new VtlBindings();
        KraftwerkExecutionContext kraftwerkExecutionContext = TestConstants.getKraftwerkExecutionContext();
        kraftwerkExecutionContext.setAddStates(true);
        BuildBindingsSequenceGenesis bbsg = new BuildBindingsSequenceGenesis(
                new FileSystemImpl(TestConstants.TEST_RESOURCES_DIRECTORY),
                kraftwerkExecutionContext
        );

        MetadataModel metadata = new MetadataModel();

        metadata.getVariables().putVariable(new Variable(ROOT_COLLECTED_VARIABLE_NAME, metadata.getRootGroup(), VariableType.STRING));
        Group group = new Group("TESTLOOP",Constants.ROOT_GROUP_NAME);
        metadata.getVariables().putVariable(new Variable(GROUP_COLLECTED_VARIABLE_NAME, group, VariableType.STRING));

        metadata.getVariables().putVariable(new Variable(ROOT_EXTERNAL_VARIABLE_NAME, metadata.getRootGroup(), VariableType.STRING));
        metadata.getVariables().putVariable(new Variable(GROUP_EXTERNAL_VARIABLE_NAME, group, VariableType.STRING));
        metadata.getGroups().put("TESTLOOP", group);
        Map<String, MetadataModel> modeMetadataMap = new HashMap<>();
        modeMetadataMap.put(dataMode,metadata);

        //WHEN + THEN
        assertDoesNotThrow(() -> bbsg.buildVtlBindings(dataMode, vtlBindings, modeMetadataMap, surveyUnits,
                Path.of(TestConstants.UNIT_TESTS_DIRECTORY, "genesis")));
        Assertions.assertThat(vtlBindings.getDataset("WEB").getDataAsMap()).hasSize(1);
        Map<String, Object> surveyUnitData = vtlBindings.getDataset("WEB").getDataAsMap().getFirst();
        Assertions.assertThat(surveyUnitData)
                .containsEntry(
                        GROUP_COLLECTED_VARIABLE_NAME + Constants.VARIABLE_STATE_SUFFIX_NAME,
                        ""
                )
                .containsEntry(
                        GROUP_EXTERNAL_VARIABLE_NAME + Constants.VARIABLE_STATE_SUFFIX_NAME,
                        ""
                );
    }
}
