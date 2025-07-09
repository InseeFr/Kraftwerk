package fr.insee.kraftwerk.core.sequence;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ControlInputSequenceGenesisTest {

    @Test
    void constructeur_test() {
        ControlInputSequenceGenesis cisg = new ControlInputSequenceGenesis("/a/b/c/default_dir");
        assertEquals("/a/b/c/default_dir", cisg.defaultDirectory);
    }

    @Test
    void getSpecsDirectory_test() {
        ControlInputSequenceGenesis cisg = new ControlInputSequenceGenesis("/a/b/c/default_dir");
        assertEquals("\\a\\b\\c\\default_dir\\specs\\specsDir", cisg.getSpecsDirectory("specsDir").toString());
    }



}
