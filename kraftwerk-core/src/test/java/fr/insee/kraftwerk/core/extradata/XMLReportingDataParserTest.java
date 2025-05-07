package fr.insee.kraftwerk.core.extradata;

import fr.insee.kraftwerk.core.Constants;
import fr.insee.kraftwerk.core.TestConstants;
import fr.insee.kraftwerk.core.exceptions.KraftwerkException;
import fr.insee.kraftwerk.core.exceptions.NullException;
import fr.insee.kraftwerk.core.extradata.reportingdata.ReportingData;
import fr.insee.kraftwerk.core.extradata.reportingdata.ReportingDataUE;
import fr.insee.kraftwerk.core.extradata.reportingdata.XMLReportingDataParser;
import fr.insee.kraftwerk.core.rawdata.QuestionnaireData;
import fr.insee.kraftwerk.core.rawdata.SurveyRawData;
import fr.insee.kraftwerk.core.rawdata.SurveyRawDataTest;
import fr.insee.kraftwerk.core.utils.files.FileSystemImpl;
import fr.insee.kraftwerk.core.utils.files.FileUtilsInterface;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class XMLReportingDataParserTest {

	private final FileUtilsInterface fileUtilsInterface = new FileSystemImpl(TestConstants.TEST_RESOURCES_DIRECTORY);

	@Test
	void parseReportingDataTest() throws KraftwerkException {
		XMLReportingDataParser xMLReportingDataParser = new XMLReportingDataParser(fileUtilsInterface);

		SurveyRawData data = SurveyRawDataTest.createFakePapiSurveyRawData();
		ReportingData reportingData = new ReportingData(
				Paths.get(TestConstants.UNIT_TESTS_DIRECTORY + "/reportingdata/reportingdata.xml"), new ArrayList<>());
			xMLReportingDataParser.parseReportingData(reportingData, data, true);


		// Check the reporting data is well captured
		assertEquals(12, reportingData.getListReportingDataUE().size());
		QuestionnaireData questionnaire = data.getQuestionnaires().stream()
				.filter(questionnaireToSearch -> "TNL1102000275".equals(questionnaireToSearch.getIdentifier())).findAny()
				.orElse(null);

		// Check the reporting data's values are well captured
		// Second state of the first UE
		assertEquals("ANV", reportingData.getListReportingDataUE().get(0).getStates().get(1).getStateType());
		// Check the reporting data is correctly reported in the output (no more transformation of modalities)
		assertEquals("ANV", questionnaire.getAnswers().getSubGroup(Constants.REPORTING_DATA_GROUP_NAME).getInstance(Constants.REPORTING_DATA_PREFIX_NAME + "TNL1102000275").getValue("STATE_2"));
		assertEquals("FIN", questionnaire.getAnswers().getSubGroup(Constants.REPORTING_DATA_GROUP_NAME).getInstance(Constants.REPORTING_DATA_PREFIX_NAME + "TNL1102000275").getValue(Constants.LAST_STATE_NAME));

		// Null interviewrIds checks
		//Check interviewerId placeholder value on TNL1102000278
		questionnaire = data.getQuestionnaires().stream()
				.filter(questionnaireToSearch -> "TNL1102000278".equals(questionnaireToSearch.getIdentifier())).findAny()
				.orElse(null);
        assertEquals(Constants.REPORTING_DATA_INTERVIEWER_ID_NULL_PLACEHOLDER + "TNL1102000278", questionnaire.getAnswers().getSubGroup(Constants.REPORTING_DATA_GROUP_NAME).getInstance(Constants.REPORTING_DATA_PREFIX_NAME + "TNL1102000278").getValue(Constants.INTERVIEWER_ID_NAME));

		//Check interviewerId placeholder incrementation on TNL1102000279
		questionnaire = data.getQuestionnaires().stream()
				.filter(questionnaireToSearch -> "TNL1102000279".equals(questionnaireToSearch.getIdentifier())).findAny()
				.orElse(null);
		assertEquals(Constants.REPORTING_DATA_INTERVIEWER_ID_NULL_PLACEHOLDER + "TNL1102000279", questionnaire.getAnswers().getSubGroup(Constants.REPORTING_DATA_GROUP_NAME).getInstance(Constants.REPORTING_DATA_PREFIX_NAME + "TNL1102000279").getValue(Constants.INTERVIEWER_ID_NAME));

	}

	@Test
	void parseMoogReportingDataTest() throws KraftwerkException {
		XMLReportingDataParser xMLReportingDataParser = new XMLReportingDataParser(fileUtilsInterface);

		SurveyRawData data = SurveyRawDataTest.createFakePapiSurveyRawData();
		ReportingData reportingData = new ReportingData(
				Paths.get(TestConstants.UNIT_TESTS_DIRECTORY + "/reportingdata/reportingdatamoog.xml"), new ArrayList<>());
			xMLReportingDataParser.parseReportingData(reportingData, data, true);


		// Check the reporting data is well captured
		assertEquals(101, reportingData.getListReportingDataUE().size());
		String interrogationId = "BLA000010";
		QuestionnaireData questionnaire = data.getQuestionnaires().stream()
				.filter(questionnaireToSearch -> interrogationId.equals(questionnaireToSearch.getIdentifier())).findAny()
				.orElse(null);

		// Check the reporting data's values are well captured
		// Second state of the first UE
		assertEquals("REFUSAL", reportingData.getListReportingDataUE().getFirst().getStates().get(1).getStateType());
		// Check the reporting data is correctly reported in the output (no more transformation of modalities)
		assertEquals("INITLA", questionnaire.getAnswers().getSubGroup(Constants.REPORTING_DATA_GROUP_NAME).getInstance(Constants.REPORTING_DATA_PREFIX_NAME + interrogationId).getValue("STATE_1"));



		// Survey validation date checks
		// Case 1 VALPAP only
		questionnaire = data.getQuestionnaires().stream()
				.filter(questionnaireToSearch -> "BLA000001".equals(questionnaireToSearch.getIdentifier())).findAny()
				.orElse(null);
        assert questionnaire != null;
        assertNotEquals(null, questionnaire.getAnswers().getSubGroup(Constants.REPORTING_DATA_GROUP_NAME).getInstance(Constants.REPORTING_DATA_PREFIX_NAME + "BLA000001").getValue(Constants.REPORTING_DATA_SURVEY_VALIDATION_NAME));
		ReportingDataUE reportingDataUE = reportingData.getListReportingDataUE().stream().filter(
				reportingDataUEtmp -> reportingDataUEtmp.getIdentifier().equals("BLA000001")
		).findAny().orElse(null);
        assert reportingDataUE != null;
        assertEquals(1696418304931L, reportingDataUE.getSurveyValidationDateTimeStamp());

		// Case 2 VALINT only
		reportingDataUE = reportingData.getListReportingDataUE().stream().filter(
				reportingDataUEtmp -> reportingDataUEtmp.getIdentifier().equals("BLA000002")
		).findAny().orElse(null);
		assert reportingDataUE != null;
		assertEquals(1696418304931L, reportingDataUE.getSurveyValidationDateTimeStamp());

		// Case 3 PARTIELINT only
		reportingDataUE = reportingData.getListReportingDataUE().stream().filter(
				reportingDataUEtmp -> reportingDataUEtmp.getIdentifier().equals("BLA000003")
		).findAny().orElse(null);
		assert reportingDataUE != null;
		assertEquals(1696418304931L, reportingDataUE.getSurveyValidationDateTimeStamp());

		// Case 4 VALINT over others
		reportingDataUE = reportingData.getListReportingDataUE().stream().filter(
				reportingDataUEtmp -> reportingDataUEtmp.getIdentifier().equals("BLA000004")
		).findAny().orElse(null);
		assert reportingDataUE != null;
		assertEquals(1696618304931L, reportingDataUE.getSurveyValidationDateTimeStamp());

		// Case 5 VALINT over PARTIELINT
		reportingDataUE = reportingData.getListReportingDataUE().stream().filter(
				reportingDataUEtmp -> reportingDataUEtmp.getIdentifier().equals("BLA000005")
		).findAny().orElse(null);
		assert reportingDataUE != null;
		assertEquals(1696418304931L, reportingDataUE.getSurveyValidationDateTimeStamp());

		// Case 6 VALPAP over PARTIELINT
		reportingDataUE = reportingData.getListReportingDataUE().stream().filter(
				reportingDataUEtmp -> reportingDataUEtmp.getIdentifier().equals("BLA000006")
		).findAny().orElse(null);
		assert reportingDataUE != null;
		assertEquals(1696418304931L, reportingDataUE.getSurveyValidationDateTimeStamp());

		//Case 7 VALINT over VALPAP
		reportingDataUE = reportingData.getListReportingDataUE().stream().filter(
				reportingDataUEtmp -> reportingDataUEtmp.getIdentifier().equals("BLA000007")
		).findAny().orElse(null);
		assert reportingDataUE != null;
		assertEquals(1696418304931L, reportingDataUE.getSurveyValidationDateTimeStamp());

		//Case 8 2 VALINTs
		reportingDataUE = reportingData.getListReportingDataUE().stream().filter(
				reportingDataUEtmp -> reportingDataUEtmp.getIdentifier().equals("BLA000008")
		).findAny().orElse(null);
		assert reportingDataUE != null;
		assertEquals(1696618304931L, reportingDataUE.getSurveyValidationDateTimeStamp());
	}

	@Test
	void maxTest() {
		XMLReportingDataParser xMLReportingDataParser = new XMLReportingDataParser(fileUtilsInterface);
		ReportingData reportingData = new ReportingData(Path.of("test"), new ArrayList<>());
		reportingData.putReportingDataUE(ReportingDataUETest.createFakeReportingDataUEs());
		assertEquals(5, xMLReportingDataParser.countMaxStates(reportingData)); //remove double
	}

	@Test
	void maxTest_noData() {
		XMLReportingDataParser xMLReportingDataParser = new XMLReportingDataParser(fileUtilsInterface);
		ReportingData reportingData = new ReportingData(Path.of("test"), new ArrayList<>());
		assertEquals(0, xMLReportingDataParser.countMaxStates(reportingData));
		assertEquals(0, xMLReportingDataParser.countMaxAttempts(reportingData));
		assertEquals(0, xMLReportingDataParser.countMaxComments(reportingData));
	}
}
