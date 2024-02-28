package fr.insee.kraftwerk.core.metadata;

import fr.insee.kraftwerk.core.TestConstants;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class LunaticReaderTest {

    static final Path lunaticSamplesPath = Path.of(TestConstants.UNIT_TESTS_DIRECTORY, "lunatic");

    @Test
    void readLogX21TelLunaticFile() {
        //
        CalculatedVariables calculatedVariables = LunaticReader.getCalculatedFromLunatic(
                lunaticSamplesPath.resolve("log2021x21_tel.json"));

        //
        assertNotNull(calculatedVariables);
        //
        assertTrue(calculatedVariables.containsKey("AGE"));
        assertEquals(
                "if (FUTURANNIVERSAIRE) then cast((cast(AGEMILLESIME,integer) - 1),integer) else cast(AGEMILLESIME,integer)",
                calculatedVariables.getVtlExpression("AGE"));
        assertEquals(
                List.of("FUTURANNIVERSAIRE", "AGEMILLESIME", "DATENAIS"),
                calculatedVariables.getDependantVariables("AGE"));
        //
        assertTrue(calculatedVariables.containsKey("ANNEENQ"));
        assertEquals(
                "cast(current_date(),string,\"YYYY\")",
                calculatedVariables.getVtlExpression("ANNEENQ"));
        assertTrue(calculatedVariables.getDependantVariables("ANNEENQ").isEmpty());

    }
    
    @Test
    void readLogX22WebLunaticFile() {
        //
        CalculatedVariables calculatedVariables = LunaticReader.getCalculatedFromLunatic(
                lunaticSamplesPath.resolve("log2021x22_web.json"));

        //
        assertNotNull(calculatedVariables);
        assertTrue(calculatedVariables.containsKey("S2_MAA1AT"));
       
    }


    @Test
  //Same test  with DDI [2 groups, 463 variables]
    void readVariablesFromLogX21WebLunaticFile() {
        //
        MetadataModel variables = LunaticReader.getMetadataFromLunatic(
                lunaticSamplesPath.resolve("log2021x21_web.json"));

        //
        assertNotNull(variables);
        assertEquals(11,variables.getGroupsCount());
        assertEquals(683, variables.getVariables().getVariables().size());
       
    }
}
