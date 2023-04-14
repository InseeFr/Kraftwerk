package fr.insee.kraftwerk.core.extradata;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import fr.insee.kraftwerk.core.Constants;
import fr.insee.kraftwerk.core.TestConstants;
import fr.insee.kraftwerk.core.exceptions.KraftwerkException;
import fr.insee.kraftwerk.core.exceptions.NullException;
import fr.insee.kraftwerk.core.extradata.paradata.Event;
import fr.insee.kraftwerk.core.extradata.paradata.ParaDataUE;
import fr.insee.kraftwerk.core.extradata.paradata.Paradata;
import fr.insee.kraftwerk.core.extradata.paradata.ParadataParser;
import fr.insee.kraftwerk.core.metadata.DDIReader;
import fr.insee.kraftwerk.core.rawdata.SurveyRawData;
import fr.insee.kraftwerk.core.rawdata.SurveyRawDataTest;

class ParaDataParserTest {

	@Disabled("waiting for some paradata test files")
	@Test
	void parseParaDataTest() throws KraftwerkException {

		ParadataParser paraDataParser = new ParadataParser();

		Paradata paraData = new Paradata();
		SurveyRawData srdTest = SurveyRawDataTest.createFakeCawiSurveyRawData();

		try {
			srdTest.setVariablesMap(DDIReader.getVariablesFromDDI(new URL(
					"https://gitlab.insee.fr/enquetes-menages/integration-metallica/-/raw/master/Logement/LOG2021T01/S1logement13juil_ddi.xml")));
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		paraData.setFilepath(Paths.get(TestConstants.UNIT_TESTS_DIRECTORY + "/paradata/LOG2021T01"));
		try {
			paraDataParser.parseParadata(paraData, srdTest);
		} catch (NullException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// Test we get the raw data correctly
		assertEquals("init-session", paraData.getListParadataUE().get(1).getEvents().get(0).getIdParadataObject());
		// Test we get the raw data correctly
		assertEquals(13, paraData.getParadataUE("S00000005").getParadataVariable("PRENOM").size());
		assertEquals(2, paraData.getParadataUE("S00000001").getSessions().size());
		assertEquals(1, paraData.getParadataUE("S00000002").getSessions().size());
		assertEquals(1, paraData.getParadataUE("L0000004").getSessions().size());
		// Test we get each file in the paradata folder
		assertEquals(8, paraData.getListParadataUE().size());
		// Test we get the final value correctly
		assertEquals("0 jours, 01:24:25",
				srdTest.getQuestionnaires().get(1).getAnswers().getValue(Constants.LENGTH_ORCHESTRATORS_NAME));
	}

	@Disabled("waiting for some paradata test files")
	@Test
	void createOrchestratorsAndSessionsTest() {
		ParaDataUE paraDataUE = new ParaDataUE();
		paraDataUE.setFilepath(Paths.get(TestConstants.UNIT_TESTS_DIRECTORY
				+ "/paradata/LOG2021T01/paradata.complete.LOG2021T01.S00000001.Example.json"));
		paraDataUE.setIdentifier("S00000001");

		SurveyRawData srdTest = SurveyRawDataTest.createFakeCawiSurveyRawData();
		ParadataParser paraDataParser = new ParadataParser();
		try {
			paraDataParser.parseParadataUE(paraDataUE, srdTest);
			paraDataParser.integrateParaDataVariablesIntoUE(paraDataUE, srdTest);
			paraDataUE.sortEvents();
			paraDataUE.createOrchestratorsAndSessions();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		assertEquals(1, paraDataUE.getParadataVariable("FIRST_NAME").size());
		assertEquals(2, paraDataUE.getSessions().size());
		assertEquals(3, paraDataUE.getOrchestrators().size());
		assertEquals(1626775230972L, paraDataUE.getOrchestrators().get(0).getInitialization());
		assertEquals(1627373911800L, paraDataUE.getOrchestrators().get(2).getValidation());
		// Test we get the raw data correctly
	}


	@Test
	void sortTest() {
		ParaDataUE paraDataUE = new ParaDataUE();
		Event e1 = new Event("s1");
		e1.setIdParadataObject("A");
		e1.setTimestamp(1);

		Event e2 = new Event("s1");
		e2.setIdParadataObject("A");
		e2.setTimestamp(2);

		Event e3 = new Event("s1");
		e3.setIdParadataObject("B");
		e3.setTimestamp(1);
		
		Event e4 = new Event("s2"); //same as e3, but with different id => remove in final list
		e4.setIdParadataObject("B");
		e4.setTimestamp(1);


		List<Event> events = new ArrayList<>();
		events.add(e3);
		events.add(e1);
		events.add(e2);
		events.add(e4);

		paraDataUE.setEvents(events);

		paraDataUE.sortEvents();

		List<Event> sortedList = paraDataUE.getEvents();
		assertEquals(3, sortedList.size());
		
		assertEquals("A", sortedList.get(0).getIdParadataObject());
		assertEquals(1, sortedList.get(0).getTimestamp());
		assertEquals("B", sortedList.get(1).getIdParadataObject());
		assertEquals(1, sortedList.get(1).getTimestamp());
		assertEquals("A", sortedList.get(2).getIdParadataObject());
		assertEquals(2, sortedList.get(2).getTimestamp());
	}
	
}
