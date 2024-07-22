package fr.insee.kraftwerk.core.utils.files;

import fr.insee.kraftwerk.core.Constants;
import fr.insee.kraftwerk.core.TestConstants;
import fr.insee.kraftwerk.core.exceptions.KraftwerkException;
import fr.insee.kraftwerk.core.inputs.UserInputsFile;
import org.assertj.core.api.Assertions;
import org.junit.Assert;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
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
        Assertions.assertThat(fileNames).contains("file1.txt","file2.json","file3.xml");
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

    @Test
    void createDirectoryTest() throws IOException {
        //GIVEN
        String campaignName = "create_directory";
        Path inputDirectory = Path.of(TestConstants.UNIT_TESTS_DIRECTORY, "files", campaignName);

        //WHEN
        fileSystemImpl.createDirectoryIfNotExist(inputDirectory);

        //THEN
        Assertions.assertThat(inputDirectory.toFile()).exists().isDirectory();

        //CLEAN
        Files.deleteIfExists(inputDirectory);
    }

    @Test
    void isDirectoryTest(){
        //GIVEN
        String campaignName = "move_files";
        Path inputDirectory = Path.of(TestConstants.UNIT_TESTS_DIRECTORY, "files", campaignName);

        //WHEN + THEN
        Assertions.assertThat(fileSystemImpl.isDirectory(String.valueOf(inputDirectory))).isTrue();
        Assertions.assertThat(fileSystemImpl.isDirectory(String.valueOf(inputDirectory.resolve("move_files.json")))).isFalse();
        Assertions.assertThat(fileSystemImpl.isDirectory(String.valueOf(inputDirectory.resolve("NULL.json")))).isNull();
    }

    @Test
    void getSizeOfTest() throws IOException {
        //GIVEN
        String campaignName = "convert_path";
        Path file = Path.of(TestConstants.UNIT_TESTS_DIRECTORY, "files", campaignName).resolve("test.txt");

        //WHEN + THEN
        Assertions.assertThat(fileSystemImpl.getSizeOf(file.toString())).isEqualTo(Files.size(file));
    }

    @Test
    void writeFileTest() throws IOException {
        //GIVEN
        String campaignName = "write_file";
        Path inputDirectory = Path.of(TestConstants.UNIT_TESTS_DIRECTORY, "files", campaignName);

        //WHEN
        fileSystemImpl.writeFile(inputDirectory.resolve("test.txt").toString(),"test",true);

        //THEN
        Assertions.assertThat(new File(inputDirectory.resolve("test.txt").toString())).exists();
        Assertions.assertThat(Files.readAllBytes(inputDirectory.resolve("test.txt"))).contains("test".getBytes());

        //CLEAN
        Files.deleteIfExists(inputDirectory.resolve("test.txt"));
        Files.deleteIfExists(inputDirectory);
    }

    @Test
    void writeFileTest_append() throws IOException {
        //GIVEN
        String campaignName = "write_file";
        Path inputDirectory = Path.of(TestConstants.UNIT_TESTS_DIRECTORY, "files", campaignName);

        //WHEN
        fileSystemImpl.writeFile(inputDirectory.resolve("test.txt").toString(),"test1",true);
        fileSystemImpl.writeFile(inputDirectory.resolve("test.txt").toString(),"\ntest2",false);

        //THEN
        Assertions.assertThat(new File(inputDirectory.resolve("test.txt").toString())).exists();
        Assertions.assertThat(Files.readAllBytes(inputDirectory.resolve("test.txt"))).contains("test".getBytes()).contains("test2".getBytes());

        //CLEAN
        Files.deleteIfExists(inputDirectory.resolve("test.txt"));
        Files.deleteIfExists(inputDirectory);
    }
    @Test
    void findFileTest() throws KraftwerkException {
        //GIVEN
        String campaignName = "list_files";
        Path inputDirectory = Path.of(TestConstants.UNIT_TESTS_DIRECTORY, "files", campaignName);

        //WHEN + THEN
        Assertions.assertThat(fileSystemImpl.findFile(inputDirectory.toString(), "[\\w,\\s-]+\\.xml")).isNotEmpty();
    }

    @Test
    void findFileTest_notfound() throws KraftwerkException {
        //GIVEN
        String campaignName = "list_files";
        Path inputDirectory = Path.of(TestConstants.UNIT_TESTS_DIRECTORY, "files", campaignName);

        //WHEN+THEN
        assertThrows(KraftwerkException.class, () -> fileSystemImpl.findFile(inputDirectory.toString(),Constants.DDI_FILE_REGEX));
    }

    @Test
    void readFileTest() throws IOException {
        //GIVEN
        String campaignName = "read_files";
        Path inputDirectory = Path.of(TestConstants.UNIT_TESTS_DIRECTORY, "files", campaignName);

        //WHEN + THEN
        try(InputStream inputStream = fileSystemImpl.readFile(inputDirectory.resolve("test.txt").toString())){
            Assertions.assertThat(inputStream).hasContent("hello !");
        }
    }

    @Test
    void isFileExistsTest(){
        //GIVEN
        String campaignName = "read_files";
        Path inputDirectory = Path.of(TestConstants.UNIT_TESTS_DIRECTORY, "files", campaignName);

        //WHEN + THEN
        Assertions.assertThat(fileSystemImpl.isFileExists(inputDirectory.resolve("test.txt").toString())).isTrue();
        Assertions.assertThat(fileSystemImpl.isFileExists(inputDirectory.resolve("null.txt").toString())).isFalse();
    }

    @Test
    void moveFileTest() throws KraftwerkException, IOException {
        //GIVEN
        String campaignName = "move_files";
        Path inputDirectory = Path.of(TestConstants.UNIT_TESTS_DIRECTORY, "files", campaignName);
        Files.copy(inputDirectory.resolve("move_files.json"),inputDirectory.resolve("test1.txt"));

        String campaignName2 = "move_files2";
        Path inputDirectory2 = Path.of(TestConstants.UNIT_TESTS_DIRECTORY, "files", campaignName2);

        //WHEN
        fileSystemImpl.moveFile(inputDirectory.resolve("test1.txt"), inputDirectory2.resolve("test2.txt").toString());

        //THEN
        Assertions.assertThat(inputDirectory2.resolve("test2.txt")).exists();

        //CLEAN
        Files.deleteIfExists(inputDirectory2.resolve("test2.txt"));
        Files.deleteIfExists(inputDirectory.resolve("test1.txt"));
    }

    @Test
    void moveFileTest_FromPath() throws KraftwerkException, IOException {
        //GIVEN
        String campaignName = "move_files";
        Path inputDirectory = Path.of(TestConstants.UNIT_TESTS_DIRECTORY, "files", campaignName);
        Files.copy(inputDirectory.resolve("move_files.json"),inputDirectory.resolve("test1.txt"));

        String campaignName2 = "move_files2";
        Path inputDirectory2 = Path.of(TestConstants.UNIT_TESTS_DIRECTORY, "files", campaignName2);

        //WHEN
        fileSystemImpl.moveFile(inputDirectory.resolve("test1.txt"), inputDirectory2.resolve("test2.txt").toString());

        //THEN
        Assertions.assertThat(inputDirectory2.resolve("test2.txt")).exists();

        //CLEAN
        Files.deleteIfExists(inputDirectory2.resolve("test2.txt"));
        Files.deleteIfExists(inputDirectory.resolve("test1.txt"));
        Files.deleteIfExists(inputDirectory2);
    }



    @Test
    void convertToPathTest_nullUserField() throws KraftwerkException {
        Assertions.assertThat(fileSystemImpl.convertToPath(null,null)).isNull();
    }

    @Test
    void convertToPathTest_directoryNotExists(){
        Assert.assertThrows(KraftwerkException.class, () -> fileSystemImpl.convertToPath("test", Path.of("NOT SUPPOSED TO EXIST")));
    }

    @Test
    void convertToPathTest() throws KraftwerkException {
        //GIVEN
        String campaignName = "convert_path";
        Path inputDirectory = Path.of(TestConstants.UNIT_TESTS_DIRECTORY, "files", campaignName);

        //WHEN+THEN
        Assertions.assertThat(fileSystemImpl.convertToPath("test.txt", inputDirectory)).exists();
    }

    @Test
    void convertToURLTest_nullUserField(){
        Assertions.assertThat(fileSystemImpl.convertToUrl(null,null)).isNull();
    }

    @Test
    void convertToURLTest(){
        //GIVEN
        String campaignName = "convert_path";
        Path inputDirectory = Path.of(TestConstants.UNIT_TESTS_DIRECTORY, "files", campaignName);

        //WHEN+THEN
        Assertions.assertThat(fileSystemImpl.convertToUrl("test.txt", inputDirectory)).endsWith("test.txt");
    }
}
