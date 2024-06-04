package fr.insee.kraftwerk.core.extradata;

import fr.insee.kraftwerk.core.TestConstants;
import fr.insee.kraftwerk.core.exceptions.KraftwerkException;
import fr.insee.kraftwerk.core.extradata.reportingdata.CSVReportingDataParser;
import fr.insee.kraftwerk.core.extradata.reportingdata.ReportingData;
import fr.insee.kraftwerk.core.extradata.reportingdata.ReportingDataUE;
import fr.insee.kraftwerk.core.extradata.reportingdata.State;
import fr.insee.kraftwerk.core.rawdata.SurveyRawData;
import fr.insee.kraftwerk.core.rawdata.SurveyRawDataTest;
import fr.insee.kraftwerk.core.utils.SqlUtils;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.nio.file.Paths;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

class CSVReportingDataParserTest {

	@Test
	 void parseReportingDataTest() throws KraftwerkException {
		CSVReportingDataParser csvReportingDataParser = new CSVReportingDataParser();

		SurveyRawData data = SurveyRawDataTest.createFakePapiSurveyRawData();
		ReportingData reportingData = new ReportingData(
				Paths.get(TestConstants.UNIT_TESTS_DIRECTORY + "/reportingdata/reportingdata.csv"));
		try (Statement database = SqlUtils.openConnection().createStatement()){
			csvReportingDataParser.parseReportingData(reportingData, data, true,database);
		} catch (SQLException e){
			throw new KraftwerkException(500,"SQL error");
		}

        // Check the reporting data is well captured
		Assertions.assertThat(reportingData.getListReportingDataUE().size()).isEqualTo(3);
		ReportingDataUE reportingDataUE = reportingData.getListReportingDataUE().stream()
				.filter(reportingDataUEToSearch -> "L0000169".equals(reportingDataUEToSearch.getIdentifier())).findAny()
				.orElse(null);
		// Check the reporting data's values are well captured
		// Second state of the first UE

        assert reportingDataUE != null;
		Set<String> stateTypes = new HashSet<>();
		for(State state : reportingDataUE.getStates()){
			stateTypes.add(state.getStateType());
		}
		Assertions.assertThat(stateTypes).contains("INITLA","VALINT","PARTIELINT");

		// Check the reporting data is correctly translated in the output
		/* à implémenter ? *//*
		"Affectée, non visible enquêteur" et "Questionnaire démarré"*/
	}

	@Test
	void controlHeaderTest() {
		CSVReportingDataParser csvReportingDataParser = new CSVReportingDataParser();
		List<String> validHeaderToControl = new ArrayList<>(List.of(new String[]{"statut", "dateInfo", "idUe", "idContact", "nom", "prenom", "adresse", "numeroDeLot"}));
		List<String> invalidHeaderWrongValues = new ArrayList<>(List.of(new String [] {"statut", "dateInfo", "idUe2", "idContact", "nom", "prenom", "adresse2", "numeroDeLot2"}));
		List<String> headerToControlWrongSize = new ArrayList<>(List.of(new String [] {"statut", "dateInfo", "idUe", "idContact", "nom", "prenom", "adresse", "numeroDeLot", "ninth"}));
		
		Assertions.assertThat(csvReportingDataParser.controlHeader(validHeaderToControl)).isTrue();
		Assertions.assertThat(csvReportingDataParser.controlHeader(invalidHeaderWrongValues)).isFalse();
		Assertions.assertThat(csvReportingDataParser.controlHeader(headerToControlWrongSize)).isFalse();
	}

	@Test
	void convertDateTest() {
		CSVReportingDataParser csvReportingDataParser = new CSVReportingDataParser();
		Assertions.assertThat(csvReportingDataParser.convertToTimestamp("2022-02-16 11:24:58.0")).isEqualTo(1645007098);
		Assertions.assertThat(csvReportingDataParser.convertToTimestamp("2019-08-23 09:08:52.0")).isEqualTo(1566544132);
		Assertions.assertThat(csvReportingDataParser.convertToTimestamp("2005-03-18 02:58:31.0")).isEqualTo(1111111111);
		Assertions.assertThat(csvReportingDataParser.convertToTimestamp("1970-01-01 01:00:01.0")).isEqualTo(1);
	}

}
