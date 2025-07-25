package fr.insee.kraftwerk.core.sequence;

import org.junit.jupiter.api.Test;

import java.io.File;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ControlInputSequenceGenesisTest {

    //OS DEPENDENT!
    private static final String FILE_SEPARATOR = File.separator;

    @Test
    void constructor_test() {
        ControlInputSequenceGenesis cisg = new ControlInputSequenceGenesis("/a/b/c/default_dir");
        assertEquals("/a/b/c/default_dir", cisg.defaultDirectory);
    }

    @Test
    void getSpecsDirectory_test() {
        String[] pathNames = { "a", "b", "c", "default_dir", "specs", "specsDir" };
        String path = String.join(FILE_SEPARATOR, pathNames);
        ControlInputSequenceGenesis cisg = new ControlInputSequenceGenesis("/a/b/c/default_dir");
        assertEquals(FILE_SEPARATOR + path, cisg.getSpecsDirectory("specsDir").toString());
    }



}
