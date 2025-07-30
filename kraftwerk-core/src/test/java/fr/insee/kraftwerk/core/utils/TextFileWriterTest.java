package fr.insee.kraftwerk.core.utils;

import fr.insee.bpm.metadata.model.Group;
import fr.insee.bpm.metadata.model.Variable;
import fr.insee.bpm.metadata.model.VariableType;
import fr.insee.kraftwerk.core.Constants;
import fr.insee.kraftwerk.core.TestConstants;
import fr.insee.kraftwerk.core.metadata.ErrorVariableLength;
import fr.insee.kraftwerk.core.utils.files.FileSystemImpl;
import fr.insee.kraftwerk.core.utils.files.FileUtilsInterface;
import fr.insee.kraftwerk.core.vtl.ErrorVtlTransformation;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class TextFileWriterTest {

    private static Stream<Arguments> pathContainsFolderParameterizedTests() {
        return Stream.of(
                Arguments.of(Path.of("aaa", "bbb", "ccc", "ddd", "eee"), "aaa", Boolean.TRUE),
                Arguments.of(Path.of("aaa", "bbb", "ccc", "ddd", "eee"), "bbb", Boolean.TRUE),
                Arguments.of(Path.of("aaa", "bbb", "ccc", "ddd", "eee"), "ccc", Boolean.TRUE),
                Arguments.of(Path.of("aaa", "bbb", "ccc", "ddd", "eee"), "ddd", Boolean.TRUE),
                Arguments.of(Path.of("aaa", "bbb", "ccc", "ddd", "eee"), "eee", Boolean.TRUE),
                Arguments.of(Path.of("aaa", "bbb", "ccc", "ddd", "eee"), "fff", Boolean.FALSE)
        );
    }

    @ParameterizedTest
    @MethodSource("pathContainsFolderParameterizedTests")
    void pathContainsFolder_ParameterizedTest(Path pathToScan, String folderToFind, boolean expectedResult, @TempDir Path tempDir) {
        Path fullPath = Paths.get(tempDir.toString(),pathToScan.toString());
        boolean pathContainsFolder = TextFileWriter.pathContainsFolder(fullPath, folderToFind);
        assertEquals(expectedResult, pathContainsFolder);
    }


    @Test
    void writeLogFile(@TempDir Path tempDir) throws Exception {
        //Folder creation
        final String campaignDirFolder = "CAMPAIGN-NAME-FOLDER";
        Path fullPath = Paths.get(tempDir.toString(),"in",campaignDirFolder);
        Files.createDirectories(fullPath);

        KraftwerkExecutionContext kraftwerkExecutionContext = TestConstants.getKraftwerkExecutionContext();
        FileUtilsInterface fileUtils = new FileSystemImpl(tempDir.toString());

        TextFileWriter.writeLogFile(fullPath, kraftwerkExecutionContext, fileUtils);

        //Checks
        //1) "out/<campaignName>/<date>" folder created
        assertTrue(Files.exists(Paths.get(tempDir.toString(),"out",campaignDirFolder,
                kraftwerkExecutionContext.getExecutionDateTime().format(DateTimeFormatter.ofPattern(Constants.OUTPUT_FOLDER_DATETIME_PATTERN)))));
        //2) log file created & exists
        Path logFilePath = Paths.get(tempDir.toString(),"out",campaignDirFolder,
                kraftwerkExecutionContext.getExecutionDateTime().format(DateTimeFormatter.ofPattern(Constants.OUTPUT_FOLDER_DATETIME_PATTERN)),
                campaignDirFolder + "_LOG_" + kraftwerkExecutionContext.getStartTimeStamp() +".txt");
        assertTrue(Files.exists(logFilePath));
        //3) log file content not empty
        assertNotEquals(0, logFilePath.toFile().length());
        //4) check log file content
        List<String> content = Files.readAllLines(logFilePath);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss.SSS");
        List<String> expectedContent = new LinkedList<>();
        expectedContent.add("Début exécution: " + simpleDateFormat.format(kraftwerkExecutionContext.getStartTimeStamp()));
        expectedContent.add("Fin exécution: " + simpleDateFormat.format(kraftwerkExecutionContext.getEndTimeStamp()));
        expectedContent.add("Lignes par table:");
        expectedContent.add("Fichiers traités avec succès: ");
        for (int i = 0; i < content.size(); i++) {
            assertEquals(expectedContent.get(i), content.get(i));
        }
    }


    @Test
    void writeErrorsFile_empty(@TempDir Path tempDir) throws Exception {
        //Folder creation
        final String campaignDirFolder = "CAMPAIGN-NAME-FOLDER";
        Path fullPath = Paths.get(tempDir.toString(),"in",campaignDirFolder);
        Files.createDirectories(fullPath);

        KraftwerkExecutionContext kraftwerkExecutionContext = TestConstants.getKraftwerkExecutionContext();
        FileUtilsInterface fileUtils = new FileSystemImpl(tempDir.toString());

        TextFileWriter.writeErrorsFile(fullPath, kraftwerkExecutionContext, fileUtils);

        //Checks
        //1) "out/<campaignName>/<date>" folder created
        assertTrue(Files.exists(Paths.get(tempDir.toString(),"out",campaignDirFolder,
                kraftwerkExecutionContext.getExecutionDateTime().format(DateTimeFormatter.ofPattern(Constants.OUTPUT_FOLDER_DATETIME_PATTERN)))));
        //2) error file NOT created in case of no error
        Path errorFilePath = Paths.get(tempDir.toString(),"out",campaignDirFolder,
                kraftwerkExecutionContext.getExecutionDateTime().format(DateTimeFormatter.ofPattern(Constants.OUTPUT_FOLDER_DATETIME_PATTERN)),
                "errors.txt");
        assertFalse(Files.exists(errorFilePath));
    }


    @Test
    void writeErrorsFile_notEmpty(@TempDir Path tempDir) throws Exception {
        //Folder creation
        final String campaignDirFolder = "CAMPAIGN-NAME-FOLDER";
        Path fullPath = Paths.get(tempDir.toString(),"in",campaignDirFolder);
        Files.createDirectories(fullPath);

        KraftwerkExecutionContext kraftwerkExecutionContext = TestConstants.getKraftwerkExecutionContext();
        //Add errors
        //NOTE : "ErrorVtlTransformation" will generate 3 lines in the "errors.txt" file.
        kraftwerkExecutionContext.getErrors().add(new ErrorVtlTransformation("testVtlTransformation","testVtlTransformation-error-msg"));
        Variable var = new Variable("TESTVAR1234",new Group("TESTGROUP"), VariableType.STRING, "2887.expected_length");
        var.setMaxLengthData(3578);
        //NOTE : "ErrorVariableLength" will generate 2 lines in the "errors.txt" file.
        kraftwerkExecutionContext.getErrors().add(new ErrorVariableLength(var,"WEB-ABCD"));
        FileUtilsInterface fileUtils = new FileSystemImpl(tempDir.toString());

        TextFileWriter.writeErrorsFile(fullPath, kraftwerkExecutionContext, fileUtils);

        //Checks
        //1) "out/<campaignName>/<date>" folder created
        assertTrue(Files.exists(Paths.get(tempDir.toString(),"out",campaignDirFolder,
                kraftwerkExecutionContext.getExecutionDateTime().format(DateTimeFormatter.ofPattern(Constants.OUTPUT_FOLDER_DATETIME_PATTERN)))));
        //2) error file created & exists
        Path errorFilePath = Paths.get(tempDir.toString(),"out",campaignDirFolder,
                kraftwerkExecutionContext.getExecutionDateTime().format(DateTimeFormatter.ofPattern(Constants.OUTPUT_FOLDER_DATETIME_PATTERN)),
                "errors.txt");
        assertTrue(Files.exists(errorFilePath));
        //3) log file content not empty
        assertNotEquals(0, errorFilePath.toFile().length());
        //4) check log file content
        List<String> content = Files.readAllLines(errorFilePath);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss.SSS");
        List<String> expectedContent = new LinkedList<>();
        expectedContent.add("VTL Transformation error detected on :");
        expectedContent.add("Script='testVtlTransformation'");
        expectedContent.add("Message='testVtlTransformation-error-msg'");
        expectedContent.add("Warning : The maximum length read for variable TESTVAR1234 (DataMode: WEB-ABCD) exceed expected length");
        expectedContent.add("Expected: 2887 but received: 3578");
        for (int i = 0; i < content.size(); i++) {
            assertEquals(expectedContent.get(i), content.get(i));
        }
    }

}
