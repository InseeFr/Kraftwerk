package fr.insee.kraftwerk.core.dataprocessing;

import fr.insee.kraftwerk.core.KraftwerkError;
import fr.insee.kraftwerk.core.metadata.CalculatedVariables;
import fr.insee.kraftwerk.core.metadata.CalculatedVariables.CalculatedVariable;
import fr.insee.kraftwerk.core.metadata.MetadataModel;
import fr.insee.kraftwerk.core.metadata.Variable;
import fr.insee.kraftwerk.core.metadata.VariableType;
import fr.insee.kraftwerk.core.utils.files.FileSystemImpl;
import fr.insee.kraftwerk.core.utils.files.FileUtilsInterface;
import fr.insee.kraftwerk.core.vtl.VtlBindings;
import fr.insee.kraftwerk.core.vtl.VtlScript;
import fr.insee.vtl.model.Dataset;
import fr.insee.vtl.model.Dataset.Role;
import fr.insee.vtl.model.InMemoryDataset;
import fr.insee.vtl.model.Structured;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CalculatedProcessingTest {

    private static CalculatedVariables fooCalculated;
    private static MetadataModel fooMetadataModel;
    private static VtlBindings vtlBindings;
    private static List<KraftwerkError> errors;
    private final FileUtilsInterface fileUtilsInterface = new FileSystemImpl();

    @BeforeAll
    static void setFooCalculated() {
        //
        fooCalculated = new CalculatedVariables();
        fooCalculated.putVariable(
                new CalculatedVariable("FOO1", "FOO2 + FOO3", List.of("FOO3", "FOO2")));
        fooCalculated.putVariable(
                new CalculatedVariable("FOO2", "FOO3", List.of("FOO3")));
        fooCalculated.putVariable(
                new CalculatedVariable("FOO3", "1"));
        //
        fooMetadataModel = new MetadataModel();
        fooMetadataModel.getVariables().putVariable(new Variable("FOO", fooMetadataModel.getRootGroup(), VariableType.STRING));
        fooMetadataModel.getVariables().putVariable(new Variable("FOO1", fooMetadataModel.getRootGroup(), VariableType.STRING));
        fooMetadataModel.getVariables().putVariable(new Variable("FOO2", fooMetadataModel.getRootGroup(), VariableType.STRING));
        fooMetadataModel.getVariables().putVariable(new Variable("FOO3", fooMetadataModel.getRootGroup(), VariableType.STRING));
        //
        vtlBindings = new VtlBindings();
        errors = new ArrayList<>();
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
        VtlBindings vtlBindings = getVtlBindings();
        //
        CalculatedProcessing processing = new CalculatedProcessing(vtlBindings, fooCalculated, fileUtilsInterface);
        processing.applyAutomatedVtlInstructions("TEST", errors);
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
                        new Structured.Component("FOO", String.class, Role.MEASURE))
        );
        VtlBindings vtlBindings = new VtlBindings();
        vtlBindings.put("TEST", fooDataset);
        return vtlBindings;
    }

}
