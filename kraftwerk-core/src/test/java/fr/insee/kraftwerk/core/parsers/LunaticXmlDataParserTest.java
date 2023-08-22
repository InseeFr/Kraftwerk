package fr.insee.kraftwerk.core.parsers;

import static org.junit.Assert.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import fr.insee.kraftwerk.core.TestConstants;
import fr.insee.kraftwerk.core.exceptions.NullException;
import fr.insee.kraftwerk.core.metadata.Variable;
import fr.insee.kraftwerk.core.metadata.VariableType;
import fr.insee.kraftwerk.core.metadata.VariablesMap;
import fr.insee.kraftwerk.core.metadata.VariablesMapTest;
import fr.insee.kraftwerk.core.rawdata.GroupData;
import fr.insee.kraftwerk.core.rawdata.QuestionnaireData;
import fr.insee.kraftwerk.core.rawdata.SurveyRawData;

class LunaticXmlDataParserTest {

	private final String dataSamplesFolder = TestConstants.UNIT_TESTS_DIRECTORY + "/data";

	@Test
	void readXmlFile_null() {
		SurveyRawData data = new SurveyRawData("TEST");
		LunaticXmlDataParser parser = new LunaticXmlDataParser(data);
		
		assertThrows(NullPointerException.class,() ->parser.parseDataFile(null));
		parser.parseDataFile(Path.of("notfound.xml"));
		assertEquals(new ArrayList<QuestionnaireData>(),data.getQuestionnaires());

	}
	
	@Test
	void parseLunaticDataFolder() throws NullException {
		//
		SurveyRawData data = new SurveyRawData("TEST");
		VariablesMap variablesMap = new VariablesMap();
		variablesMap.putVariable(new Variable("FOO", variablesMap.getRootGroup(), VariableType.STRING));
		data.setVariablesMap(variablesMap);
		Path dataPath = Paths.get(dataSamplesFolder + "/lunatic_xml/fake-multiple-files");
		LunaticXmlDataParser parser = new LunaticXmlDataParser(data);
		parser.parseSurveyData(dataPath);

		//
		assertEquals(4, data.getQuestionnairesCount());
		//
		for (QuestionnaireData questionnaireData : data.getQuestionnaires()) {
			String fooValue = questionnaireData.getValue("FOO");
			if (fooValue != null) {
				assertEquals("foo", fooValue);
			}
		}
	}

	@Test
	void parseLunaticXml_rootOnly() throws NullException {
		//
		SurveyRawData data = new SurveyRawData("TEST");
		data.setVariablesMap(VariablesMapTest.createVariablesMap_rootOnly());
		Path dataPath = Paths.get(dataSamplesFolder + "/lunatic_xml/fake-lunatic-data-root-only.xml");
		LunaticXmlDataParser parser = new LunaticXmlDataParser(data);
		parser.parseSurveyData(dataPath);

		//
		assertEquals(5, data.getQuestionnairesCount());
		//
		checkRootContent(data);
	}

	public void checkRootContent(SurveyRawData data) {
		//
		QuestionnaireData q1 = data.getQuestionnaires().get(0);
		assertEquals("T0000001", q1.getIdentifier());
		assertEquals("742 Evergreen Terrace", q1.getValue("ADDRESS"));
		assertEquals("20000", q1.getValue("HOUSEHOLD_INCOME"));
		//
		QuestionnaireData q2 = data.getQuestionnaires().get(1);
		assertEquals("T0000091", q2.getIdentifier());
		assertNull(q2.getValue("ADDRESS"));
		assertNull(q2.getValue("HOUSEHOLD_INCOME"));
		//
		QuestionnaireData q5 = data.getQuestionnaires().get(4);
		assertEquals("T0000004", q5.getIdentifier());
		assertEquals("1000 Mammon Street", q5.getValue("ADDRESS"));
		assertEquals("1000000000", q5.getValue("HOUSEHOLD_INCOME"));
	}

	@Test
	void parseLunaticXml_oneLevel() throws NullException {
		//
		SurveyRawData data = new SurveyRawData("TEST");
		data.setVariablesMap(VariablesMapTest.createVariablesMap_oneLevel());
		Path dataPath = Paths.get(dataSamplesFolder + "/lunatic_xml/fake-lunatic-data-1.xml");
		LunaticXmlDataParser parser = new LunaticXmlDataParser(data);
		parser.parseSurveyData(dataPath);

		//
		assertEquals(5, data.getQuestionnairesCount());
		//
		checkRootContent(data);
		checkLevelOneContent(data);
	}

	public void checkLevelOneContent(SurveyRawData data) {
		// Simpson's family
		GroupData individuals1 = data.getQuestionnaires().get(0).getAnswers().getSubGroup("INDIVIDUALS_LOOP");
		assertEquals("Homer", individuals1.getValue("FIRST_NAME", 0));
		assertEquals("Marge", individuals1.getValue("FIRST_NAME", 1));
		assertEquals("Santa's Little Helper", individuals1.getValue("FIRST_NAME", 6));
		// Empty questionnaire
		GroupData individuals2 = data.getQuestionnaires().get(1).getAnswers().getSubGroup("INDIVIDUALS_LOOP");
		for (String variableName : List.of("FIRST_NAME", "LAST_NAME", "GENDER")) {
			assertNull(individuals2.getValue(variableName, 0));
		}
		// Flanders' family
		GroupData individuals3 = data.getQuestionnaires().get(2).getAnswers().getSubGroup("INDIVIDUALS_LOOP");
		for (int i=0; i<3; i++) {
			assertEquals("Flanders", individuals3.getValue("LAST_NAME", i));
		}
	}

}