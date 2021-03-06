package fr.insee.kraftwerk.core.vtl;

import fr.insee.kraftwerk.core.Constants;
import fr.insee.kraftwerk.core.metadata.*;
import fr.insee.kraftwerk.core.rawdata.QuestionnaireData;
import fr.insee.kraftwerk.core.rawdata.SurveyRawData;
import fr.insee.kraftwerk.core.rawdata.SurveyRawDataTest;
import fr.insee.vtl.model.Dataset;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class VtlJsonDatasetWriterTest {

	private VtlBindings vtlBindings;

	@BeforeEach
	public void initVtlBindings() {
		vtlBindings = new VtlBindings();
	}

	@Test
	public void testConvertToVtlDataset_rootOnly() {
		//
		VariablesMap variablesMap = VariablesMapTest.createVariablesMap_rootOnly();
		//
		SurveyRawData testData = SurveyRawDataTest.createFakeData_rootOnly();
		//
		vtlBindings.convertToVtlDataset(testData, "TEST");
		Dataset ds = vtlBindings.getDataset("TEST");

		//
		assertNotNull(ds);
		//
		assertEquals(3, ds.getDataStructure().keySet().size());
		assertEquals(Dataset.Role.IDENTIFIER, ds.getDataStructure().get(Constants.ROOT_IDENTIFIER_NAME).getRole());
		for(String variableName : variablesMap.getVariableNames()) {
			String fullyQualifiedName = variablesMap.getFullyQualifiedName(variableName);
			assertTrue(ds.getDataStructure().containsKey(fullyQualifiedName));
			assertEquals(Dataset.Role.MEASURE, ds.getDataStructure().get(fullyQualifiedName).getRole());
		}
		//
		assertEquals(2, ds.getDataPoints().size());
		Set<String> expectedIdentifiers = Set.of("S0000001", "S0000002");
		Set<String> dsIdentifiers = new HashSet<>();
		ds.getDataPoints().forEach(
				dataPoint -> dsIdentifiers.add( (String) dataPoint.get(Constants.ROOT_IDENTIFIER_NAME) )
		);
		assertEquals(expectedIdentifiers, dsIdentifiers);
		Set<Double> expectedIncomes = Set.of(20000d, 30000d);
		Set<Double> dsIncomes = new HashSet<>();
		ds.getDataPoints().forEach(
				dataPoint -> dsIncomes.add(
						(Double) dataPoint.get(variablesMap.getFullyQualifiedName("HOUSEHOLD_INCOME")) )
		);
		assertEquals(expectedIncomes, dsIncomes);
	}

	@Test
	public void testConvertToVtlDataset_oneLevel() {
		//
		VariablesMap variablesMap = VariablesMapTest.createVariablesMap_oneLevel();
		//
		SurveyRawData testData = SurveyRawDataTest.createFakeData_oneLevel();
		//
		vtlBindings.convertToVtlDataset(testData, "TEST");
		Dataset ds = vtlBindings.getDataset("TEST");

		//
		assertNotNull(ds);
		//
		assertEquals(7, ds.getDataStructure().keySet().size());
		assertEquals(Dataset.Role.IDENTIFIER, ds.getDataStructure().get(Constants.ROOT_IDENTIFIER_NAME).getRole());
		assertEquals(Dataset.Role.IDENTIFIER, ds.getDataStructure().get("INDIVIDUALS_LOOP").getRole());
		for(String variableName : variablesMap.getVariableNames()) {
			assertTrue(ds.getDataStructure().containsKey(variableName));
			assertEquals(Dataset.Role.MEASURE, ds.getDataStructure().get(variableName).getRole());
		}
		//
		assertEquals(4, ds.getDataPoints().size());
		Set<String> expectedIdentifiers = Set.of("S0000001", "S0000002");
		Set<String> dsIdentifiers = new HashSet<>();
		ds.getDataPoints().forEach(
				dataPoint -> dsIdentifiers.add( (String) dataPoint.get(Constants.ROOT_IDENTIFIER_NAME) )
		);
		assertEquals(expectedIdentifiers, dsIdentifiers);
		Set<String> expectedFirstNames = Set.of("Homer", "Marge", "Santa's Little Helper", "Ned");
		Set<String> dsFirstNames = new HashSet<>();
		ds.getDataPoints().forEach(
				dataPoint -> dsFirstNames.add(
						(String) dataPoint.get("FIRST_NAME") )
		);
		assertEquals(expectedFirstNames, dsFirstNames);
	}

	/** In paper data, a single survey unit may be split into several questionnaires.
	 * This method test if VTL conversion works well in that case. */
	@Test
	public void convertToVtlDataset_withSplitQuestionnaires() {
		//
		SurveyRawData paperLikeData = new SurveyRawData();
		//
		VariablesMap variables = VariablesMapTest.createVariablesMap_oneLevel();
		paperLikeData.setVariablesMap(variables);

		// First household but split in several questionnaires
		for (int i=0; i<3; i++) {
			QuestionnaireData q1 = new QuestionnaireData();
			q1.setIdentifier("S0000001");
			q1.putValue("20000", "HOUSEHOLD_INCOME");
			q1.putValue("740 Evergreen Terrace", "ADDRESS");
			String firstName = List.of("Homer", "Marge", "Santa's Little Helper").get(i);
			q1.putValue(firstName, "FIRST_NAME", Pair.of("INDIVIDUALS_LOOP", i));
			paperLikeData.addQuestionnaire(q1);
		}

		// Second household with one individual
		QuestionnaireData q2 = new QuestionnaireData();
		q2.setIdentifier("S0000002");
		q2.putValue("30000", "HOUSEHOLD_INCOME");
		q2.putValue("742 Evergreen Terrace", "ADDRESS");
		q2.putValue("Ned", "FIRST_NAME", Pair.of("INDIVIDUALS_LOOP", 0));
		paperLikeData.addQuestionnaire(q2);

		//
		vtlBindings.convertToVtlDataset(paperLikeData, "TEST");
		Dataset ds = vtlBindings.getDataset("TEST");

		//
		assertNotNull(ds);
		//
		assertEquals(4, ds.getDataPoints().size());
		Set<String> expectedFirstNames = Set.of("Homer", "Marge", "Santa's Little Helper", "Ned");
		Set<String> dsFirstNames = new HashSet<>();
		ds.getDataPoints().forEach(
				dataPoint -> dsFirstNames.add(
						(String) dataPoint.get("FIRST_NAME") )
		);
		assertEquals(expectedFirstNames, dsFirstNames);
	}

	@Test
	public void convertSurveyRawDataWithEmptyGroup() {
		//
		SurveyRawData srd = new SurveyRawData();
		//
		VariablesMap variablesMap = new VariablesMap();
		variablesMap.putVariable(new Variable("FOO", variablesMap.getRootGroup(), VariableType.STRING));
		variablesMap.putGroup(new Group("DEPTH1", variablesMap.getRootGroup().getName()));
		variablesMap.putVariable(new Variable("FOO1", variablesMap.getGroup("DEPTH1"), VariableType.STRING));
		srd.setVariablesMap(variablesMap);
		//
		QuestionnaireData questionnaire = new QuestionnaireData();
		questionnaire.putValue("foo", "FOO");
		questionnaire.getAnswers().getSubGroup("DEPTH1"); // this call adds a GroupInstance in the data object
		srd.addQuestionnaire(questionnaire);

		//
		vtlBindings.convertToVtlDataset(srd, "test");
		Dataset dataset = vtlBindings.getDataset("test");

		//
		assertEquals(1, dataset.getDataPoints().size());
	}

	@Test
	public void convertSurveyRawDataWithTwoParallelGroups() {
		//
		SurveyRawData srd = new SurveyRawData();
		//
		VariablesMap variablesMap = new VariablesMap();
		variablesMap.putVariable(new Variable("FOO", variablesMap.getRootGroup(), VariableType.STRING));
		variablesMap.putGroup(new Group("GROUP_A", variablesMap.getRootGroup().getName()));
		variablesMap.putGroup(new Group("GROUP_B", variablesMap.getRootGroup().getName()));
		variablesMap.putVariable(new Variable("FOO_A", variablesMap.getGroup("GROUP_A"), VariableType.STRING));
		variablesMap.putVariable(new Variable("FOO_B", variablesMap.getGroup("GROUP_B"), VariableType.STRING));
		srd.setVariablesMap(variablesMap);
		//
		QuestionnaireData completeQuestionnaire = new QuestionnaireData();
		completeQuestionnaire.putValue("foo", "FOO");
		completeQuestionnaire.putValue("foo_a", "FOO_A", Pair.of("GROUP_A", 0));
		completeQuestionnaire.putValue("foo_a", "FOO_A", Pair.of("GROUP_A", 1));
		completeQuestionnaire.putValue("foo_b", "FOO_B", Pair.of("GROUP_B", 0));
		srd.addQuestionnaire(completeQuestionnaire);
		//
		QuestionnaireData oneGroupQuestionnaire = new QuestionnaireData();
		oneGroupQuestionnaire.putValue("foo_a", "FOO_A", Pair.of("GROUP_A", 0));
		srd.addQuestionnaire(oneGroupQuestionnaire);

		//
		vtlBindings.convertToVtlDataset(srd, "test");
		Dataset dataset = vtlBindings.getDataset("test");

		//
		assertEquals(4, dataset.getDataPoints().size());
	}

}
