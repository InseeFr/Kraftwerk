package fr.insee.kraftwerk.parsers;

import org.junit.jupiter.api.Test;

import fr.insee.kraftwerk.Constants;
import fr.insee.kraftwerk.TestConstants;
import fr.insee.kraftwerk.metadata.DDIReader;
import fr.insee.kraftwerk.rawdata.GroupInstance;
import fr.insee.kraftwerk.rawdata.QuestionnaireData;
import fr.insee.kraftwerk.rawdata.SurveyRawData;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class LunaticJsonDataParserTest {

	@Test
	public void parseLunaticSurveyDataTest() {
		SurveyRawData lunaticData = new SurveyRawData();
		lunaticData.setVariablesMap(DDIReader.getVariablesFromDDI(
				"https://gitlab.insee.fr/enquetes-menages/integration-metallica/-/raw/master/Simpsons/V2/ddi-simpsons-v2.xml"));

		lunaticData.setDataFilePath(Constants.getResourceAbsolutePath(TestConstants.TEST_FUNCTIONAL_INPUT_DIRECTORY + "/SIMPSONS-v2/simpsons-v2-sample.json"));
		LunaticJsonDataParser parser = new LunaticJsonDataParser();
		parser.parseSurveyData(lunaticData);
		
		QuestionnaireData q = lunaticData.getQuestionnaires().get(0);
		GroupInstance rootGroup = q.getAnswers();
		assertEquals(1, lunaticData.getQuestionnairesCount()); // TODO
		assertEquals("1234", q.getIdentifier()); // TODO
		assertEquals("Joe Quimby", rootGroup.getValue("MAYOR")); // TODO
		assertNull(rootGroup.getValue("FAVOURITE_CHARACTERS31"));
	}
}
