package fr.insee.kraftwerk.core.vtl;

import fr.insee.bpm.metadata.model.Group;
import fr.insee.bpm.metadata.model.MetadataModel;
import fr.insee.bpm.metadata.model.Variable;
import fr.insee.bpm.metadata.model.VariableType;
import fr.insee.bpm.metadata.model.VariablesMap;
import fr.insee.kraftwerk.core.Constants;
import fr.insee.kraftwerk.core.rawdata.GroupData;
import fr.insee.kraftwerk.core.rawdata.GroupInstance;
import fr.insee.kraftwerk.core.rawdata.QuestionnaireData;
import fr.insee.kraftwerk.core.rawdata.SurveyRawData;
import fr.insee.kraftwerk.core.utils.KraftwerkExecutionContext;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class VtlJsonDatasetWriterTest {

	@Mock private SurveyRawData surveyData;
	@Mock private MetadataModel metadataModel;
	@Mock private VariablesMap variablesMap;
	@Mock private KraftwerkExecutionContext context;

	private VtlJsonDatasetWriter writer;

	@BeforeEach
	void setUp() {
		when(surveyData.getMetadataModel()).thenReturn(metadataModel);
		when(metadataModel.getVariables()).thenReturn(variablesMap);
		when(metadataModel.getSubGroupNames()).thenReturn(Collections.emptyList());
		when(variablesMap.getVariableNames()).thenReturn(new HashSet<>());
		when(context.isAddStates()).thenReturn(false);

		writer = new VtlJsonDatasetWriter(surveyData, "test-dataset", context);
	}

	@Test
	void writeVtlJsonDataset_shouldCreateFileAndReturnItsPath_whenDataIsEmpty() {
		// GIVEN
		when(surveyData.getQuestionnaires()).thenReturn(Collections.emptyList());

		// WHEN
		String path = writer.writeVtlJsonDataset();

		// THEN
		assertThat(path).isNotNull().endsWith(".json");
		assertThat(Files.exists(Path.of(path))).isTrue();
	}

	@Test
	@SuppressWarnings("unchecked")
	void writeVtlJsonDataset_shouldProduceWellFormedJson_withBothTopLevelKeys() throws IOException, ParseException {
		// GIVEN
		when(surveyData.getQuestionnaires()).thenReturn(Collections.emptyList());

		// WHEN
		String path = writer.writeVtlJsonDataset();

		// THEN
		String content = Files.readString(Path.of(path), StandardCharsets.UTF_8);
		JSONObject parsed = (JSONObject) new JSONParser().parse(content);

		assertThat(parsed).containsKeys("dataStructure", "dataPoints");
	}

	@Test
	void jsonDataStructure_shouldAlwaysContainFourRootIdentifiers() {
		// WHEN
		JSONArray structure = writer.jsonDataStructure();

		// THEN
		List<String> names = extractNames(structure);
		assertThat(names).contains(
				Constants.ROOT_IDENTIFIER_NAME,
				Constants.SURVEY_UNIT_IDENTIFIER_NAME,
				Constants.VALIDATION_DATE_NAME,
				Constants.QUESTIONNAIRE_STATE_NAME
		);
	}

	@Test
	@SuppressWarnings("unchecked")
	void jsonDataStructure_shouldContainGroupIdentifier_whenSubGroupExists() {
		// GIVEN
		when(metadataModel.getSubGroupNames()).thenReturn(List.of("LOOP1"));

		// WHEN
		JSONArray structure = writer.jsonDataStructure();

		// THEN
		assertThat(extractNames(structure)).contains("LOOP1");
		assertThat(findEntry(structure, "LOOP1")).containsEntry("role", "IDENTIFIER");
	}

	@Test
	@SuppressWarnings("unchecked")
	void jsonDataStructure_shouldContainVariable_withCorrectTypeAndRole() {
		// GIVEN
		Variable variable = new Variable("MY_VAR", mock(Group.class), VariableType.STRING);
		when(variablesMap.getVariableNames()).thenReturn(Set.of("MY_VAR"));
		when(variablesMap.getVariable("MY_VAR")).thenReturn(variable);

		// WHEN
		JSONArray structure = writer.jsonDataStructure();

		// THEN
		JSONObject entry = findEntry(structure, "MY_VAR");
		assertThat(entry)
				.containsEntry("type","STRING")
				.containsEntry("role", "MEASURE");
	}

	@Test
	void jsonDataStructure_shouldAddStateField_whenAddStatesIsEnabled() {
		// GIVEN
		when(context.isAddStates()).thenReturn(true);
		Variable variable = new Variable("MY_VAR", mock(Group.class), VariableType.STRING);
		when(variablesMap.getVariableNames()).thenReturn(Set.of("MY_VAR"));
		when(variablesMap.getVariable("MY_VAR")).thenReturn(variable);
		String stateFieldName = "MY_VAR" + Constants.VARIABLE_STATE_SUFFIX_NAME;
		when(variablesMap.getVariable(stateFieldName)).thenReturn(null);

		writer = new VtlJsonDatasetWriter(surveyData, "test", context);

		// WHEN
		JSONArray structure = writer.jsonDataStructure();

		// THEN
		assertThat(extractNames(structure)).contains(stateFieldName);
	}

	@Test
	void jsonDataPoints_shouldBeEmpty_whenNoQuestionnaires() {
		// GIVEN
		when(surveyData.getQuestionnaires()).thenReturn(Collections.emptyList());

		// WHEN
		JSONArray dataPoints = writer.jsonDataPoints();

		// THEN
		assertThat(dataPoints).isEmpty();
	}

	@Test
	void jsonDataPoints_shouldContainOneRow_perQuestionnaireWithoutSubGroups() {
		// GIVEN
		writer.jsonDataStructure();
		//3 answers
		List<QuestionnaireData> questionnaires = java.util.stream.IntStream.range(0, 3)
				.mapToObj(i -> {
					GroupInstance root = mock(GroupInstance.class);
					when(root.getValue(Constants.SURVEY_UNIT_IDENTIFIER_NAME)).thenReturn(null);
					when(root.getValue(Constants.VALIDATION_DATE_NAME)).thenReturn(null);
					when(root.getValue(Constants.QUESTIONNAIRE_STATE_NAME)).thenReturn(null);
					when(root.hasSubGroups()).thenReturn(false);
					when(root.getVariableNames()).thenReturn(new HashSet<>());

					QuestionnaireData qd = mock(QuestionnaireData.class);
					when(qd.getIdentifier()).thenReturn("ID_" + i);
					when(qd.getAnswers()).thenReturn(root);
					return qd;
				})
				.toList();
		when(surveyData.getQuestionnaires()).thenReturn(questionnaires);

		// WHEN
		JSONArray dataPoints = writer.jsonDataPoints();

		// THEN
		assertThat(dataPoints).hasSize(3);
	}

	@Test
	void jsonDataPoints_shouldContainOneRowPerGroupInstance_whenSubGroupsExist() {
		// GIVEN
		when(metadataModel.getSubGroupNames()).thenReturn(List.of("LOOP1"));
		writer.jsonDataStructure();

		QuestionnaireData qd = buildQuestionnaireWithSubGroup(2);
		when(surveyData.getQuestionnaires()).thenReturn(List.of(qd));

		// WHEN
		JSONArray dataPoints = writer.jsonDataPoints();

		// THEN : 2 instances de groupe → 2 lignes
		assertThat(dataPoints).hasSize(2);
	}

	@Test
	void jsonDataPoints_shouldWriteSingleRow_whenSubGroupsAreEmpty() {
		// GIVEN
		when(metadataModel.getSubGroupNames()).thenReturn(List.of("LOOP1"));
		writer.jsonDataStructure();

		QuestionnaireData qd = buildQuestionnaireWithSubGroup(0);
		when(surveyData.getQuestionnaires()).thenReturn(List.of(qd));

		// WHEN
		JSONArray dataPoints = writer.jsonDataPoints();

		// THEN
		assertThat(dataPoints).hasSize(1);
	}

	@Test
	void convertToVtlType_shouldReturnString_whenTypeIsNull() {
		assertThat(VtlJsonDatasetWriter.convertToVtlType(null)).isEqualTo("STRING");
	}

	@ParameterizedTest
	@EnumSource(VariableType.class)
	void convertToVtlType_shouldReturnCorrectVtlType_forEachVariableType(VariableType variableType) {
		assertThat(VtlJsonDatasetWriter.convertToVtlType(variableType)).isEqualTo(variableType.getVtlType());
	}

	// UTILS
	@SuppressWarnings("unchecked")
	private List<String> extractNames(JSONArray structure) {
		return structure.stream()
				.map(o -> ((JSONObject) o).get("name"))
				.toList();
	}

	private JSONObject findEntry(JSONArray structure, String name) {
		for (Object o : structure) {
			if (o instanceof JSONObject entry && name.equals(entry.get("name"))) {
				return entry;
			}
		}
		throw new AssertionError("Entry not found in dataStructure: " + name);
	}

	private QuestionnaireData buildQuestionnaireWithSubGroup(int instanceCount) {
		GroupInstance root = mock(GroupInstance.class);
		when(root.getValue(Constants.SURVEY_UNIT_IDENTIFIER_NAME)).thenReturn(null);
		when(root.getValue(Constants.VALIDATION_DATE_NAME)).thenReturn(null);
		when(root.getValue(Constants.QUESTIONNAIRE_STATE_NAME)).thenReturn(null);
		when(root.getVariableNames()).thenReturn(new HashSet<>());
		when(root.hasSubGroups()).thenReturn(true);
		when(root.getSubGroupNames()).thenReturn(Set.of("LOOP1"));

		GroupData groupData = mock(GroupData.class);
		Set<String> instanceIds = new HashSet<>(java.util.stream.IntStream.range(0, instanceCount)
				.mapToObj(i -> "inst_" + i)
				.toList());
		when(groupData.getInstanceIds()).thenReturn(instanceIds);

		for (String instanceId : instanceIds) {
			GroupInstance groupInstance = mock(GroupInstance.class);
			when(groupInstance.getId()).thenReturn(instanceId);
			when(groupInstance.getVariableNames()).thenReturn(new HashSet<>());
			when(groupData.getInstance(instanceId)).thenReturn(groupInstance);
		}

		when(root.getSubGroup("LOOP1")).thenReturn(groupData);

		QuestionnaireData qd = mock(QuestionnaireData.class);
		when(qd.getIdentifier()).thenReturn("ID001");
		when(qd.getAnswers()).thenReturn(root);
		return qd;
	}
}