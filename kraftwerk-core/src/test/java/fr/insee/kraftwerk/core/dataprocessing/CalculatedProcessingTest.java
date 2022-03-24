package fr.insee.kraftwerk.core.dataprocessing;

import fr.insee.kraftwerk.core.TestConstants;
import fr.insee.kraftwerk.core.metadata.*;
import fr.insee.kraftwerk.core.vtl.VtlBindings;
import fr.insee.kraftwerk.core.vtl.VtlScript;
import fr.insee.vtl.model.Dataset;
import fr.insee.vtl.model.InMemoryDataset;
import fr.insee.vtl.model.Structured;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;

import static fr.insee.kraftwerk.core.metadata.CalculatedVariables.CalculatedVariable;
import static fr.insee.vtl.model.Dataset.Role;
import static org.junit.jupiter.api.Assertions.*;

public class CalculatedProcessingTest {

    private static CalculatedVariables fooCalculated;
    private static VariablesMap fooVariables;
    private static VtlBindings vtlBindings;

    static final String LUNATIC_SAMPLES = TestConstants.UNIT_TESTS_DIRECTORY + "/lunatic";

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
        fooVariables = new VariablesMap();
        fooVariables.putVariable(new Variable("FOO", fooVariables.getRootGroup(), VariableType.STRING));
        fooVariables.putVariable(new Variable("FOO1", fooVariables.getRootGroup(), VariableType.STRING));
        fooVariables.putVariable(new Variable("FOO2", fooVariables.getRootGroup(), VariableType.STRING));
        fooVariables.putVariable(new Variable("FOO3", fooVariables.getRootGroup(), VariableType.STRING));
        //
        vtlBindings = new VtlBindings();
    }

    @Test
    public void testIfCalculatedAreCorrectlyResolved() {
        //
        CalculatedProcessing processing = new CalculatedProcessing(vtlBindings);
        VtlScript vtlScript = processing.generateVtlInstructions("TEST", fooCalculated, fooVariables);

        //
        assertTrue(vtlScript.get(0).contains("FOO3"));
        assertFalse(vtlScript.get(0).contains("FOO2"));
        assertTrue(vtlScript.get(1).contains("FOO2"));
        assertFalse(vtlScript.get(1).contains("FOO1"));
        assertTrue(vtlScript.get(2).contains("FOO1"));
    }

    @Test
    public void testIfCalculatedAreProcessed() {
        //
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
        vtlBindings.getBindings().put("TEST", fooDataset);
        //
        CalculatedProcessing processing = new CalculatedProcessing(vtlBindings);
        processing.applyAutomatedVtlInstructions("TEST", fooCalculated, fooVariables);
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

    @Test
    public void processCalculatedOnLogX21Web() {
        //
        CalculatedVariables calculatedVariables = LunaticReader.getCalculatedFromLunatic(
                LUNATIC_SAMPLES + "/log2021x21_tel.json");

        //
        // TODO: maybe move this toward integration / functional tests (need data + DDI to be tested)
    }
}
