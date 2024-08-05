package fr.insee.kraftwerk.core.sequence;

import fr.insee.kraftwerk.core.Constants;
import fr.insee.kraftwerk.core.TestConstants;
import fr.insee.kraftwerk.core.data.model.ExternalVariable;
import fr.insee.kraftwerk.core.data.model.Mode;
import fr.insee.kraftwerk.core.data.model.SurveyUnitUpdateLatest;
import fr.insee.kraftwerk.core.data.model.VariableState;
import fr.insee.bpm.metadata.model.Group;
import fr.insee.bpm.metadata.model.MetadataModel;
import fr.insee.bpm.metadata.model.Variable;
import fr.insee.bpm.metadata.model.VariableType;
import fr.insee.kraftwerk.core.utils.files.FileSystemImpl;
import fr.insee.kraftwerk.core.vtl.VtlBindings;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertThrows;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class BuildBIndingsSequenceGenesisTest {
    private static final List<SurveyUnitUpdateLatest> surveyUnits = new ArrayList<>();

    @BeforeAll
    static void init(){
        String dataMode = "WEB";
        //Create one document
        surveyUnits.clear();
        SurveyUnitUpdateLatest surveyUnitUpdateLatest = new SurveyUnitUpdateLatest();
        surveyUnitUpdateLatest.setIdUE("0000001");
        surveyUnitUpdateLatest.setIdCampaign("TESTCAMPAIGN");
        surveyUnitUpdateLatest.setIdQuest("TESTQUEST");
        surveyUnitUpdateLatest.setMode(Mode.valueOf(dataMode));

        surveyUnitUpdateLatest.setVariablesUpdate(new ArrayList<>());
        VariableState variableState = new VariableState();
        variableState.setIdVar("COLLVAR1");
        variableState.setIdLoop(Constants.ROOT_GROUP_NAME);
        variableState.setValues(new ArrayList<>());
        variableState.getValues().add("TEST1");
        surveyUnitUpdateLatest.getVariablesUpdate().add(variableState);
        variableState = new VariableState();
        variableState.setIdVar("COLLVAR2");
        variableState.setIdLoop("LOOP1");
        variableState.setIdParent(Constants.ROOT_GROUP_NAME);
        variableState.setValues(new ArrayList<>());
        variableState.getValues().add("TEST3");
        surveyUnitUpdateLatest.getVariablesUpdate().add(variableState);

        surveyUnitUpdateLatest.setExternalVariables(new ArrayList<>());
        ExternalVariable externalVariable = new ExternalVariable();
        externalVariable.setIdVar("EXTVAR1");
        externalVariable.setValues(new ArrayList<>());
        externalVariable.getValues().add("TEST2");
        surveyUnitUpdateLatest.getExternalVariables().add(externalVariable);

        surveyUnits.add(surveyUnitUpdateLatest);
    }

    @Test
    void buildVtlBindings_errorWithoutMetadata(){
        //GIVEN
        String dataMode = "WEB";

        VtlBindings vtlBindings = new VtlBindings();
        BuildBindingsSequenceGenesis bbsg = new BuildBindingsSequenceGenesis(new FileSystemImpl());
        //WHEN + THEN
        assertThrows(NullPointerException.class, () -> bbsg.buildVtlBindings(dataMode, vtlBindings, null, surveyUnits, null));

    }

    @Test
    void buildVtlBindings_success(){
        //GIVEN
        String dataMode = "WEB";

        VtlBindings vtlBindings = new VtlBindings();
        BuildBindingsSequenceGenesis bbsg = new BuildBindingsSequenceGenesis(new FileSystemImpl());
        MetadataModel metadata = new MetadataModel();
        metadata.getVariables().putVariable(new Variable("COLLVAR1", metadata.getRootGroup(), VariableType.STRING));
        Group group = new Group("TESTLOOP",Constants.ROOT_GROUP_NAME);
        metadata.getVariables().putVariable(new Variable("COLLVAR2", group, VariableType.STRING));
        metadata.getVariables().putVariable(new Variable("EXTVAR1", metadata.getRootGroup(), VariableType.STRING));
        metadata.getGroups().put("TESTLOOP", group);
        Map<String, MetadataModel> modeMetadataMap = new HashMap<>();
        modeMetadataMap.put(dataMode,metadata);

        //WHEN + THEN
        assertDoesNotThrow(() -> bbsg.buildVtlBindings(dataMode, vtlBindings, modeMetadataMap, surveyUnits,
                Path.of(TestConstants.UNIT_TESTS_DIRECTORY, "genesis")));
    }
}
