package fr.insee.kraftwerk.core.dataprocessing;

import fr.insee.kraftwerk.core.metadata.Group;
import fr.insee.kraftwerk.core.metadata.Variable;
import fr.insee.kraftwerk.core.metadata.VariableType;
import fr.insee.kraftwerk.core.metadata.VariablesMap;
import fr.insee.kraftwerk.core.vtl.VtlBindings;
import fr.insee.vtl.model.Dataset;
import fr.insee.vtl.model.InMemoryDataset;
import fr.insee.vtl.model.Structured;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class GroupProcessingTest {

    @Test
    public void addPrefixes() {
        //
        Dataset initialDataset = new InMemoryDataset(
                List.of(
                        List.of("T01", "foo", 1L, 2L)
                ),
                List.of(
                        new Structured.Component("ID", String.class, Dataset.Role.IDENTIFIER),
                        new Structured.Component("FOO", String.class, Dataset.Role.MEASURE),
                        new Structured.Component("FOO1", Long.class, Dataset.Role.MEASURE),
                        new Structured.Component("FOO2", Long.class, Dataset.Role.MEASURE)
                )
        );
        VtlBindings vtlBindings = new VtlBindings();
        vtlBindings.getBindings().put("TEST", initialDataset);
        //
        VariablesMap variablesMap = new VariablesMap();
        variablesMap.putGroup(new Group("DEPTH1", variablesMap.getRootGroup().getName()));
        variablesMap.putGroup(new Group("DEPTH2", "DEPTH1"));
        variablesMap.putVariable(new Variable("FOO", variablesMap.getRootGroup(), VariableType.STRING));
        variablesMap.putVariable(new Variable("FOO1", variablesMap.getGroup("DEPTH1"), VariableType.NUMBER));
        variablesMap.putVariable(new Variable("FOO2", variablesMap.getGroup("DEPTH2"), VariableType.NUMBER));
        //
        new GroupProcessing(vtlBindings).applyAutomatedVtlInstructions("TEST", variablesMap);
        Dataset outDataset = vtlBindings.getDataset("TEST");

        //
        assertEquals(
                Set.of("ID", "FOO", "DEPTH1.FOO1", "DEPTH1.DEPTH2.FOO2"),
                outDataset.getDataStructure().keySet());
    }
}
