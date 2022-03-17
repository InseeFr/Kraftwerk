package fr.insee.kraftwerk.extradata;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import fr.insee.kraftwerk.Constants;
import fr.insee.kraftwerk.TestConstants;
import fr.insee.kraftwerk.extradata.reportingdata.ReportingData;
import fr.insee.kraftwerk.extradata.reportingdata.XMLReportingDataParser;
import fr.insee.kraftwerk.rawdata.QuestionnaireData;
import fr.insee.kraftwerk.rawdata.SurveyRawData;
import fr.insee.kraftwerk.rawdata.SurveyRawDataTest;

public class XMLReportingDataParserTest {

	@Disabled("waiting test reporting data file")
	@Test
	public void parseReportingDataTest() {
		XMLReportingDataParser xMLReportingDataParser = new XMLReportingDataParser();

		SurveyRawData data = SurveyRawDataTest.createFakePapiSurveyRawData();
		ReportingData reportingData = new ReportingData(
				Constants.getResourceAbsolutePath(TestConstants.UNIT_TESTS_DIRECTORY + "/reportingdata/reportingdata.xml"));
		xMLReportingDataParser.parseReportingData(reportingData, data);

		// Check the reporting data is well captured
		assertEquals(4, reportingData.getListReportingDataUE().size());
		QuestionnaireData questionnaire = data.getQuestionnaires().stream()
				.filter(questionnaireToSearch -> "L0000005".equals(questionnaireToSearch.getIdentifier())).findAny()
				.orElse(null);

		// Check the reporting data's values are well captured
		// Second state of the first UE
		assertEquals("ANV", reportingData.getListReportingDataUE().get(0).getStates().get(1).getStateType());
		// Check the reporting data is correctly translated in the output
		assertEquals("Affectée, non visible enquêteur", questionnaire.getAnswers().getValue("STATE_2"));
		assertEquals("Questionnaire démarré", questionnaire.getAnswers().getValue(Constants.LAST_STATE_NAME));
	}

	@Test
	public void maxTest() {
		XMLReportingDataParser xMLReportingDataParser = new XMLReportingDataParser();
		ReportingData reportingData = new ReportingData();
		reportingData.putReportingDataUE(ReportingDataUETest.createFakeReportingDataUEs());
		assertEquals(8, xMLReportingDataParser.max(reportingData));
	}

}
