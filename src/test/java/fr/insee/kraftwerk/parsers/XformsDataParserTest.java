
package fr.insee.kraftwerk.parsers;


import fr.insee.kraftwerk.TestConstants;
import fr.insee.kraftwerk.metadata.DDIReader;
import fr.insee.kraftwerk.rawdata.GroupData;
import fr.insee.kraftwerk.rawdata.QuestionnaireData;
import fr.insee.kraftwerk.rawdata.SurveyRawData;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class XformsDataParserTest {
	
	@Test
	public void parseSimpsonsV1Data() {
		//
		SurveyRawData data = new SurveyRawData();
		data.setVariablesMap(DDIReader.getVariablesFromDDI(TestConstants.SIMPSONS_V1_DDI));
		data.setDataFilePath(TestConstants.SIMPSONS_V1_XFORMS_DATA);
		XformsDataParser parser = new XformsDataParser();
		parser.parseSurveyData(data);

		//
		assertEquals(19, data.getQuestionnairesCount());
		//
		QuestionnaireData q1 = data.getQuestionnaires().get(0);
		assertEquals("SP0000009", q1.getIdentifier());
		assertEquals("Simpsons, not simspons", q1.getValue("COMMENT"));
		assertNull(q1.getValue("FAVOURITE_CHARACTERS31"));
	}

	@Test
	public void parseVqsWebData() {
		//
		SurveyRawData data = new SurveyRawData();
		data.setVariablesMap(DDIReader.getVariablesFromDDI(TestConstants.VQS_WEB_DDI));
		data.setDataFilePath(TestConstants.VQS_WEB_DATA);
		XformsDataParser parser = new XformsDataParser();
		parser.parseSurveyData(data);

		//
		assertEquals(31, data.getQuestionnairesCount());
		//
		QuestionnaireData q1 = data.getQuestionnaires().get(0);
		assertEquals("VQS9586544", q1.getIdentifier());
		assertEquals("73  OTM BSAHEGJR   53587 NPCUS", q1.getValue("ADRESSE"));
		GroupData q1Loop = q1.getAnswers().getSubGroup("BOUCLEINDIV");
		assertEquals("Edyydo", q1Loop.getValue("PRENOM", "BOUCLEINDIV-1"));
		assertEquals("Fzglnn", q1Loop.getValue("NOM", "BOUCLEINDIV-1"));
		assertEquals("2", q1Loop.getValue("SEXE", "BOUCLEINDIV-1"));
		assertEquals("1931-11-09", q1Loop.getValue("DTNAIS", "BOUCLEINDIV-1"));
		assertEquals("3", q1Loop.getValue("ETAT_SANT", "BOUCLEINDIV-1"));
		//
		QuestionnaireData q3 = data.getQuestionnaires().get(2);
		assertEquals("VQS7195564", q3.getIdentifier());
		GroupData q3Loop = q3.getAnswers().getSubGroup("BOUCLEINDIV");
		assertEquals(2, q3Loop.getInstanceIds().size());
		assertEquals("Guss-Upazdjnhw", q3Loop.getValue("PRENOM", "BOUCLEINDIV-1"));
		assertEquals("Vpeltm", q3Loop.getValue("PRENOM", "BOUCLEINDIV-2"));
		assertEquals("1", q3Loop.getValue("ETAT_SANT", "BOUCLEINDIV-1"));
		assertEquals("2", q3Loop.getValue("ETAT_SANT", "BOUCLEINDIV-2"));
	}
	
}
