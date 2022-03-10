package fr.insee.kraftwerk;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.Paths;
import java.util.Arrays;

import static org.junit.Assert.*;

public class KraftwerkTest {

    @Test
    public void listDirectories() {
        File[] directories = new File(TestConstants.TEST_FUNCTIONAL_INPUT_DIRECTORY).listFiles(File::isDirectory);
        assertNotNull(directories);
        assertTrue(directories.length > 0);
        assertTrue(Arrays.stream(directories).sequential()
                .anyMatch(directory -> directory.getAbsolutePath().contains("VQS-2021-x00")));
    }
    
    @Test
    public void testTransformToOut() {
        Launcher launcher = new Launcher();
        assertEquals(Paths.get("C://Users/in/Kraftwerk/src/test/resources/functional_tests/out/VQS"),
                launcher.transformToOut(Paths.get("C://Users/in/Kraftwerk/src/test/resources/functional_tests/in/VQS")));
    }

    @Test
    public void testReadCampaignName() {
        assertEquals("VQS",
                Launcher.readCampaignName(Paths.get("C://Users/in/Kraftwerk/src/test/resources/functional_tests/in/VQS")));
    }

}
