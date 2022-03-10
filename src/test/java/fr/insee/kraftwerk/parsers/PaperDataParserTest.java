package fr.insee.kraftwerk.parsers;

import fr.insee.kraftwerk.TestConstants;
import fr.insee.kraftwerk.metadata.DDIReader;
import fr.insee.kraftwerk.metadata.PaperUcq;
import fr.insee.kraftwerk.metadata.UcqVariable;
import fr.insee.kraftwerk.metadata.VariablesMap;
import fr.insee.kraftwerk.rawdata.GroupData;
import fr.insee.kraftwerk.rawdata.QuestionnaireData;
import fr.insee.kraftwerk.rawdata.SurveyRawData;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class PaperDataParserTest {

	@Test
	public void parseVqsPaperData() {
		//
		SurveyRawData data = new SurveyRawData();
		data.setVariablesMap(DDIReader.getVariablesFromDDI(TestConstants.VQS_PAP_DDI));
		data.setDataFilePath(TestConstants.VQS_PAP_DATA);
		PaperDataParser parser = new PaperDataParser();
		parser.parseSurveyData(data);

		// Tests on variables
		VariablesMap variablesMap = data.getVariablesMap();
		//
		assertTrue(variablesMap.getVariable("ETAT_SANT_1") instanceof PaperUcq);
		assertTrue(variablesMap.getVariable("ETAT_SANT_2") instanceof PaperUcq);
		assertTrue(variablesMap.getVariable("LIMITAT_1") instanceof PaperUcq);
		assertTrue(variablesMap.getVariable("LIMITAT_2") instanceof PaperUcq);
		//
		assertEquals("ETAT_SANT", ((PaperUcq) variablesMap.getVariable("ETAT_SANT_1")).getUcqName());
		assertEquals("ETAT_SANT", ((PaperUcq) variablesMap.getVariable("ETAT_SANT_2")).getUcqName());
		assertEquals("LIMITAT", ((PaperUcq) variablesMap.getVariable("LIMITAT_1")).getUcqName());
		assertEquals("LIMITAT", ((PaperUcq) variablesMap.getVariable("LIMITAT_2")).getUcqName());
		//
		UcqVariable ucq1 = (UcqVariable) variablesMap.getVariable("ETAT_SANT");
		UcqVariable ucq2 = (UcqVariable) variablesMap.getVariable("LIMITAT");
		assertEquals("ETAT_SANT_1", ucq1.getModalityFromName("ETAT_SANT_1").getVariableName());
		assertEquals("ETAT_SANT_2", ucq1.getModalityFromName("ETAT_SANT_2").getVariableName());
		assertEquals("LIMITAT_1", ucq2.getModalityFromName("LIMITAT_1").getVariableName());
		assertEquals("LIMITAT_2", ucq2.getModalityFromName("LIMITAT_2").getVariableName());

		// Tests on data content
		assertEquals(32, data.getQuestionnairesCount());
		//
		QuestionnaireData q1 = data.getQuestionnaires().get(0);
		assertEquals("VQS0588832", q1.getIdentifier());
		GroupData q1Loop = q1.getAnswers().getSubGroup("BOUCLEINDIV");
		assertEquals("GDCGIWYJJ", q1Loop.getValue("PRENOM", "BOUCLEINDIV-20210302125101"));
		assertEquals("0", q1Loop.getValue("ETAT_SANT_1", "BOUCLEINDIV-20210302125101"));
		assertEquals("1", q1Loop.getValue("ETAT_SANT_2", "BOUCLEINDIV-20210302125101"));
		//
		QuestionnaireData q4 = data.getQuestionnaires().get(3);
		QuestionnaireData q5 = data.getQuestionnaires().get(4);
		assertEquals("VQS9616867", q4.getIdentifier());
		assertEquals("VQS9616867", q5.getIdentifier());
		GroupData q4Loop = q4.getAnswers().getSubGroup("BOUCLEINDIV");
		GroupData q5Loop = q5.getAnswers().getSubGroup("BOUCLEINDIV");
		assertEquals("DPHREJYXA", q4Loop.getValue("NOM", "BOUCLEINDIV-20210302125033"));
		assertEquals("DPHREJYXA", q5Loop.getValue("NOM", "BOUCLEINDIV-20210302125035"));
	}

}
