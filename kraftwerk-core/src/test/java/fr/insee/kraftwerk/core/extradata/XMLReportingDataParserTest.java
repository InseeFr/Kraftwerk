package fr.insee.kraftwerk.core.extradata;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.file.Paths;

import fr.insee.kraftwerk.core.rawdata.SurveyRawDataTest;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import fr.insee.kraftwerk.core.Constants;
import fr.insee.kraftwerk.core.TestConstants;
import fr.insee.kraftwerk.core.exceptions.NullException;
import fr.insee.kraftwerk.core.extradata.reportingdata.ReportingData;
import fr.insee.kraftwerk.core.extradata.reportingdata.XMLReportingDataParser;
import fr.insee.kraftwerk.core.rawdata.QuestionnaireData;
import fr.insee.kraftwerk.core.rawdata.SurveyRawData;

public class XMLReportingDataParserTest {

	@Disabled("waiting test reporting data file")
	@Test
	public void parseReportingDataTest() {
		XMLReportingDataParser xMLReportingDataParser = new XMLReportingDataParser();

		SurveyRawData data = SurveyRawDataTest.createFakePapiSurveyRawData();
		ReportingData reportingData = new ReportingData(
				Paths.get(TestConstants.UNIT_TESTS_DIRECTORY + "/reportingdata/reportingdata.xml"));
		try {
			xMLReportingDataParser.parseReportingData(reportingData, data);
		} catch (NullException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

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
		assertEquals(8, xMLReportingDataParser.maxStates(reportingData));
	}

}
