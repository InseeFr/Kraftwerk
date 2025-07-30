package fr.insee.kraftwerk.core.parsers;

import fr.insee.bpm.metadata.model.MetadataModel;
import fr.insee.kraftwerk.core.TestConstants;
import fr.insee.kraftwerk.core.exceptions.NullException;
import fr.insee.kraftwerk.core.rawdata.SurveyRawData;
import fr.insee.kraftwerk.core.utils.KraftwerkExecutionContext;
import fr.insee.kraftwerk.core.utils.files.FileSystemImpl;
import fr.insee.kraftwerk.core.utils.files.FileUtilsInterface;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

class DataParserTest {

    private final String dataSamplesFolder = TestConstants.UNIT_TESTS_DIRECTORY + "/data";


    @Test
    void parseDataFileWithoutDDI(@TempDir Path tempDir) throws Exception {
        //PREDICATE : "SurveyRawData" MUST NOT BE NULL!
        SurveyRawData data = new SurveyRawData();
        FileUtilsInterface fileUtilsInterface = new FileSystemImpl(tempDir.toString());
        //Note : As "DataParser" is abstract, we instanciate an implementation (here : "XformsDataParser") to unit-test the "DataParser" methods
        DataParser dataParser = new XformsDataParser(data, fileUtilsInterface);
        dataParser.parseDataFileWithoutDDI(null, null);
    }


    @Test
    void parseSurveyData_nullDataPath(@TempDir Path tempDir) {
        //PREDICATE : "SurveyRawData" MUST NOT BE NULL!
        SurveyRawData data = new SurveyRawData();
        FileUtilsInterface fileUtilsInterface = new FileSystemImpl(tempDir.toString());
        //Note : As "DataParser" is abstract, we instanciate an implementation (here : "XformsDataParser") to unit-test the "DataParser" methods
        DataParser dataParser = new XformsDataParser(data, fileUtilsInterface);

        //Checks
        NullException thrown = assertThrows(
                NullException.class,
                () -> dataParser.parseSurveyData(null, null), //test with "dataPath set to null"
                "Expected doThing() to throw, but it didn't"
        );
        assertEquals(DataParser.DATAPATH_IS_NULL, thrown.getMessage());
    }


    @Test
    void parseSurveyData_emptyDirectory(@TempDir Path tempDir) throws NullException {
        //PREDICATE : "SurveyRawData" MUST NOT BE NULL!
        SurveyRawData data = new SurveyRawData();
        FileUtilsInterface fileUtilsInterface = new FileSystemImpl(tempDir.toString());
        KraftwerkExecutionContext kraftwerkExecutionContext = TestConstants.getKraftwerkExecutionContext();
        //Note : As "DataParser" is abstract, we instanciate an implementation (here : "XformsDataParser") to unit-test the "DataParser" methods
        DataParser dataParser = new XformsDataParser(data, fileUtilsInterface);

        //Test
        dataParser.parseSurveyData(tempDir, null); //Test with "kraftwerkExecutionContext" null
        dataParser.parseSurveyData(tempDir, kraftwerkExecutionContext);
        //Checks
        assertEquals(0, kraftwerkExecutionContext.getOkFileNames().size());
    }


    @Test
    void parseSurveyData_notEmptyDirectory() throws Exception {
        Path dataPath = Paths.get(dataSamplesFolder + "/xforms");

        //PREDICATE : "SurveyRawData" and its "MetadataModel" MUST NOT BE NULL!
        SurveyRawData data = new SurveyRawData();
        data.setMetadataModel(new MetadataModel());
        FileUtilsInterface fileUtilsInterface = new FileSystemImpl(TestConstants.TEST_RESOURCES_DIRECTORY);
        KraftwerkExecutionContext kraftwerkExecutionContext = TestConstants.getKraftwerkExecutionContext();
        //Note : As "DataParser" is abstract, we instanciate an implementation (here : "XformsDataParser") to unit-test the "DataParser" methods
        DataParser dataParser = new XformsDataParser(data, fileUtilsInterface);

        //Test
        dataParser.parseSurveyData(dataPath, null); //Test with "kraftwerkExecutionContext" null
        dataParser.parseSurveyData(dataPath, kraftwerkExecutionContext);

        //Checks
        assertEquals(1, kraftwerkExecutionContext.getOkFileNames().size());
        assertEquals("xforms-data-1.xml", kraftwerkExecutionContext.getOkFileNames().get(0));
    }


