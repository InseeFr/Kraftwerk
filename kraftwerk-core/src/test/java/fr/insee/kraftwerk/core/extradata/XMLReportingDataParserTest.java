package fr.insee.kraftwerk.core.extradata;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.file.Paths;

import org.junit.jupiter.api.Test;

import fr.insee.kraftwerk.core.Constants;
import fr.insee.kraftwerk.core.TestConstants;
import fr.insee.kraftwerk.core.exceptions.NullException;
import fr.insee.kraftwerk.core.extradata.reportingdata.ReportingData;
import fr.insee.kraftwerk.core.extradata.reportingdata.XMLReportingDataParser;
import fr.insee.kraftwerk.core.rawdata.QuestionnaireData;
import fr.insee.kraftwerk.core.rawdata.SurveyRawData;
import fr.insee.kraftwerk.core.rawdata.SurveyRawDataTest;

class XMLReportingDataParserTest {

	@Test
	void parseReportingDataTest() {
		XMLReportingDataParser xMLReportingDataParser = new XMLReportingDataParser();

		SurveyRawData data = SurveyRawDataTest.createFakePapiSurveyRawData();
		ReportingData reportingData = new ReportingData(
				Paths.get(TestConstants.UNIT_TESTS_DIRECTORY + "/reportingdata/reportingdata.xml"));
		try {
			xMLReportingDataParser.parseReportingData(reportingData, data, true);
		} catch (NullException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// Check the reporting data is well captured
		assertEquals(12, reportingData.getListReportingDataUE().size());
		QuestionnaireData questionnaire = data.getQuestionnaires().stream()
				.filter(questionnaireToSearch -> "TNL1102000275".equals(questionnaireToSearch.getIdentifier())).findAny()
				.orElse(null);

		// Check the reporting data's values are well captured
		// Second state of the first UE
		assertEquals("ANV", reportingData.getListReportingDataUE().get(0).getStates().get(1).getStateType());
		// Check the reporting data is correctly translated in the output
		assertEquals("Affectée, non visible enquêteur", questionnaire.getAnswers().getSubGroup(Constants.REPORTING_DATA_GROUP_NAME).getInstance(Constants.REPORTING_DATA_PREFIX_NAME + "TNL1102000275").getValue("STATE_2"));
		assertEquals("UE finalisée", questionnaire.getAnswers().getSubGroup(Constants.REPORTING_DATA_GROUP_NAME).getInstance(Constants.REPORTING_DATA_PREFIX_NAME + "TNL1102000275").getValue(Constants.LAST_STATE_NAME));

		// Null interviewrIds checks
		//Check interviewerId placeholder value on TNL1102000278
		questionnaire = data.getQuestionnaires().stream()
				.filter(questionnaireToSearch -> "TNL1102000278".equals(questionnaireToSearch.getIdentifier())).findAny()
				.orElse(null);
        assertEquals(Constants.REPORTING_DATA_INTERVIEWER_ID_NULL_PLACEHOLDER + "_01", questionnaire.getAnswers().getSubGroup(Constants.REPORTING_DATA_GROUP_NAME).getInstance(Constants.REPORTING_DATA_PREFIX_NAME + "TNL1102000278").getValue(Constants.INTERVIEWER_ID_NAME));

		//Check interviewerId placeholder incrementation on TNL1102000279
		questionnaire = data.getQuestionnaires().stream()
				.filter(questionnaireToSearch -> "TNL1102000279".equals(questionnaireToSearch.getIdentifier())).findAny()
				.orElse(null);
		assertEquals(Constants.REPORTING_DATA_INTERVIEWER_ID_NULL_PLACEHOLDER + "_02", questionnaire.getAnswers().getSubGroup(Constants.REPORTING_DATA_GROUP_NAME).getInstance(Constants.REPORTING_DATA_PREFIX_NAME + "TNL1102000279").getValue(Constants.INTERVIEWER_ID_NAME));

	}

	@Test
	void maxTest() {
		XMLReportingDataParser xMLReportingDataParser = new XMLReportingDataParser();
		ReportingData reportingData = new ReportingData();
		reportingData.putReportingDataUE(ReportingDataUETest.createFakeReportingDataUEs());
		assertEquals(5, xMLReportingDataParser.countMaxStates(reportingData)); //remove double
	}

}
