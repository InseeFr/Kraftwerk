package fr.insee.kraftwerk.parsers;

import fr.insee.kraftwerk.TestConstants;
import fr.insee.kraftwerk.metadata.DDIReader;
import fr.insee.kraftwerk.metadata.VariablesMapTest;
import fr.insee.kraftwerk.rawdata.GroupData;
import fr.insee.kraftwerk.rawdata.QuestionnaireData;
import fr.insee.kraftwerk.rawdata.SurveyRawData;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class LunaticXmlDataParserTest {

	private final String dataSamplesFolder = TestConstants.TEST_UNIT_INPUT_DIRECTORY + "/data";

	@Test
	public void parseLunaticXml_rootOnly() {
		//
		SurveyRawData data = new SurveyRawData("TEST");
		data.setVariablesMap(VariablesMapTest.createVariablesMap_rootOnly());
		data.setDataFilePath(dataSamplesFolder + "/lunatic_xml/fake-lunatic-data-root-only.xml");
		LunaticXmlDataParser parser = new LunaticXmlDataParser();
		parser.parseSurveyData(data);

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
		assertEquals("", q2.getValue("ADDRESS"));
		assertEquals("", q2.getValue("HOUSEHOLD_INCOME"));
		//
		QuestionnaireData q5 = data.getQuestionnaires().get(4);
		assertEquals("T0000004", q5.getIdentifier());
		assertEquals("1000 Mammon Street", q5.getValue("ADDRESS"));
		assertEquals("1000000000", q5.getValue("HOUSEHOLD_INCOME"));
	}

	@Test
	public void parseLunaticXml_oneLevel() {
		//
		SurveyRawData data = new SurveyRawData("TEST");
		data.setVariablesMap(VariablesMapTest.createVariablesMap_oneLevel());
		data.setDataFilePath(dataSamplesFolder + "/lunatic_xml/fake-lunatic-data-1.xml");
		LunaticXmlDataParser parser = new LunaticXmlDataParser();
		parser.parseSurveyData(data);

		//
		assertEquals(5, data.getQuestionnairesCount());
		//
		checkRootContent(data);
		checkLevelOneContent(data);
	}

	public void checkLevelOneContent(SurveyRawData data) {
		//
		GroupData individuals1 = data.getQuestionnaires().get(0).getAnswers().getSubGroup("INDIVIDUALS_LOOP");
		assertEquals("Homer", individuals1.getValue("FIRST_NAME", 0));
		assertEquals("Marge", individuals1.getValue("FIRST_NAME", 1));
		assertEquals("Santa's Little Helper", individuals1.getValue("FIRST_NAME", 6));
		//
		GroupData individuals2 = data.getQuestionnaires().get(1).getAnswers().getSubGroup("INDIVIDUALS_LOOP");
		for (String variableName : List.of("FIRST_NAME", "LAST_NAME", "GENDER")) {
			assertEquals("", individuals2.getValue(variableName, 0));
		}
		//
		GroupData individuals3 = data.getQuestionnaires().get(2).getAnswers().getSubGroup("INDIVIDUALS_LOOP");
		for (int i=0; i<3; i++) {
			assertEquals("Flanders", individuals3.getValue("LAST_NAME", i));
		}
	}

}