    @Test
    void parseSurveyData_file() throws Exception {
        Path file = Paths.get(dataSamplesFolder + "/xforms/xforms-data-1.xml");

        //PREDICATE : "SurveyRawData" and its "MetadataModel" MUST NOT BE NULL!
        SurveyRawData data = new SurveyRawData();
        data.setMetadataModel(new MetadataModel());
        FileUtilsInterface fileUtilsInterface = new FileSystemImpl(TestConstants.TEST_RESOURCES_DIRECTORY);
        KraftwerkExecutionContext kraftwerkExecutionContext = TestConstants.getKraftwerkExecutionContext();
        //Note : As "DataParser" is abstract, we instanciate an implementation (here : "XformsDataParser") to unit-test the "DataParser" methods
        DataParser dataParser = new XformsDataParser(data, fileUtilsInterface);

        //Test
        dataParser.parseSurveyData(file, null); //Test with "kraftwerkExecutionContext" null
        dataParser.parseSurveyData(file, kraftwerkExecutionContext);

        //Checks
        assertEquals(1, kraftwerkExecutionContext.getOkFileNames().size());
        assertEquals("xforms-data-1.xml", kraftwerkExecutionContext.getOkFileNames().getFirst());
    }


    @Test
    void parseSurveyDataWithoutDDI_nullDataPath(@TempDir Path tempDir) {
        //PREDICATE : "SurveyRawData" MUST NOT BE NULL!
        SurveyRawData data = new SurveyRawData();
        FileUtilsInterface fileUtilsInterface = new FileSystemImpl(tempDir.toString());
        //Note : As "DataParser" is abstract, we instanciate an implementation (here : "XformsDataParser") to unit-test the "DataParser" methods
        DataParser dataParser = new XformsDataParser(data, fileUtilsInterface);

        //Checks
        NullException thrown = assertThrows(
                NullException.class,
                () -> dataParser.parseSurveyDataWithoutDDI(null, null, null), //test with "dataPath set to null"
                "Expected doThing() to throw, but it didn't"
        );
        assertEquals(DataParser.DATAPATH_IS_NULL, thrown.getMessage());
    }


    @Test
    void parseSurveyDataWithoutDDI_emptyDirectory(@TempDir Path tempDir) throws NullException {
        //PREDICATE : "SurveyRawData" MUST NOT BE NULL!
        SurveyRawData data = new SurveyRawData();
        FileUtilsInterface fileUtilsInterface = new FileSystemImpl(tempDir.toString());
        KraftwerkExecutionContext kraftwerkExecutionContext = TestConstants.getKraftwerkExecutionContext();
        //Note : As "DataParser" is abstract, we instanciate an implementation (here : "XformsDataParser") to unit-test the "DataParser" methods
        DataParser dataParser = new XformsDataParser(data, fileUtilsInterface);

        //Test
        dataParser.parseSurveyDataWithoutDDI(tempDir, null, null); //Test with "kraftwerkExecutionContext" null
        dataParser.parseSurveyDataWithoutDDI(tempDir, null, kraftwerkExecutionContext);
        //Checks
        assertEquals(0, kraftwerkExecutionContext.getOkFileNames().size());
    }


    @Test
    void parseSurveyDataWithoutDDI_notEmptyDirectory(@TempDir Path tempDir) throws Exception {
        Path file1 = tempDir.resolve("tmp1.xml");
        Files.write(file1, "\n".getBytes());
        Path file2 = tempDir.resolve("tmp2.xml");
        Files.write(file2, "\n".getBytes());

        //PREDICATE : "SurveyRawData" MUST NOT BE NULL!
        SurveyRawData data = new SurveyRawData();
        FileUtilsInterface fileUtilsInterface = new FileSystemImpl(tempDir.toString());
        KraftwerkExecutionContext kraftwerkExecutionContext = TestConstants.getKraftwerkExecutionContext();
        //Note : As "DataParser" is abstract, we instanciate an implementation (here : "XformsDataParser") to unit-test the "DataParser" methods
        DataParser dataParser = new XformsDataParser(data, fileUtilsInterface);

        //Test
        dataParser.parseSurveyDataWithoutDDI(tempDir, null, null); //Test with "kraftwerkExecutionContext" null
        dataParser.parseSurveyDataWithoutDDI(tempDir, null, kraftwerkExecutionContext);

        //Checks
        assertEquals(2, kraftwerkExecutionContext.getOkFileNames().size());
    }


    @Test
    void parseSurveyDataWithoutDDI_file(@TempDir Path tempDir) throws Exception {
        Path file = tempDir.resolve("tmp.xml");
        Files.write(file, "\n".getBytes());

        //PREDICATE : "SurveyRawData" MUST NOT BE NULL!
        SurveyRawData data = new SurveyRawData();
        FileUtilsInterface fileUtilsInterface = new FileSystemImpl(tempDir.toString());
        KraftwerkExecutionContext kraftwerkExecutionContext = TestConstants.getKraftwerkExecutionContext();
        //Note : As "DataParser" is abstract, we instanciate an implementation (here : "XformsDataParser") to unit-test the "DataParser" methods
        DataParser dataParser = new XformsDataParser(data, fileUtilsInterface);

        //Test
        dataParser.parseSurveyDataWithoutDDI(file, null, null); //Test with "kraftwerkExecutionContext" null
        dataParser.parseSurveyDataWithoutDDI(file, null, kraftwerkExecutionContext);

        //Checks
        assertEquals(1, kraftwerkExecutionContext.getOkFileNames().size());
        assertEquals("tmp.xml", kraftwerkExecutionContext.getOkFileNames().getFirst());
    }

}
