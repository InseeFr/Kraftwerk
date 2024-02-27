package fr.insee.kraftwerk.core.dataprocessing;

import fr.insee.kraftwerk.core.KraftwerkError;
import fr.insee.kraftwerk.core.metadata.*;
import fr.insee.kraftwerk.core.vtl.VtlBindings;
import fr.insee.vtl.model.Dataset;
import fr.insee.vtl.model.InMemoryDataset;
import fr.insee.vtl.model.Structured;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GroupProcessingTest {

    @Test
    void addPrefixes() {
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
        List<KraftwerkError> errors = new ArrayList<>();
        VtlBindings vtlBindings = new VtlBindings();
        vtlBindings.put("TEST", initialDataset);
        //
        MetadataModel metadata = new MetadataModel();
        metadata.putGroup(new Group("DEPTH1", metadata.getRootGroup().getName()));
        metadata.putGroup(new Group("DEPTH2", "DEPTH1"));
        metadata.getVariables().putVariable(new Variable("FOO", metadata.getRootGroup(), VariableType.STRING));
        metadata.getVariables().putVariable(new Variable("FOO1", metadata.getGroup("DEPTH1"), VariableType.NUMBER));
        metadata.getVariables().putVariable(new Variable("FOO2", metadata.getGroup("DEPTH2"), VariableType.NUMBER));
        //
        new GroupProcessing(vtlBindings, metadata).applyAutomatedVtlInstructions("TEST",errors);
        Dataset outDataset = vtlBindings.getDataset("TEST");

        //
        assertEquals(
                Set.of("ID", "FOO", "DEPTH1.FOO1", "DEPTH1.DEPTH2.FOO2"),
                outDataset.getDataStructure().keySet());
    }
}
