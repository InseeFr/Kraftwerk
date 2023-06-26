package fr.insee.kraftwerk.core.extradata;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import fr.insee.kraftwerk.core.TestConstants;
import fr.insee.kraftwerk.core.exceptions.KraftwerkException;
import fr.insee.kraftwerk.core.exceptions.NullException;
import fr.insee.kraftwerk.core.extradata.paradata.Paradata;
import fr.insee.kraftwerk.core.extradata.paradata.ParadataParser;
import fr.insee.kraftwerk.core.metadata.VariablesMap;
import fr.insee.kraftwerk.core.rawdata.QuestionnaireData;
import fr.insee.kraftwerk.core.rawdata.SurveyRawData;

class ParaDataParserTest {

	ParadataParser paradataParser = new ParadataParser();

	@Test
	void catchExceptionAndErrors() {

		//Null SurveyRawdata
		Exception exception = assertThrows(NullPointerException.class, () -> {
			paradataParser.parseParadata(null, null);
		});

		String expectedMessage = "Cannot invoke \"fr.insee.kraftwerk.core.rawdata.SurveyRawData.getIdSurveyUnits()\" because \"surveyRawData\" is null";
		String actualMessage = exception.getMessage();

		assertTrue(actualMessage.contains(expectedMessage));

		//Null Paradata
		SurveyRawData srd = new SurveyRawData();
		exception = assertThrows(NullPointerException.class, () -> {
			paradataParser.parseParadata(null, srd);
		});

		expectedMessage = "Cannot invoke \"fr.insee.kraftwerk.core.extradata.paradata.Paradata.getFilepath()\" because \"paradata\" is null";
		actualMessage = exception.getMessage();

		assertTrue(actualMessage.contains(expectedMessage));
		
		//Empty paradata and empty SurveyRawData
		Paradata paradata = new Paradata();
		exception = assertThrows(NullException.class, () -> {
			paradataParser.parseParadata(paradata, srd);
		});

		expectedMessage = "JSONFile not defined";
		actualMessage = exception.getMessage();
		int actualStatus = ((KraftwerkException) exception).getStatus();
		assertEquals(500, actualStatus);
		assertTrue(actualMessage.contains(expectedMessage));
		
		//assert that do nothng without file
		paradata.setFilepath(Path.of(""));
		assertDoesNotThrow(() -> paradataParser.parseParadata(paradata, srd));
		
	}
	
	@Test
	void whenCorrectParadata_thenCalculatedTimeIsOk() throws NullException {
		SurveyRawData srd = new SurveyRawData();
		srd.setVariablesMap(new VariablesMap());

		srd = addIdToTest(srd, "PL1100000101");
		srd = addIdToTest(srd, "RR100144");
		
		Paradata paradata = new Paradata();
		paradata.setFilepath(Paths.get(TestConstants.UNIT_TESTS_DIRECTORY + "/paradata"));
		paradataParser.parseParadata(paradata, srd);
		
		//SESSIONS
		assertEquals(1, paradata.getParadataUE("PL1100000101").getSessions().size());
		assertEquals(135102, paradata.getParadataUE("PL1100000101").createLengthSessionsVariable());

		//ORCHESTRATORS
		assertEquals(2, paradata.getParadataUE("PL1100000101").getOrchestrators().size());
		assertEquals(134671, paradata.getParadataUE("PL1100000101").createLengthOrchestratorsVariable());

		
		//SESSIONS
		assertEquals(5, paradata.getParadataUE("RR100144").getSessions().size());
		assertEquals(6058470, paradata.getParadataUE("RR100144").createLengthSessionsVariable());

		//ORCHESTRATORS
		assertEquals(6, paradata.getParadataUE("RR100144").getOrchestrators().size());
		assertEquals(2577505436L, paradata.getParadataUE("RR100144").createLengthOrchestratorsVariable());

	}
	
	private SurveyRawData addIdToTest(SurveyRawData srd, String idToAdd) {
		List<String> ids = srd.getIdSurveyUnits();
		if (ids == null) ids = new ArrayList<>();
		ids.add(idToAdd);
		srd.setIdSurveyUnits(ids);
		
		QuestionnaireData qd = new QuestionnaireData();
		qd.setIdentifier(idToAdd);
		srd.addQuestionnaire(qd);
		return srd;
	}

}
