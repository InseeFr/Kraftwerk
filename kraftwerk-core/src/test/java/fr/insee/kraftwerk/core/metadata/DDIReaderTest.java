package fr.insee.kraftwerk.core.metadata;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.MalformedURLException;
import java.util.Set;

import org.junit.jupiter.api.Test;

import fr.insee.kraftwerk.core.Constants;
import fr.insee.kraftwerk.core.TestConstants;
import fr.insee.kraftwerk.core.exceptions.KraftwerkException;

class DDIReaderTest {

	static final String DDI_FOLDER = TestConstants.UNIT_TESTS_DIRECTORY + "/ddi";

	static final String DDI_SIMPSONS_V1 = "ddi-simpsons-v1.xml";
	static final String DDI_SIMPSONS_V2 = "ddi-simpsons-v2.xml";
	static final String DDI_VQS_WEB = "vqs-2021-x00-xforms-ddi.xml";
	static final String DDI_VQS_PAP = "vqs-2021-x00-fo-ddi.xml";
	@Test
	void readSimpsonsV1Variables() throws MalformedURLException, KraftwerkException {

		Set<String> expectedVariables = Set.of(
				//
				"FAVOURITE_CHARACTERS11", "FAVOURITE_CHARACTERS102",
				//
				"SUM_EXPENSES", "LAST_BROADCAST", "COMMENT", "READY", "PRODUCER", "SEASON_NUMBER", "DATEFIRST",
				"AUDIENCE_SHARE", "CITY", "MAYOR", "STATE", "PET1", "PET4", "ICE_FLAVOUR1", "ICE_FLAVOUR4",
				"NUCLEAR_CHARACTER1", "NUCLEAR_CHARACTER4", "BIRTH_CHARACTER1", "BIRTH_CHARACTER5",
				"PERCENTAGE_EXPENSES11", "PERCENTAGE_EXPENSES101", "CLOWNING11", "CLOWNING42", "TRAVEL11", "TRAVEL46",
				"SURVEY_COMMENT");

		VariablesMap simpsonsVariables = DDIReader
				.getVariablesFromDDI(Constants.convertToUrl(DDI_FOLDER + "/" + DDI_SIMPSONS_V1));

		//
		assertNotNull(simpsonsVariables);
		//
		assertTrue(simpsonsVariables.hasGroup(Constants.ROOT_GROUP_NAME));
		assertTrue(simpsonsVariables.hasGroup("FAVOURITE_CHARACTERS"));
		//
		for (String variableName : expectedVariables) {
			assertTrue(simpsonsVariables.hasVariable(variableName));
		}
		//
		assertEquals(Constants.ROOT_GROUP_NAME, simpsonsVariables.getVariable("SUM_EXPENSES").getGroup().getName());
		assertEquals(Constants.ROOT_GROUP_NAME, simpsonsVariables.getVariable("SURVEY_COMMENT").getGroup().getName());
		assertEquals("FAVOURITE_CHARACTERS",
				simpsonsVariables.getVariable("FAVOURITE_CHARACTERS11").getGroup().getName());
		assertEquals("FAVOURITE_CHARACTERS",
				simpsonsVariables.getVariable("FAVOURITE_CHARACTERS102").getGroup().getName());
	}

	@Test
	void readSimpsonsV2Variables() throws MalformedURLException, KraftwerkException {

		Set<String> expectedVariables = Set.of(
				//
				"SUM_EXPENSES", "YEAR", "FAVOURITE_CHAR1", "FAVOURITE_CHAR2", "FAVOURITE_CHAR3", "FAVOURITE_CHAR33CL",
				//
				"NAME_CHAR", "AGE_CHAR", "FAVCHAR", "MEMORY_CHAR",
				//
				"LAST_BROADCAST", "COMMENT", "READY", "PRODUCER", "SEASON_NUMBER", "DATEFIRST", "DATEYYYYMM",
				"DATEYYYY", "DURATIONH", "DURATIONHH", "AUDIENCE_SHARE", "CITY", "MAYOR", "MAYOROTCL", "STATE", "PET1",
				"PET4", "PETOCL", "ICE_FLAVOUR1", "ICE_FLAVOUR4", "ICE_FLAVOUROTCL", "NUCLEAR_CHARACTER1",
				"NUCLEAR_CHARACTER4", "BIRTH_CHARACTER1", "BIRTH_CHARACTER5", "PERCENTAGE_EXPENSES11",
				"PERCENTAGE_EXPENSES71", "LAST_FOOD_SHOPPING11", "LAST_FOOD_SHOPPING81", "LAST_FOOD_SHOPPING113CL",
				"LAST_FOOD_SHOPPING813CL", "CLOWNING11", "CLOWNING42", "TRAVEL11", "TRAVEL46", "FEELCHAREV1",
				"FEELCHAREV4", "LEAVDURATION11", "LEAVDURATION52", "NB_CHAR", "SURVEY_COMMENT");

		VariablesMap simpsonsVariables = DDIReader
				.getVariablesFromDDI(Constants.convertToUrl(DDI_FOLDER + "/" + DDI_SIMPSONS_V2));

		//
		assertNotNull(simpsonsVariables);
		//
		assertTrue(simpsonsVariables.hasGroup(Constants.ROOT_GROUP_NAME));
		assertTrue(simpsonsVariables.hasGroup("FAVOURITE_CHAR"));
		assertTrue(simpsonsVariables.hasGroup("Loop1"));
		//
		for (String variableName : expectedVariables) {
			assertTrue(simpsonsVariables.hasVariable(variableName));
		}
		//
		assertEquals(Constants.ROOT_GROUP_NAME, simpsonsVariables.getVariable("LAST_BROADCAST").getGroup().getName());
		assertEquals(Constants.ROOT_GROUP_NAME, simpsonsVariables.getVariable("SURVEY_COMMENT").getGroup().getName());
		assertEquals("FAVOURITE_CHAR", simpsonsVariables.getVariable("SUM_EXPENSES").getGroup().getName());
		assertEquals("FAVOURITE_CHAR", simpsonsVariables.getVariable("FAVOURITE_CHAR1").getGroup().getName());
		assertEquals("Loop1", simpsonsVariables.getVariable("NAME_CHAR").getGroup().getName());
	}

