package fr.insee.kraftwerk.core.data.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ModeLabelTest {

    @Test
    void modeLabel_Test() {
        ModeLabel modeLabel = new ModeLabel();
        modeLabel.setMode("xyz");
        assertEquals("xyz", modeLabel.getMode());
    }

}
