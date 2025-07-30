package fr.insee.kraftwerk.core.dataprocessing;

import fr.insee.kraftwerk.core.vtl.VtlScript;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class XformsDataProcessingTest {

    @Test
    void getStepName() {
        //Constructor arguments have no importance here : thus, we set all to "null".
        XformsDataProcessing xformsDataProcessing = new XformsDataProcessing(null, null, null);
        String stepName = xformsDataProcessing.getStepName();
        assertEquals("Coleman", stepName);
    }

    @Test
    void generateVtlInstructions() {
        //Constructor arguments have no importance here : thus, we set all to "null".
        XformsDataProcessing xformsDataProcessing = new XformsDataProcessing(null, null, null);
        VtlScript vtlScript = xformsDataProcessing.generateVtlInstructions("aaa");
        assertEquals("aaa := union(aaa,aaa);", vtlScript.getFirst());
    }


}