	@Test
	void readVqsWebVariables() throws MalformedURLException, KraftwerkException {

		Set<String> expectedVariables = Set.of("prenom", "NOM", "SEXE", "DTNAIS", "ETAT_SANT", "APPRENT", "AIDREG_A",
				"AIDREG_B", "AIDREG_C", "AIDREG_D", "RELATION1", "RELATION2", "RELATION3", "RELATION4", "ADRESSE",
				"RESIDM", "NHAB");

		VariablesMap vqsVariables = DDIReader
				.getVariablesFromDDI(Constants.convertToUrl(DDI_FOLDER + "/" + DDI_VQS_WEB));

		//
		assertNotNull(vqsVariables);
		//
		assertTrue(vqsVariables.hasGroup(Constants.ROOT_GROUP_NAME));
		assertTrue(vqsVariables.hasGroup("BOUCLEINDIV"));
		//
		for (String variableName : expectedVariables) {
			assertTrue(vqsVariables.hasVariable(variableName));
		}
		//
		assertEquals(Constants.ROOT_GROUP_NAME, vqsVariables.getVariable("ADRESSE").getGroup().getName());
		assertEquals("BOUCLEINDIV", vqsVariables.getVariable("prenom").getGroup().getName());
		// UCQ
		assertTrue(vqsVariables.getVariable("ETAT_SANT") instanceof UcqVariable);
		UcqVariable etatSant = (UcqVariable) vqsVariables.getVariable("ETAT_SANT");
		assertEquals(5, etatSant.getModalities().size());
		// MCQ
		assertTrue(vqsVariables.getVariable("AIDREG_A") instanceof McqVariable);
		assertTrue(vqsVariables.getVariable("AIDREG_D") instanceof McqVariable);
		assertTrue(vqsVariables.getVariable("RELATION1") instanceof McqVariable);
		assertTrue(vqsVariables.getVariable("RELATION4") instanceof McqVariable);
		McqVariable aidregA = (McqVariable) vqsVariables.getVariable("AIDREG_A");
		assertEquals("AIDREG", aidregA.getQuestionItemName());
		assertEquals("1 - Oui, une aide aux activités de la vie quotidienne", aidregA.getText());
		//
		assertFalse(vqsVariables.getVariable("ADRESSE") instanceof McqVariable);
		assertFalse(vqsVariables.getVariable("ADRESSE") instanceof UcqVariable);
		assertFalse(vqsVariables.getVariable("PRENOM") instanceof McqVariable);
		assertFalse(vqsVariables.getVariable("PRENOM") instanceof UcqVariable);
	}

	@Test
	void readVqsPapVariables() throws MalformedURLException, KraftwerkException {

		Set<String> expectedVariables = Set.of("PRENOM", "NOM", "SEXE", "DTNAIS", "ETAT_SANT", "APPRENT", "AIDREG_A",
				"AIDREG_B", "AIDREG_C", "AIDREG_D", "RESID", "RESIDANCIEN", "NBQUEST");

		VariablesMap vqsVariables = DDIReader
				.getVariablesFromDDI(Constants.convertToUrl(DDI_FOLDER + "/" + DDI_VQS_PAP));

		//
		assertNotNull(vqsVariables);
		//
		assertTrue(vqsVariables.hasGroup(Constants.ROOT_GROUP_NAME));
		assertTrue(vqsVariables.hasGroup("BOUCLEINDIV"));
		//
		for (String variableName : expectedVariables) {
			assertTrue(vqsVariables.hasVariable(variableName));
		}
		//
		assertEquals(Constants.ROOT_GROUP_NAME, vqsVariables.getVariable("NBQUEST").getGroup().getName());
		assertEquals("BOUCLEINDIV", vqsVariables.getVariable("PRENOM").getGroup().getName());
	}

}
