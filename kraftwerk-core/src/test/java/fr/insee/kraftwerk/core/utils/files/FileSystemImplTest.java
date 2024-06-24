package fr.insee.kraftwerk.core.utils.files;

import fr.insee.kraftwerk.core.Constants;
import fr.insee.kraftwerk.core.TestConstants;
import fr.insee.kraftwerk.core.exceptions.KraftwerkException;
import fr.insee.kraftwerk.core.inputs.UserInputsFile;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.Assert.assertThrows;

class FileSystemImplTest {
    private static final FileSystemImpl fileSystemImpl = new FileSystemImpl();

    @Test
    void renameInputFileTest() throws IOException {

        //GIVEN
        String campaignName = "rename_files";
        Path inputDirectory = Path.of(TestConstants.UNIT_TESTS_DIRECTORY,"files", campaignName);
        //backup
        Files.copy(inputDirectory.resolve("kraftwerk.json"), inputDirectory.getParent().resolve("kraftwerk.json"));

        //WHEN
        fileSystemImpl.renameInputFile(inputDirectory);

        //THEN
        try(Stream<Path> stream = Files.list(inputDirectory).filter(path -> path.getFileName().toString().startsWith("kraftwerk-"))){
            Assertions.assertThat(stream).isNotEmpty();
        }

        //CLEAN
        org.springframework.util.FileSystemUtils.deleteRecursively(inputDirectory);
        Files.createDirectories(inputDirectory);
        Files.move(Path.of(inputDirectory.getParent().toString(),"kraftwerk.json"),Path.of(inputDirectory.toString(),"kraftwerk.json"));
    }

    @Test
    void archiveInputFiles_failWhenNull() {
        assertThrows(NullPointerException.class, () -> fileSystemImpl.archiveInputFiles(null));
    }

    @Test
    void archiveInputFiles_ok() throws IOException, KraftwerkException {

        //GIVEN
        String campaignName = "move_files";
        Path inputDirectory = Path.of(TestConstants.UNIT_TESTS_DIRECTORY, "files", campaignName);
        //backup
        Files.copy(Path.of(TestConstants.UNIT_TESTS_DIRECTORY, "files", campaignName,"move_files.json"),
                Path.of(inputDirectory.getParent().toString(),"move_files.json"));

        //WEB
        Path webDirectory = Paths.get(inputDirectory.toString(), "web");
        Files.createDirectories(webDirectory);
        new File(webDirectory+"/web.xml").createNewFile();
        new File(webDirectory+"/vqs-2021-x00-xforms-ddi.xml").createNewFile();
        new File(webDirectory+"/WEB.vtl").createNewFile();

        //PAPER
        Path paperDirectory =  Paths.get(inputDirectory.toString(), "papier");
        Files.createDirectories(paperDirectory);
        new File(paperDirectory+"/paper.txt").createNewFile();
        new File(paperDirectory+"/vqs-2021-x00-fo-ddi.xml").createNewFile();

        //Reporting
        Path reportingDirectory =  Paths.get(inputDirectory.toString(), "suivi");
        Files.createDirectories(reportingDirectory);
        new File(reportingDirectory+"/reportingdata.xml").createNewFile();

        //Paradata
        Path paradataDirectory =  Paths.get(inputDirectory.toString(), "paradata");
        Files.createDirectories(paradataDirectory);
        new File(Constants.getResourceAbsolutePath(paradataDirectory +"/L0000003.json")).createNewFile();
        new File(Constants.getResourceAbsolutePath(paradataDirectory +"/L0000004.json")).createNewFile();
        new File(Constants.getResourceAbsolutePath(paradataDirectory +"/L0000009.json")).createNewFile();
        new File(Constants.getResourceAbsolutePath(paradataDirectory +"/L0000010.json")).createNewFile();

        UserInputsFile testUserInputsFile = new UserInputsFile(Path.of(inputDirectory.toString(), "move_files.json"),inputDirectory, fileSystemImpl);


        //WHEN
        fileSystemImpl.archiveInputFiles(testUserInputsFile);

        //THEN
        Assertions.assertThat(new File(inputDirectory + "/Archive/papier")).exists();
        Assertions.assertThat(new File(inputDirectory + "/Archive/web")).exists();
        Assertions.assertThat(new File(inputDirectory + "/Archive/paradata/L0000010.json")).exists();
        Assertions.assertThat(new File(inputDirectory + "/Archive/suivi/reportingdata.xml")).exists();

        //CLEAN
        org.springframework.util.FileSystemUtils.deleteRecursively(inputDirectory);
        Files.createDirectories(inputDirectory);
        Files.move(Path.of(inputDirectory.getParent().toString(),"move_files.json"),Path.of(inputDirectory.toString(),"move_files.json"));
    }

    @Test
    void deleteDirectoryTest() throws KraftwerkException, IOException {
        //GIVEN
        String campaignName = "delete_directory";
        Path inputDirectory = Path.of(TestConstants.UNIT_TESTS_DIRECTORY, "files", campaignName);
        Files.createDirectories(inputDirectory);
        Files.createFile(inputDirectory.resolve("test.txt"));

        //WHEN
        fileSystemImpl.deleteDirectory(inputDirectory);

        //THEN
        Assertions.assertThat(new File(inputDirectory.toString())).doesNotExist();
    }

    @Test
    void listFileNamesTest(){
        //GIVEN
        String campaignName = "list_files";
        Path inputDirectory = Path.of(TestConstants.UNIT_TESTS_DIRECTORY, "files", campaignName);

        //WHEN
        List<String> fileNames = fileSystemImpl.listFileNames(inputDirectory.toString());

        //THEN
        Assertions.assertThat(fileNames).containsExactly("file1.txt","file2.json","file3.xml");
    }

    @Test
    void listFilePathsTest() {
        //GIVEN
        String campaignName = "list_files";
        Path inputDirectory = Path.of(TestConstants.UNIT_TESTS_DIRECTORY, "files", campaignName);

        //WHEN
        List<String> filePaths = fileSystemImpl.listFilePaths(inputDirectory.toString());

        //THEN
        Assertions.assertThat(filePaths).hasSize(3);
        for (String filePath : filePaths) {
            Assertions.assertThat(Path.of(filePath).getFileName().toString()).containsAnyOf("file1.txt", "file2.json", "file3.xml");
        }
    }
}
