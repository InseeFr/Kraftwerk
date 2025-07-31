package fr.insee.kraftwerk.core.sequence;

import fr.insee.kraftwerk.core.exceptions.KraftwerkException;
import fr.insee.kraftwerk.core.utils.files.FileSystemImpl;
import fr.insee.kraftwerk.core.utils.files.FileUtilsInterface;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

class ControlInputSequenceTest {

    @Test
    void getInDirectory_present_on_paramPath_test(@TempDir Path tempDir) throws Exception {
        Path file = tempDir.resolve("kraftwerk.json");
        Files.write(file, "\n".getBytes());
        FileUtilsInterface fileUtils = new FileSystemImpl(tempDir.toString());
        ControlInputSequence cis = new ControlInputSequence(tempDir.toString(), fileUtils);
        assertEquals(tempDir, cis.getInDirectory(tempDir.toString()));
    }


    @Test
    void getInDirectory_present_on_inDirectory_test(@TempDir Path tempDir) throws Exception {
        final String campaignDirFolder = "CAMPAIGN-NAME-FOLDER";
        Files.createDirectories(Paths.get(tempDir.toString(),"in",campaignDirFolder));
        Path kraftwerkFile = Paths.get(tempDir.toString(),"in", campaignDirFolder, "kraftwerk.json");
        Files.write(kraftwerkFile, "\n".getBytes());

        FileUtilsInterface fileUtils = new FileSystemImpl(tempDir.toString());
        ControlInputSequence cis = new ControlInputSequence(tempDir.toString(), fileUtils);
        assertEquals(tempDir.resolve(Path.of("in", campaignDirFolder)), cis.getInDirectory(campaignDirFolder));
    }


    @Test
    void getInDirectory_absent_on_inDirectory_test(@TempDir Path tempDir) throws Exception {
        //Folder creation
        final String campaignDirFolder = "CAMPAIGN-NAME-FOLDER";
        Path fullPath = Paths.get(tempDir.toString(),"in",campaignDirFolder);
        Files.createDirectories(fullPath);
        //No "kraftwerk.json" file created for that use-case

        FileUtilsInterface fileUtils = new FileSystemImpl(tempDir.toString());
        ControlInputSequence cis = new ControlInputSequence(tempDir.toString(), fileUtils);
        KraftwerkException thrown = assertThrows(
                KraftwerkException.class,
                () -> cis.getInDirectory(campaignDirFolder),
                "Expected doThing() to throw, but it didn't"
        );
        assertEquals("Configuration file not found at paths " + campaignDirFolder + " and " + fullPath, thrown.getMessage());
    }

    @Test
    void kraftwerkJsonFile_absent_test(@TempDir Path tempDir) {
        FileUtilsInterface fileUtils = new FileSystemImpl(tempDir.toString());
        ControlInputSequence cis = new ControlInputSequence(tempDir.toString(), fileUtils);
        assertEquals(Boolean.FALSE, cis.verifyInDirectory(tempDir));
    }


    @Test
    void kraftwerkJsonFile_preent_test(@TempDir Path tempDir) throws IOException {
        Path file = tempDir.resolve("kraftwerk.json");
        Files.write(file, "\n".getBytes());
        FileUtilsInterface fileUtils = new FileSystemImpl(tempDir.toString());
        ControlInputSequence cis = new ControlInputSequence(tempDir.toString(), fileUtils);
        assertEquals(Boolean.TRUE, cis.verifyInDirectory(tempDir));
    }

}
