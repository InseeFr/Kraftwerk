package fr.insee.kraftwerk.core.parsers;


import fr.insee.bpm.metadata.model.MetadataModel;
import fr.insee.bpm.metadata.model.Variable;
import fr.insee.bpm.metadata.model.VariableType;
import fr.insee.kraftwerk.core.TestConstants;
import fr.insee.kraftwerk.core.rawdata.QuestionnaireData;
import fr.insee.kraftwerk.core.rawdata.SurveyRawData;
import fr.insee.kraftwerk.core.utils.files.FileSystemImpl;
import fr.insee.kraftwerk.core.utils.files.FileUtilsInterface;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

import static org.junit.Assert.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;

class XformsDataParserTest {

    private final String dataSamplesFolder = TestConstants.UNIT_TESTS_DIRECTORY + "/data";
    private final FileUtilsInterface fileUtilsInterface = new FileSystemImpl(TestConstants.TEST_RESOURCES_DIRECTORY);

    @Test
    void readXmlFile_null() {
        SurveyRawData data = new SurveyRawData("TEST");
        XformsDataParser parser = new XformsDataParser(data, fileUtilsInterface);

        assertThrows(NullPointerException.class,() ->parser.parseDataFile(null));
        parser.parseDataFile(Path.of("notfound.xml"));
        assertEquals(new ArrayList<QuestionnaireData>(),data.getQuestionnaires());
    }


    @Test
    void parseDataFile_EmptyMetadataModel_Test() {
        //
        SurveyRawData data = new SurveyRawData("TEST");
        //PREDICATE : "data..getMetadataModel()" MUST NOT BE NULL! -> function "parseDataFile()" must evolve to manage that case!
        MetadataModel metadataModel = new MetadataModel(); //Here, we test with an empty MetadataModel
        data.setMetadataModel(metadataModel);
        XformsDataParser parser = new XformsDataParser(data, fileUtilsInterface);

        Path dataPath = Paths.get(dataSamplesFolder + "/xforms/xforms-data-1.xml");
        parser.parseDataFile(dataPath);

        //Only 1 "<Questionnaire>[...]</Questionnaire>" in the XML file => thus me expect to also have 1 "questionnaireData"
        assertEquals(1, data.getQuestionnairesCount());
    }


    @Test
    void parseDataFile_NotEmptyMetadataModel_Test() {
        //
        SurveyRawData data = new SurveyRawData("TEST");
        //PREDICATE : "data..getMetadataModel()" MUST NOT BE NULL! -> function "parseDataFile()" must evolve to manage that case!
        MetadataModel metadataModel = new MetadataModel();
        //Variable belonging to "Questionnaires > Questionnaire > InformationsPersonnalisees > Variable"
        metadataModel.getVariables().putVariable(new Variable("Q1-IDV1", metadataModel.getRootGroup(), VariableType.STRING));
        //Variable belonging to "Questionnaires > Questionnaire > InformationsPersonnalisees > Groupe > Groupe > Variable"
        metadataModel.getVariables().putVariable(new Variable("Q1-IDG1-IDV1", metadataModel.getRootGroup(), VariableType.STRING));
        data.setMetadataModel(metadataModel);
        XformsDataParser parser = new XformsDataParser(data, fileUtilsInterface);

        Path dataPath = Paths.get(dataSamplesFolder + "/xforms/xforms-data-1.xml");
        parser.parseDataFile(dataPath);

        //Only 1 "<Questionnaire>[...]</Questionnaire>" in the XML file => thus me expect to also have 1 "questionnaireData"
        assertEquals(1, data.getQuestionnairesCount());

        for (QuestionnaireData questionnaireData : data.getQuestionnaires()) {
            String varValue = questionnaireData.getValue("Q1-IDV1");
            if (varValue != null) {
                assertEquals("Q1-IDV1-newVal", varValue);
            }
            Pair<String, Object> instanceReference = new Pair<String, Object>() {
                @Override
                public String getLeft() {
                    //!!!WARNING!! what we call "groupName" is actually the "typeGroup" attribute!
                    return "Q1-TG1";
                }

                @Override
                public Object getRight() {
                    //group id (String) or group number (Integer)
                    return "Q1-IDG1";
                }

                @Override
                public Object setValue(Object value) {
                    return "Q1-IDG1";
                }
            };
            String groupVarValue = questionnaireData.getValue("Q1-IDG1-IDV1", instanceReference);
            if (groupVarValue != null) {
                assertEquals("Q1-IDG1-IDV1-newVal", groupVarValue);
            }
        }
    }

}
