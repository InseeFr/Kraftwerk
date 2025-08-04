package fr.insee.kraftwerk.core.dataprocessing;

import fr.insee.kraftwerk.core.vtl.VtlScript;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LunaticDataProcessingTest {

    @Test
    void getStepName_Test() {
        LunaticDataProcessing lunaticDataProcessing = new LunaticDataProcessing(null, null, null);
        assertEquals("Lunatic", lunaticDataProcessing.getStepName());
    }

    @Test
    void generateVtlInstructions_Test() {
        LunaticDataProcessing lunaticDataProcessing = new LunaticDataProcessing(null, null, null);
        String param = "aaa";
        VtlScript retour = lunaticDataProcessing.generateVtlInstructions(param);
        assertEquals(1, retour.size());
        assertEquals(param + " := union(" + param + "," + param + ");", retour.getFirst());
    }

}
