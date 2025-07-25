package fr.insee.kraftwerk.core.vtl;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class VtlScriptTest {

    @Test
    void constructor1_test() {
        VtlScript array = new VtlScript("first_instruction");
        assertEquals("first_instruction", array.getFirst());
    }

    @Test
    void constructor2_test() {
        VtlScript array = new VtlScript("first_instruction", "2_instruction", "3_instruction", "4_instruction");
        assertEquals(4, array.size());
        assertEquals("first_instruction", array.get(0));
        assertEquals("2_instruction", array.get(1));
        assertEquals("3_instruction", array.get(2));
        assertEquals("4_instruction", array.get(3));

    }

}
