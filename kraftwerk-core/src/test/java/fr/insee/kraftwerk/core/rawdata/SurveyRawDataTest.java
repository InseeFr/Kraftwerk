package fr.insee.kraftwerk.core.rawdata;

import fr.insee.bpm.metadata.model.MetadataModel;
import fr.insee.kraftwerk.core.outputs.scripts.ImportScriptTest;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SurveyRawDataTest {

	/* Unit testing */

	@Test
	void constructorsAndDataModeTest() {
		SurveyRawData d1 = new SurveyRawData("CAWI");
		SurveyRawData d2 = new SurveyRawData();
		d2.setDataMode("PAPI");
		//
		assertEquals("CAWI", d1.getDataMode());
		assertEquals("PAPI", d2.getDataMode());
	}
	
	@Test
	void addQuestionnaireAndQuestionnairesCountTest() {
		SurveyRawData data = new SurveyRawData();
		data.addQuestionnaire(new QuestionnaireData());
		data.addQuestionnaire(new QuestionnaireData());
		//
		assertEquals(2, data.getQuestionnairesCount());
	}

	@Test
	void surveyRawDataTest(){
		SurveyRawData testData = createFakeCawiSurveyRawData();
		//
		QuestionnaireData q1 = testData.getQuestionnaires().get(0);
		QuestionnaireData q2 = testData.getQuestionnaires().get(1);
		assertEquals("L0000003", q1.getIdentifier());
		assertEquals("Flanders", q2.getAnswers().getValue("LAST_NAME"));
		assertEquals("Red", q1.getAnswers().getSubGroup("CARS_LOOP").getInstance("CARS_LOOP-2")
				.getValue("CAR_COLOR"));
	}



	/* Methods used for functional testing */

	/**
	 * Create a SurveyRawData object with some fake data in it.
	 * Use the variables from the method createFakeVariablesMap.
	 * @see fr.insee.kraftwerk.core.outputs.scripts.ImportScriptTest
	 * */
	public static SurveyRawData createFakeCawiSurveyRawData() {
		// Instantiate the data object
		SurveyRawData data = new SurveyRawData("CAWI");

		// Give it a data structure
		MetadataModel metMod = ImportScriptTest.createCompleteFakeVariablesMap();
		data.setMetadataModel(metMod);

		// Homer Simpsons, aged 40, has a purple and a red car.
		QuestionnaireData q1 = new QuestionnaireData();
		q1.setIdentifier("L0000003");
		GroupInstance q1Root = q1.getAnswers();
		q1Root.putValue("FIRST_NAME", "Homer");
		q1Root.putValue("LAST_NAME","Simpson");
		q1Root.putValue("AGE", "40");
		GroupData q1carsGroup = q1Root.getSubGroup("CARS_LOOP");
		q1carsGroup.getInstance("CARS_LOOP-1").putValue("CAR_COLOR", "Purple");
		q1carsGroup.getInstance("CARS_LOOP-2").putValue("CAR_COLOR", "Red");
		data.addQuestionnaire(q1);

		// Ned Flanders, aged 58, has a red and a blue car
		QuestionnaireData q2 = new QuestionnaireData();
		q2.setIdentifier("L0000004");
		GroupInstance q2Root = q2.getAnswers();
		q2Root.putValue("FIRST_NAME", "Ned");
		q2Root.putValue("LAST_NAME", "Flanders");
		q2Root.putValue("AGE", "58");
		q2Root.getSubGroup("CARS_LOOP");
		q2Root.getSubGroup("CARS_LOOP").getInstance("CARS_LOOP-1").putValue("CAR_COLOR", "Red");
		q2Root.getSubGroup("CARS_LOOP").getInstance("CARS_LOOP-2").putValue("CAR_COLOR", "Blue");
		data.addQuestionnaire(q2);

		return data;
	}

	public static SurveyRawData createFakeCapiSurveyRawData() {
		// Instantiate the data object
		SurveyRawData data = new SurveyRawData("CAPI");

		// Give it a data structure
		MetadataModel metadataModel = ImportScriptTest.createCompleteFakeVariablesMap();
		data.setMetadataModel(metadataModel);

		// Homer Simpsons, aged 40, has a purple and a red car.
		QuestionnaireData q1 = new QuestionnaireData();
		q1.setIdentifier("S00000003");
		GroupInstance q1Root = q1.getAnswers();
		q1Root.putValue("FIRST_NAME", "Homer");
		q1Root.putValue("LAST_NAME","Simpson");
		q1Root.putValue("AGE", "40");
		GroupData q1carsGroup = q1Root.getSubGroup("CARS_LOOP");
		q1carsGroup.getInstance("CARS_LOOP-1").putValue("CAR_COLOR", "Purple");
		q1carsGroup.getInstance("CARS_LOOP-2").putValue("CAR_COLOR", "Red");
		data.addQuestionnaire(q1);

		// Ned Flanders, aged 58, has a red and a blue car
		QuestionnaireData q2 = new QuestionnaireData();
		q2.setIdentifier("S00000004");
		GroupInstance q2Root = q2.getAnswers();
		q2Root.putValue("FIRST_NAME", "Ned");
		q2Root.putValue("LAST_NAME", "Flanders");
		q2Root.putValue("AGE", "58");
		q2Root.getSubGroup("CARS_LOOP");
		q2Root.getSubGroup("CARS_LOOP").getInstance("CARS_LOOP-1").putValue("CAR_COLOR", "Red");
		q2Root.getSubGroup("CARS_LOOP").getInstance("CARS_LOOP-2").putValue("CAR_COLOR", "Blue");
		data.addQuestionnaire(q2);

		return data;
	}
	
	public static SurveyRawData createFakePapiSurveyRawData() {
		// Instantiate the data object
		SurveyRawData data = new SurveyRawData("PAPI");

		// Give it a data structure
		MetadataModel metMod = ImportScriptTest.createAnotherFakeVariablesMap();
		data.setMetadataModel(metMod);

		//
		QuestionnaireData q1 = new QuestionnaireData();
		q1.setIdentifier("L0000005");
		GroupInstance q1Root = q1.getAnswers();
		q1Root.putValue("FIRST_NAME", "Homer");
		q1Root.putValue("LAST_NAME", "Simpson in PAPI");
		q1Root.putValue("ADDRESS", "742 Evergreen Terrace");
		GroupData q1carsGroup = q1Root.getSubGroup("CARS_LOOP");
		q1carsGroup.getInstance("CARS_LOOP-1").putValue("CAR_COLOR", "Purple");
		q1carsGroup.getInstance("CARS_LOOP-2").putValue("CAR_COLOR", "Red");
		data.addQuestionnaire(q1);

		//
		QuestionnaireData q2 = new QuestionnaireData();
		q2.setIdentifier("L0000006");
		GroupInstance q2Root = q2.getAnswers();
		q2Root.putValue("FIRST_NAME", "Ned");
		q2Root.putValue("LAST_NAME", "Flanders");
		q2Root.putValue("ADDRESS", "740 Evergreen Terrace");
		GroupData q2carsGroup = q2Root.getSubGroup("CARS_LOOP");
		q2carsGroup.getInstance("CARS_LOOP-1").putValue("CAR_COLOR", "Red");
		q2carsGroup.getInstance("CARS_LOOP-2").putValue("CAR_COLOR", "Blue");
		data.addQuestionnaire(q2);

		//
		QuestionnaireData q3 = new QuestionnaireData();
		q3.setIdentifier("L0000069");
		GroupInstance q3Root = q3.getAnswers();
		q3Root.putValue("FIRST_NAME", "Charles Montgomery");
		q3Root.putValue("LAST_NAME", "Burns");
		q3Root.putValue("ADDRESS", "1000 Mammon Avenue");
		GroupData q3carsGroup = q3Root.getSubGroup("CARS_LOOP");
		q3carsGroup.getInstance("CARS_LOOP-1").putValue("CAR_COLOR", "Grey");
		q3carsGroup.getInstance("CARS_LOOP-2").putValue("CAR_COLOR", "Gold Yellow");
		q3carsGroup.getInstance("CARS_LOOP-3").putValue("CAR_COLOR", "White");
		data.addQuestionnaire(q3);

		//
		QuestionnaireData q4 = new QuestionnaireData();
		q4.setIdentifier("L0000169");
		GroupInstance q4Root = q4.getAnswers();
		q4Root.putValue("FIRST_NAME", "Apu");
		q4Root.putValue("LAST_NAME", "Nahasapeemapetilon");
		q4Root.putValue("ADDRESS", "Kwik-E-Mart");
		q4Root.getSubGroup("CARS_LOOP").getInstance("CARS_LOOP-1").putValue("CAR_COLOR", "Blue");
		data.addQuestionnaire(q4);

		return data;
	}

	public static SurveyRawData createFakeData_rootOnly() {

		SurveyRawData data = new SurveyRawData();

		data.setMetadataModel(ImportScriptTest.createVariablesMap_rootOnly());

		QuestionnaireData q1 = new QuestionnaireData();
		q1.setIdentifier("S0000001");
		q1.putValue("20000", "HOUSEHOLD_INCOME");
		q1.putValue("740 Evergreen Terrace", "ADDRESS");
		data.addQuestionnaire(q1);

		QuestionnaireData q2 = new QuestionnaireData();
		q2.setIdentifier("S0000002");
		q2.putValue("30000", "HOUSEHOLD_INCOME");
		q2.putValue("742 Evergreen Terrace", "ADDRESS");
		data.addQuestionnaire(q2);

		return data;
	}

	public static SurveyRawData createFakeData_oneLevel() {

		SurveyRawData data = createFakeData_rootOnly();

		data.setMetadataModel(ImportScriptTest.createVariablesMap_oneLevel());

		QuestionnaireData q1 = data.getQuestionnaires().getFirst();
		q1.putValue("Homer", "FIRST_NAME", Pair.of("INDIVIDUALS_LOOP", 0));
		q1.putValue("Simpson", "LAST_NAME", Pair.of("INDIVIDUALS_LOOP", 0));
		q1.putValue("M", "GENDER", Pair.of("INDIVIDUALS_LOOP", 0));
		q1.putValue("Marge", "FIRST_NAME", Pair.of("INDIVIDUALS_LOOP", 1));
		q1.putValue("Simpson", "LAST_NAME", Pair.of("INDIVIDUALS_LOOP", 1));
		q1.putValue("F", "GENDER", Pair.of("INDIVIDUALS_LOOP", 1));
		q1.putValue("Santa's Little Helper", "FIRST_NAME", Pair.of("INDIVIDUALS_LOOP", 2));

		QuestionnaireData q2 = data.getQuestionnaires().get(1);
		q2.putValue("Ned", "FIRST_NAME", Pair.of("INDIVIDUALS_LOOP", 0));
		q2.putValue("Flanders", "LAST_NAME", Pair.of("INDIVIDUALS_LOOP", 0));
		q2.putValue("M", "GENDER", Pair.of("INDIVIDUALS_LOOP", 0));

		return data;
	}

	public static SurveyRawData createFakeData_twoLevels() {

		SurveyRawData data = createFakeData_oneLevel();

		data.setMetadataModel(ImportScriptTest.createVariablesMap_twoLevels());

		QuestionnaireData q1 = data.getQuestionnaires().getFirst();
		q1.putValue("Purple", "CAR_COLOR", Pair.of("INDIVIDUALS_LOOP", 0), Pair.of("CARS_LOOP", 0));
		q1.putValue("Red", "CAR_COLOR", Pair.of("INDIVIDUALS_LOOP", 1), Pair.of("CARS_LOOP", 0));

		QuestionnaireData q2 = data.getQuestionnaires().get(1);
		q2.putValue("Red", "CAR_COLOR", Pair.of("INDIVIDUALS_LOOP", 0), Pair.of("CARS_LOOP", 0));
		q2.putValue("Blue", "CAR_COLOR", Pair.of("INDIVIDUALS_LOOP", 0), Pair.of("CARS_LOOP", 1));

		return data;
	}
}
