package fr.insee.kraftwerk.core.dataprocessing;

import fr.insee.bpm.metadata.model.CalculatedVariables;
import fr.insee.bpm.metadata.model.MetadataModel;
import fr.insee.bpm.metadata.model.Variable;
import fr.insee.bpm.metadata.model.VariableType;
import fr.insee.kraftwerk.core.utils.files.FileSystemImpl;
import fr.insee.kraftwerk.core.utils.files.FileUtilsInterface;
import fr.insee.kraftwerk.core.utils.log.KraftwerkExecutionContext;
import fr.insee.kraftwerk.core.vtl.VtlBindings;
import fr.insee.kraftwerk.core.vtl.VtlScript;
import fr.insee.vtl.model.Dataset;
import fr.insee.vtl.model.Dataset.Role;
import fr.insee.vtl.model.InMemoryDataset;
import fr.insee.vtl.model.Structured;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CalculatedProcessingTest {

    private static CalculatedVariables fooCalculated;
    private static MetadataModel fooMetadataModel;
    private static VtlBindings vtlBindings;
    private static KraftwerkExecutionContext kraftwerkExecutionContext;
    private final FileUtilsInterface fileUtilsInterface = new FileSystemImpl();

    @BeforeAll
    static void setFooCalculated() {
        //
        fooCalculated = new CalculatedVariables();
        fooCalculated.putVariable(
                new CalculatedVariables.CalculatedVariable("FOO1", "FOO2 + FOO3", List.of("FOO3", "FOO2")));
        fooCalculated.putVariable(
                new CalculatedVariables.CalculatedVariable("FOO2", "FOO3", List.of("FOO3")));
        fooCalculated.putVariable(
                new CalculatedVariables.CalculatedVariable("FOO3", "1"));
        //
        fooMetadataModel = new MetadataModel();
        fooMetadataModel.getVariables().putVariable(new Variable("FOO", fooMetadataModel.getRootGroup(), VariableType.STRING));
        fooMetadataModel.getVariables().putVariable(new Variable("FOO1", fooMetadataModel.getRootGroup(), VariableType.STRING));
        fooMetadataModel.getVariables().putVariable(new Variable("FOO2", fooMetadataModel.getRootGroup(), VariableType.STRING));
        fooMetadataModel.getVariables().putVariable(new Variable("FOO3", fooMetadataModel.getRootGroup(), VariableType.STRING));
        //
        vtlBindings = new VtlBindings();
        kraftwerkExecutionContext = new KraftwerkExecutionContext();
    }

    @Test
    void testIfCalculatedAreCorrectlyResolved() {
        //
        CalculatedProcessing processing = new CalculatedProcessing(vtlBindings, fooCalculated, fileUtilsInterface);
        VtlScript vtlScript = processing.generateVtlInstructions("TEST");

        //
        assertTrue(vtlScript.get(0).contains("FOO3"));
        assertFalse(vtlScript.get(0).contains("FOO2"));
        assertTrue(vtlScript.get(1).contains("FOO2"));
        assertFalse(vtlScript.get(1).contains("FOO1"));
        assertTrue(vtlScript.get(2).contains("FOO1"));
    }

    @Test
    void testIfCalculatedAreProcessed() {
        //
        vtlBindings = getVtlBindings();
        //
        CalculatedProcessing processing = new CalculatedProcessing(vtlBindings, fooCalculated, fileUtilsInterface);
        processing.applyAutomatedVtlInstructions("TEST", kraftwerkExecutionContext);
        //
        Dataset outDataset = vtlBindings.getDataset("TEST");

        //
        assertNotNull(outDataset);
        //
        assertTrue(outDataset.getDataStructure().containsKey("FOO1"));
        assertTrue(outDataset.getDataStructure().containsKey("FOO2"));
        assertTrue(outDataset.getDataStructure().containsKey("FOO3"));
        //
        assertEquals(1L, outDataset.getDataPoints().get(0).get("FOO3"));
        assertEquals(1L, outDataset.getDataPoints().get(0).get("FOO2"));
        assertEquals(2L, outDataset.getDataPoints().get(0).get("FOO1"));
    }

    @NotNull
    private static VtlBindings getVtlBindings() {
        Dataset fooDataset = new InMemoryDataset(
                List.of(
                        List.of("A","X"),
                        List.of("B","Y"),
                        List.of("C","Z")),
                List.of(
                        new Structured.Component("ID", String.class, Role.IDENTIFIER),
                        new Structured.Component("FOO", String.class, Role.MEASURE)));

        VtlBindings vtlBindings = new VtlBindings();
        vtlBindings.put("TEST", fooDataset);
        return vtlBindings;
    }

}
