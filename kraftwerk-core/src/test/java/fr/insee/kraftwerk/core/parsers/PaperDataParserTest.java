package fr.insee.kraftwerk.core.parsers;

import fr.insee.kraftwerk.core.TestConstants;
import fr.insee.kraftwerk.core.exceptions.NullException;
import fr.insee.bpm.metadata.model.MetadataModel;
import fr.insee.bpm.metadata.model.Variable;
import fr.insee.bpm.metadata.model.VariableType;
import fr.insee.kraftwerk.core.rawdata.QuestionnaireData;
import fr.insee.kraftwerk.core.rawdata.SurveyRawData;
import fr.insee.kraftwerk.core.utils.files.FileSystemImpl;
import fr.insee.kraftwerk.core.utils.files.FileUtilsInterface;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertThrows;

class PaperDataParserTest {

    private final String dataSamplesFolder = TestConstants.UNIT_TESTS_DIRECTORY + "/data";
    private final FileUtilsInterface fileUtilsInterface = new FileSystemImpl(TestConstants.TEST_RESOURCES_DIRECTORY);

    @Test
    void readPaperFile_null() {
        //Given
        SurveyRawData data = new SurveyRawData("TEST");
        PaperDataParser parser = new PaperDataParser(data, fileUtilsInterface);

        //When + Then
        assertThrows(NullPointerException.class,() ->parser.parseDataFile(null));
    }

    @Test
    void parsePaperDataFile() throws NullException, NumberFormatException {
        //Given
        SurveyRawData data = new SurveyRawData("TEST");
        MetadataModel metadataModel = new MetadataModel();
        metadataModel.getVariables().putVariable(new Variable("TESTSTRING1", metadataModel.getRootGroup(), VariableType.STRING));
        metadataModel.getVariables().putVariable(new Variable("TESTSTRING2", metadataModel.getRootGroup(), VariableType.STRING));
        metadataModel.getVariables().putVariable(new Variable("TESTINT1", metadataModel.getRootGroup(), VariableType.INTEGER));
        data.setMetadataModel(metadataModel);
        Path dataPath = Paths.get(dataSamplesFolder + "/paper_csv/fake-paper-data.csv");
        PaperDataParser parser = new PaperDataParser(data, fileUtilsInterface);

        //When
        parser.parseSurveyData(dataPath,null);

        //Then
        //Content count assert
        Assertions.assertThat(data.getQuestionnairesCount()).isEqualTo(1);
        //Content assert
        for (QuestionnaireData questionnaireData : data.getQuestionnaires()) {
            String fooValue1 = questionnaireData.getValue("TESTSTRING1");
            String fooValue2 = questionnaireData.getValue("TESTSTRING2");
            String fooValue3 = questionnaireData.getValue("TESTINT1");
            //TESTSTRING1 must be uneven
            Assertions.assertThat(Integer.parseInt(fooValue1.replace("test","")) % 2).isEqualTo(1);
            //TESTSTRING2 must be even
            Assertions.assertThat(Integer.parseInt(fooValue2.replace("test","")) % 2).isZero();
            //TESTINT1 must be parsable
            Integer fooInt = Integer.parseInt(fooValue3);
            Assertions.assertThat(fooInt).isNotNull();
        }
    }

    @Test
    void parsePaperDataFolder() throws NullException, NumberFormatException {
        //Given
        SurveyRawData data = new SurveyRawData("TEST");
        MetadataModel metadataModel = new MetadataModel();
        metadataModel.getVariables().putVariable(new Variable("TESTSTRING1", metadataModel.getRootGroup(), VariableType.STRING));
        metadataModel.getVariables().putVariable(new Variable("TESTSTRING2", metadataModel.getRootGroup(), VariableType.STRING));
        metadataModel.getVariables().putVariable(new Variable("TESTINT1", metadataModel.getRootGroup(), VariableType.INTEGER));
        data.setMetadataModel(metadataModel);
        Path dataPath = Paths.get(dataSamplesFolder + "/paper_csv/fake-multiple-files");
        PaperDataParser parser = new PaperDataParser(data, fileUtilsInterface);

        //When
        parser.parseSurveyData(dataPath,null);

        //Then
        //Content count assert
        Assertions.assertThat(data.getQuestionnairesCount()).isEqualTo(5);
        //Content assert
        for (QuestionnaireData questionnaireData : data.getQuestionnaires()) {
            String fooValue1 = questionnaireData.getValue("TESTSTRING1");
            String fooValue2 = questionnaireData.getValue("TESTSTRING2");
            String fooValue3 = questionnaireData.getValue("TESTINT1");
            //TESTSTRING1 must be uneven
            Assertions.assertThat(Integer.parseInt(fooValue1.replace("test","")) % 2).isEqualTo(1);
            //TESTSTRING2 must be even
            Assertions.assertThat(Integer.parseInt(fooValue2.replace("test","")) % 2).isZero();
            //TESTINT1 must be parsable
            Integer fooInt = Integer.parseInt(fooValue3);
            Assertions.assertThat(fooInt).isNotNull();
        }
    }
}
