package fr.insee.kraftwerk.core.parsers;

import fr.insee.kraftwerk.core.TestConstants;
import fr.insee.kraftwerk.core.exceptions.NullException;
import fr.insee.kraftwerk.core.metadata.MetadataModel;
import fr.insee.kraftwerk.core.metadata.Variable;
import fr.insee.kraftwerk.core.metadata.VariableType;
import fr.insee.kraftwerk.core.rawdata.QuestionnaireData;
import fr.insee.kraftwerk.core.rawdata.SurveyRawData;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertThrows;

class PaperDataParserTest {

    private final String dataSamplesFolder = TestConstants.UNIT_TESTS_DIRECTORY + "/data";

    @Test
    void readPaperFile_null() {
        //Given
        SurveyRawData data = new SurveyRawData("TEST");
        PaperDataParser parser = new PaperDataParser(data);

        //When + Then
        assertThrows(NullPointerException.class,() ->parser.parseDataFile(null));
        parser.parseDataFile(Path.of("notfound.csv"));
        Assertions.assertThat(data.getQuestionnaires()).isEmpty();
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
        PaperDataParser parser = new PaperDataParser(data);

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
        PaperDataParser parser = new PaperDataParser(data);

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
