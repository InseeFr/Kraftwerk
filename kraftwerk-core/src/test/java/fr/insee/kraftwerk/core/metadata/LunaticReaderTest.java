package fr.insee.kraftwerk.core.metadata;

import fr.insee.kraftwerk.core.TestConstants;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class LunaticReaderTest {

    static final String LUNATIC_SAMPLES = TestConstants.UNIT_TESTS_DIRECTORY + "/lunatic";

    @Test
    public void readLogX21TelLunaticFile() {
        //
        CalculatedVariables calculatedVariables = LunaticReader.getCalculatedFromLunatic(
                LUNATIC_SAMPLES + "/log2021x21_tel.json");

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

    }
}
