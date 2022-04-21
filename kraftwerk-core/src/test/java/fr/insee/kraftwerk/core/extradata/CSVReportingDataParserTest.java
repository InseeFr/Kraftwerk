package fr.insee.kraftwerk.core.extradata;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.file.Paths;

import fr.insee.kraftwerk.core.rawdata.SurveyRawDataTest;
import org.junit.jupiter.api.Test;

import fr.insee.kraftwerk.core.Constants;
import fr.insee.kraftwerk.core.TestConstants;
import fr.insee.kraftwerk.core.extradata.reportingdata.ReportingData;
import fr.insee.kraftwerk.core.extradata.reportingdata.ReportingDataUE;
import fr.insee.kraftwerk.core.extradata.reportingdata.CSVReportingDataParser;
import fr.insee.kraftwerk.core.rawdata.SurveyRawData;

public class CSVReportingDataParserTest {

	@Test
	public void parseReportingDataTest() {
		CSVReportingDataParser csvReportingDataParser = new CSVReportingDataParser();

		SurveyRawData data = SurveyRawDataTest.createFakePapiSurveyRawData();
		ReportingData reportingData = new ReportingData(
				Paths.get(TestConstants.UNIT_TESTS_DIRECTORY + "/reportingdata/reportingdata.csv"));
		csvReportingDataParser.parseReportingData(reportingData, data);

		// Check the reporting data is well captured
		assertEquals(3, reportingData.getListReportingDataUE().size());
		ReportingDataUE reportingDataUE = reportingData.getListReportingDataUE().stream()
				.filter(reportingDataUEToSearch -> "L0000169".equals(reportingDataUEToSearch.getIdentifier())).findAny()
				.orElse(null);
		// Check the reporting data's values are well captured
		// Second state of the first UE
		
		assertEquals("INITLA", reportingDataUE.getStates().get(0).getStateType());
		assertEquals("PARTIELINT", reportingDataUE.getStates().get(1).getStateType());
		assertEquals("VALINT", reportingDataUE.getStates().get(2).getStateType());
		// Check the reporting data is correctly translated in the output
		/* à implémenter ? *//*
		assertEquals("Affectée, non visible enquêteur", questionnaire.getAnswers().getValue("STATE_2"));
		assertEquals("Questionnaire démarré", questionnaire.getAnswers().getValue(Constants.LAST_STATE_NAME));*/
	}

	@Test
	public void controlHeaderTest() {
		CSVReportingDataParser csvReportingDataParser = new CSVReportingDataParser();
		String[] validHeaderToControl = new String [] {"statut", "dateInfo", "idUe", "idContact", "nom", "prenom", "adresse", "numeroDeLot"};
		String[] invalidHeaderWrongValues = new String [] {"statut", "dateInfo", "idUe2", "idContact", "nom", "prenom", "adresse2", "numeroDeLot2"};
		String[] headerToControlWrongSize = new String [] {"statut", "dateInfo", "idUe", "idContact", "nom", "prenom", "adresse", "numeroDeLot", "ninth"};
		
		assertTrue(csvReportingDataParser.controlHeader(validHeaderToControl));
		assertFalse(csvReportingDataParser.controlHeader(invalidHeaderWrongValues));
		assertFalse(csvReportingDataParser.controlHeader(headerToControlWrongSize));
	}

	@Test
	public void convertDateTest() {
		CSVReportingDataParser csvReportingDataParser = new CSVReportingDataParser();
		assertEquals(1645007098, csvReportingDataParser.convertToTimestamp("16/02/2022 11:24:58"));
		assertEquals(1566544132, csvReportingDataParser.convertToTimestamp("23/08/2019 09:08:52"));
		assertEquals(1111111111, csvReportingDataParser.convertToTimestamp("18/03/2005 02:58:31"));
		assertEquals(1, csvReportingDataParser.convertToTimestamp("01/01/1970 01:00:01"));
		
	}

}
