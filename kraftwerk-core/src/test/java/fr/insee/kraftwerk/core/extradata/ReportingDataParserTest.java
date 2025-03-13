package fr.insee.kraftwerk.core.extradata;

import fr.insee.bpm.metadata.model.Group;
import fr.insee.bpm.metadata.model.MetadataModel;
import fr.insee.bpm.metadata.model.Variable;
import fr.insee.bpm.metadata.model.VariableType;
import fr.insee.kraftwerk.core.Constants;
import fr.insee.kraftwerk.core.extradata.reportingdata.ReportingData;
import fr.insee.kraftwerk.core.extradata.reportingdata.ReportingDataParser;
import fr.insee.kraftwerk.core.extradata.reportingdata.ReportingDataUE;
import fr.insee.kraftwerk.core.extradata.reportingdata.State;
import fr.insee.kraftwerk.core.extradata.reportingdata.XMLReportingDataParser;
import fr.insee.kraftwerk.core.inputs.UserInputs;
import fr.insee.kraftwerk.core.rawdata.QuestionnaireData;
import fr.insee.kraftwerk.core.rawdata.SurveyRawData;
import fr.insee.kraftwerk.core.utils.files.FileSystemImpl;
import fr.insee.kraftwerk.core.utils.files.FileUtilsInterface;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ReportingDataParserTest {

    private ReportingDataParser reportingDataParser;
    private SurveyRawData surveyRawData;
    private ReportingData reportingData;

    @BeforeEach
    void setUp() {
        FileUtilsInterface fileUtilsInterface = new FileSystemImpl("defaultDir") {
            @Override
            public Path getTempVtlFilePath(UserInputs inputs, String processName, String datasetName) {
                return Path.of("target/tmp/" + processName + "_" + datasetName + ".vtl"); // Utilisation d'un
                // répertoire temporaire
            }
        };
        reportingDataParser = new XMLReportingDataParser(fileUtilsInterface) ;

        surveyRawData = new SurveyRawData();
        MetadataModel metadataModel = new MetadataModel();
        surveyRawData.setMetadataModel(metadataModel);
        reportingData = new ReportingData(Path.of("filepath"),new ArrayList<>());

        // Create one reporting data
        ReportingDataUE reportingDataUE = new ReportingDataUE();
        reportingDataUE.setIdentifier("Q123456");
        reportingDataUE.setInterviewerId("INT001");
        reportingDataUE.setOrganizationUnitId("ORG001");

        reportingData.getListReportingDataUE().add(reportingDataUE);
    }

    @Test
    void testIntegrateReportingDataIntoUE() {
        // Check structure
        assertNull(surveyRawData.getMetadataModel().getGroup(Constants.REPORTING_DATA_GROUP_NAME));

        reportingDataParser.integrateReportingDataIntoUE(surveyRawData, reportingData, true, null);

        // Check reporting group correctly added
        Group reportingGroup = surveyRawData.getMetadataModel().getGroup(Constants.REPORTING_DATA_GROUP_NAME);
        assertNotNull(reportingGroup);

        // Check variables
        Map<String, Variable> variables = surveyRawData.getMetadataModel().getVariables().getVariables();
        assertTrue(variables.containsKey(Constants.INTERVIEWER_ID_NAME));
        assertTrue(variables.containsKey(Constants.ORGANIZATION_UNIT_ID_NAME));

        // Check variable's types
        assertEquals(VariableType.STRING, variables.get(Constants.INTERVIEWER_ID_NAME).getType());
        assertEquals(VariableType.STRING, variables.get(Constants.ORGANIZATION_UNIT_ID_NAME).getType());
    }

    @Test
    void testAddReportingValues() {
        // Add questionnaire
        QuestionnaireData questionnaire = new QuestionnaireData();
        questionnaire.setIdentifier("Q123456");
        surveyRawData.addQuestionnaire(questionnaire);

        // Add reporting data
        reportingDataParser.integrateReportingDataIntoUE(surveyRawData, reportingData, true, null);

        // THEN
        assertEquals("INT001", questionnaire.getAnswers().getSubGroup(Constants.REPORTING_DATA_GROUP_NAME)
                .getInstance(Constants.REPORTING_DATA_PREFIX_NAME + "Q123456")
                .getValue(Constants.INTERVIEWER_ID_NAME));

        assertEquals("ORG001", questionnaire.getAnswers().getSubGroup(Constants.REPORTING_DATA_GROUP_NAME)
                .getInstance(Constants.REPORTING_DATA_PREFIX_NAME + "Q123456")
                .getValue(Constants.ORGANIZATION_UNIT_ID_NAME));
    }

    @Test
    void testCountMaxStates() {
        // Add other reporting data to count
        ReportingDataUE ue1 = new ReportingDataUE();
        ue1.getStates().add(new State("STATE1", 1650000000000L));

        ReportingDataUE ue2 = new ReportingDataUE();
        ue2.getStates().add(new State("STATE1", 1650000000000L));
        ue2.getStates().add(new State("STATE2", 1650005000000L));

        reportingData.getListReportingDataUE().add(ue1);
        reportingData.getListReportingDataUE().add(ue2);

        assertEquals(2, reportingDataParser.countMaxStates(reportingData));
    }
}
