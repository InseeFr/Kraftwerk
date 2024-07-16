package fr.insee.kraftwerk.core.metadata;

import fr.insee.kraftwerk.core.Constants;
import fr.insee.kraftwerk.core.TestConstants;
import fr.insee.kraftwerk.core.exceptions.KraftwerkException;
import fr.insee.kraftwerk.core.utils.files.FileSystemImpl;
import fr.insee.kraftwerk.core.utils.files.FileUtilsInterface;
import org.junit.jupiter.api.Test;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class VariableTest {

	static final String DDI_FOLDER = TestConstants.UNIT_TESTS_DIRECTORY + "/ddi";

	static final String DDI_SIMPSONS_V1 = "ddi-simpsons-v1.xml";
	static final String DDI_SIMPSONS_V2 = "ddi-simpsons-v2.xml";
	static final String DDI_VQS_WEB = "vqs-2021-x00-xforms-ddi.xml";
	static final String DDI_VQS_PAP = "vqs-2021-x00-fo-ddi.xml";
	static final FileUtilsInterface fileUtilsInterface = new FileSystemImpl();

	@Test
	void readSimpsonsV1Variables() throws MalformedURLException, KraftwerkException, URISyntaxException {

		Set<String> expectedVariables = Set.of(
				//
				"FAVOURITE_CHARACTERS11", "FAVOURITE_CHARACTERS102",
				//
				"SUM_EXPENSES", "LAST_BROADCAST", "COMMENT", "READY", "PRODUCER", "SEASON_NUMBER", "DATEFIRST",
				"AUDIENCE_SHARE", "CITY", "MAYOR", "STATE", "PET1", "PET4", "ICE_FLAVOUR1", "ICE_FLAVOUR4",
				"NUCLEAR_CHARACTER1", "NUCLEAR_CHARACTER4", "BIRTH_CHARACTER1", "BIRTH_CHARACTER5",
				"PERCENTAGE_EXPENSES11", "PERCENTAGE_EXPENSES101", "CLOWNING11", "CLOWNING42", "TRAVEL11", "TRAVEL46",
				"SURVEY_COMMENT");

		MetadataModel simpsonsMetadata = DDIReader
				.getMetadataFromDDI(DDI_FOLDER + "/" + DDI_SIMPSONS_V1, fileUtilsInterface);

		//
		assertNotNull(simpsonsMetadata);
		//
		assertTrue(simpsonsMetadata.hasGroup(Constants.ROOT_GROUP_NAME));
		assertTrue(simpsonsMetadata.hasGroup("FAVOURITE_CHARACTERS"));
		//
		for (String variableName : expectedVariables) {
			assertTrue(simpsonsMetadata.getVariables().hasVariable(variableName));
		}
		//
		assertEquals(Constants.ROOT_GROUP_NAME, simpsonsMetadata.getVariables().getVariable("SUM_EXPENSES").getGroup().getName());
		assertEquals(Constants.ROOT_GROUP_NAME, simpsonsMetadata.getVariables().getVariable("SURVEY_COMMENT").getGroup().getName());
		assertEquals("FAVOURITE_CHARACTERS",
				simpsonsMetadata.getVariables().getVariable("FAVOURITE_CHARACTERS11").getGroup().getName());
		assertEquals("FAVOURITE_CHARACTERS",
				simpsonsMetadata.getVariables().getVariable("FAVOURITE_CHARACTERS102").getGroup().getName());
	}

	@Test
	void readSimpsonsV2Variables() throws MalformedURLException, KraftwerkException, URISyntaxException {

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

		MetadataModel simpsonsMetadata = DDIReader
				.getMetadataFromDDI(DDI_FOLDER + "/" + DDI_SIMPSONS_V2.toString(), fileUtilsInterface);

		//
		assertNotNull(simpsonsMetadata);
		//
		assertTrue(simpsonsMetadata.hasGroup(Constants.ROOT_GROUP_NAME));
		assertTrue(simpsonsMetadata.hasGroup("FAVOURITE_CHAR"));
		assertTrue(simpsonsMetadata.hasGroup("Loop1"));
		//
		for (String variableName : expectedVariables) {
			assertTrue(simpsonsMetadata.getVariables().hasVariable(variableName));
		}
		//
		assertEquals(Constants.ROOT_GROUP_NAME, simpsonsMetadata.getVariables().getVariable("LAST_BROADCAST").getGroup().getName());
		assertEquals(Constants.ROOT_GROUP_NAME, simpsonsMetadata.getVariables().getVariable("SURVEY_COMMENT").getGroup().getName());
		assertEquals("FAVOURITE_CHAR", simpsonsMetadata.getVariables().getVariable("SUM_EXPENSES").getGroup().getName());
		assertEquals("FAVOURITE_CHAR", simpsonsMetadata.getVariables().getVariable("FAVOURITE_CHAR1").getGroup().getName());
		assertEquals("Loop1", simpsonsMetadata.getVariables().getVariable("NAME_CHAR").getGroup().getName());
	}

	@Test
	void readVqsWebVariables() throws MalformedURLException, KraftwerkException, URISyntaxException {

		Set<String> expectedVariables = Set.of("prenom", "NOM", "SEXE", "DTNAIS", "ETAT_SANT", "APPRENT", "AIDREG_A",
				"AIDREG_B", "AIDREG_C", "AIDREG_D", "RELATION1", "RELATION2", "RELATION3", "RELATION4", "ADRESSE",
				"RESIDM", "NHAB");

		MetadataModel vqsMetadata = DDIReader
				.getMetadataFromDDI(DDI_FOLDER + "/" + DDI_VQS_WEB, fileUtilsInterface);

		//
		assertNotNull(vqsMetadata);
		//
		assertTrue(vqsMetadata.hasGroup(Constants.ROOT_GROUP_NAME));
		assertTrue(vqsMetadata.hasGroup("BOUCLEINDIV"));
		//
		for (String variableName : expectedVariables) {
			assertTrue(vqsMetadata.getVariables().hasVariable(variableName));
		}
		//
		assertEquals(Constants.ROOT_GROUP_NAME, vqsMetadata.getVariables().getVariable("ADRESSE").getGroup().getName());
		assertEquals("BOUCLEINDIV", vqsMetadata.getVariables().getVariable("prenom").getGroup().getName());
		// UCQ
		assertInstanceOf(UcqVariable.class, vqsMetadata.getVariables().getVariable("ETAT_SANT"));
		UcqVariable etatSant = (UcqVariable) vqsMetadata.getVariables().getVariable("ETAT_SANT");
		assertEquals(5, etatSant.getModalities().size());
		// MCQ
		assertInstanceOf(McqVariable.class, vqsMetadata.getVariables().getVariable("AIDREG_A"));
		assertInstanceOf(McqVariable.class, vqsMetadata.getVariables().getVariable("AIDREG_D"));
		assertInstanceOf(McqVariable.class, vqsMetadata.getVariables().getVariable("RELATION1"));
		assertInstanceOf(McqVariable.class, vqsMetadata.getVariables().getVariable("RELATION4"));
		McqVariable aidregA = (McqVariable) vqsMetadata.getVariables().getVariable("AIDREG_A");
		assertEquals("AIDREG", aidregA.getQuestionItemName());
		assertEquals("1 - Oui, une aide aux activités de la vie quotidienne", aidregA.getText());
		//
		assertFalse(vqsMetadata.getVariables().getVariable("ADRESSE") instanceof McqVariable);
		assertFalse(vqsMetadata.getVariables().getVariable("ADRESSE") instanceof UcqVariable);
		assertFalse(vqsMetadata.getVariables().getVariable("PRENOM") instanceof McqVariable);
		assertFalse(vqsMetadata.getVariables().getVariable("PRENOM") instanceof UcqVariable);
	}

	@Test
	void readVqsPapVariables() throws MalformedURLException, KraftwerkException, URISyntaxException {

		Set<String> expectedVariables = Set.of("PRENOM", "NOM", "SEXE", "DTNAIS", "ETAT_SANT", "APPRENT", "AIDREG_A",
				"AIDREG_B", "AIDREG_C", "AIDREG_D", "RESID", "RESIDANCIEN", "NBQUEST");

		MetadataModel vqsMetadata = DDIReader
				.getMetadataFromDDI(DDI_FOLDER + "/" + DDI_VQS_PAP, fileUtilsInterface);

		//
		assertNotNull(vqsMetadata);
		//
		assertTrue(vqsMetadata.hasGroup(Constants.ROOT_GROUP_NAME));
		assertTrue(vqsMetadata.hasGroup("BOUCLEINDIV"));
		//
		for (String variableName : expectedVariables) {
			assertTrue(vqsMetadata.getVariables().hasVariable(variableName));
		}
		//
		assertEquals(Constants.ROOT_GROUP_NAME, vqsMetadata.getVariables().getVariable("NBQUEST").getGroup().getName());
		assertEquals("BOUCLEINDIV", vqsMetadata.getVariables().getVariable("PRENOM").getGroup().getName());
	}

}
