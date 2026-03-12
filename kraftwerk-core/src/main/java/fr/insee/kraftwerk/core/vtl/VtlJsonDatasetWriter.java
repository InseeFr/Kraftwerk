package fr.insee.kraftwerk.core.vtl;

import fr.insee.bpm.metadata.model.MetadataModel;
import fr.insee.bpm.metadata.model.Variable;
import fr.insee.bpm.metadata.model.VariableType;
import fr.insee.kraftwerk.core.Constants;
import fr.insee.kraftwerk.core.rawdata.GroupData;
import fr.insee.kraftwerk.core.rawdata.GroupInstance;
import fr.insee.kraftwerk.core.rawdata.QuestionnaireData;
import fr.insee.kraftwerk.core.rawdata.SurveyRawData;
import fr.insee.kraftwerk.core.utils.KraftwerkExecutionContext;
import lombok.extern.log4j.Log4j2;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Class to write temporary VTL datasets. These datasets are JSON files that has
 * the format expected by VTL modules.
 */
@Log4j2
public class VtlJsonDatasetWriter {

	private static final String ROLE = "role";
	private static final String TYPE = "type";
	private static final String NAME = "name";
	private static final String IDENTIFIER = "IDENTIFIER";
	private static final String MEASURE = "MEASURE";
	private static final String STRING = "STRING";

	private final SurveyRawData surveyData;
	private final MetadataModel metadataModel;
	private final String datasetName;
	private final KraftwerkExecutionContext kraftwerkExecutionContext;

	/*
	 * Local variable to ensure that data points will have variables in the same
	 * order as data structure. Keys: a variable name. Values: the column number of
	 * the variable in the dataset.
	 */
	private final Map<String, Integer> columnsMapping = new HashMap<>();

	/**
	 * @param surveyData  Survey data parsed into a SurveyRawData object.
	 * @param datasetName The name (without extension) of the dataset file which
	 *                       will be written.
	 */
	public VtlJsonDatasetWriter(SurveyRawData surveyData,
								String datasetName,
								KraftwerkExecutionContext kraftwerkExecutionContext
	) {
		this.surveyData = surveyData;
		this.metadataModel = surveyData.getMetadataModel();
		this.datasetName = datasetName;
		this.kraftwerkExecutionContext = kraftwerkExecutionContext;
	}

	//TODO Remove JSON temp file writing (not cloud native)
	/**
	 * Write the given survey data in a temporary JSON file '[datasetName].json'.
	 *
	 * @return The path to get the temporary dataset file created.
	 */
	public String writeVtlJsonDataset() {
		// Write the temporary dataset file
		try {
			File datasetFile = File.createTempFile(datasetName, ".json");
			datasetFile.deleteOnExit();
			String tempPath = datasetFile.getAbsolutePath();

			try (BufferedWriter writer = Files.newBufferedWriter(
					Paths.get(tempPath),
					StandardCharsets.UTF_8,
					StandardOpenOption.TRUNCATE_EXISTING)) {

				writer.write("{");

				// dataStructure
				writer.write("\"dataStructure\":");
				JSONValue.writeJSONString(getDataStructureJSONArray(), writer);
				writer.write(",");

				// dataPoints
				writer.write("\"dataPoints\":");
				JSONValue.writeJSONString(getDataPointsJSONArray(), writer);

				writer.write("}");
			}
			return tempPath;
		} catch (IOException e) {
			log.error("Unable to write the temporary file '{}.json' : {}", datasetName, e);
			return null;
		}
	}

	protected JSONArray getDataStructureJSONArray() {
		JSONArray dataStructure = new JSONArray();

		Integer variableNumber = 0;

		// Root level identifiers
		variableNumber = addRootLevelIdentifiers(dataStructure, variableNumber);

		// Group identifiers
		variableNumber = addGroupIdentifiers(dataStructure, variableNumber);

		// Variables
		addVariablesToDataStructure(dataStructure, variableNumber);

		return dataStructure;
	}

	/**
	 * Adds the root/interrogaiton level identifiers to the dataStructure
	 * @param variableNumber the beginning index
	 * @return the incremented variableNumber
	 */
	private Integer addRootLevelIdentifiers(JSONArray dataStructure, Integer variableNumber) {
		variableNumber = addToDataStructure(Constants.ROOT_IDENTIFIER_NAME, STRING, IDENTIFIER,
				dataStructure, variableNumber);
		variableNumber = addToDataStructure(Constants.SURVEY_UNIT_IDENTIFIER_NAME, STRING, MEASURE,
				dataStructure, variableNumber);
		variableNumber = addToDataStructure(Constants.VALIDATION_DATE_NAME, STRING, MEASURE,
				dataStructure, variableNumber);
		variableNumber = addToDataStructure(Constants.QUESTIONNAIRE_STATE_NAME, STRING, MEASURE,
				dataStructure, variableNumber);
		return variableNumber;
	}

	/**
	 * Adds the group identifiers to the dataStructure
	 * @param variableNumber the beginning index
	 * @return the incremented variableNumber
	 */
	private Integer addGroupIdentifiers(JSONArray dataStructure, Integer variableNumber) {
		for (String groupName : metadataModel.getSubGroupNames()) {
			variableNumber = addToDataStructure(groupName, STRING, IDENTIFIER, dataStructure, variableNumber);
		}
		return variableNumber;
	}

	/**
	 * Adds the variables to the dataStructure
	 * @param variableNumber the beginning index
	 */
	private void addVariablesToDataStructure(JSONArray dataStructure, Integer variableNumber) {
		List<String> variableNames = new ArrayList<>(metadataModel.getVariables().getVariableNames());
		for (String variableName : variableNames) {
			if(variableName.endsWith(Constants.VARIABLE_STATE_SUFFIX_NAME)
				&& !variableName.equals(Constants.LAST_STATE_NAME)
			){
				continue;
			}
			Variable variable = metadataModel.getVariables().getVariable(variableName);
			variableNumber = addToDataStructure(
					variableName,
					convertToVtlType(variable.getType()),
					MEASURE,
					dataStructure,
					variableNumber
			);
			boolean isFilterResultOrMissing = variableName.startsWith(Constants.FILTER_RESULT_PREFIX)
					|| variableName.endsWith(Constants.MISSING_SUFFIX);
			if(kraftwerkExecutionContext.isAddStates() && !isFilterResultOrMissing){
				addVariableStateFieldToStructure(variable, dataStructure, variableNumber);
				variableNumber++;
			}
		}
	}

	/**
	 * Adds a new object in the VTL dataStructure and columnsMapping
	 * @param name name of the object
	 * @param type VTL type (cf. VariableType VTL names)
	 * @param role role of the object (IDENTIFIER or MEASURE)
	 * @param dataStructure dataStructure to insert into
	 * @param variableNumber index of the object in the mapping
	 * @return variableNumber + 1
	 */
	@SuppressWarnings("unchecked")
	private Integer addToDataStructure(
			String name,
			String type,
			String role,
			JSONArray dataStructure,
			Integer variableNumber
	) {
		JSONObject jsonObject = new JSONObject();
		jsonObject.put(NAME, name);
		jsonObject.put(TYPE, type);
		jsonObject.put(ROLE, role);
		dataStructure.add(jsonObject);
		columnsMapping.put(name, variableNumber);
		variableNumber++;
		return variableNumber;
	}

	/**
	 * Adds the variable state (COLLECTED, EDITED...) to the VTL dataset structure
	 */
	@SuppressWarnings("unchecked")
	private void addVariableStateFieldToStructure(Variable variable, JSONArray dataStructure, int variableNumber) {
		JSONObject jsonVtlVariable = new JSONObject();
		String variableStateFieldName = variable.getName() + Constants.VARIABLE_STATE_SUFFIX_NAME;
		jsonVtlVariable.put(NAME, variableStateFieldName);
		jsonVtlVariable.put(TYPE, convertToVtlType(VariableType.STRING));
		jsonVtlVariable.put(ROLE, MEASURE);

		dataStructure.add(jsonVtlVariable);

		if (!columnsMapping.containsKey(variableStateFieldName)) {
			columnsMapping.put(variableStateFieldName, variableNumber);
		}

		if (metadataModel.getVariables().getVariable(variableStateFieldName) == null) {
			metadataModel.getVariables().putVariable(new Variable(
					variableStateFieldName,
					variable.getGroup(),
					VariableType.STRING
			));
		}
	}

	@SuppressWarnings("unchecked")
	protected JSONArray getDataPointsJSONArray() {

		JSONArray dataPoints = new JSONArray();

		for (QuestionnaireData questionnaireData : surveyData.getQuestionnaires()) {
			GroupInstance rootInstance = questionnaireData.getAnswers();

			String[] rowValues = new String[datasetWidth()];
			Arrays.fill(rowValues, null); // NOTE: recent change here to differentiate empty string and non-response
			// (previous implementation was: fill with empty strings ("")

			// Root level identifiers
			rowValues[0] = questionnaireData.getIdentifier();
			rowValues[1] = questionnaireData.getAnswers().getValue(Constants.SURVEY_UNIT_IDENTIFIER_NAME);
			rowValues[2] = questionnaireData.getAnswers().getValue(Constants.VALIDATION_DATE_NAME);
			rowValues[3] = questionnaireData.getAnswers().getValue(Constants.QUESTIONNAIRE_STATE_NAME);
			
			// Root variables values
			addValuesToRow(rootInstance, rowValues);

			// TODO: only works with at most one level of subgroups. Implement recursion or

			// If no subgroups, write line right away
			if (! rootInstance.hasSubGroups()) {
				JSONArray array = new JSONArray();
				array.addAll(Arrays.asList(rowValues));
				dataPoints.add(array);
			}

			else {
				boolean emptySubGroups = true;

				for (String groupName : rootInstance.getSubGroupNames()) {
					GroupData groupData = rootInstance.getSubGroup(groupName);

					// Group values: one row per group instance
					for (String groupId : groupData.getInstanceIds()) {
						String [] groupRowValues = rowValues.clone();
						GroupInstance groupInstance = groupData.getInstance(groupId);
						groupRowValues[columnsMapping.get(groupName)] = groupInstance.getId();
						addValuesToRow(groupInstance, groupRowValues);

						JSONArray array = new JSONArray();
						array.addAll(Arrays.asList(groupRowValues));
						dataPoints.add(array);
						emptySubGroups = false;
					}
				}

				// If all subgroups are empty, write a single line
				if (emptySubGroups) {
					JSONArray array = new JSONArray();
					array.addAll(Arrays.asList(rowValues));
					dataPoints.add(array);
				}
			}
		}

		return dataPoints;
	}

	private void addValuesToRow(GroupInstance groupInstance, String[] rowValues) {
		for (String variableName : groupInstance.getVariableNames()) {
			if (columnsMapping.get(variableName) == null) {
				log.debug(String.format("Variable named \"%s\" found in data object is unknown.", variableName));
				continue;
			}
			String value = groupInstance.getValue(variableName);
			Set<String> rootLevelVariables = Set.of(
					Constants.SURVEY_UNIT_IDENTIFIER_NAME,
					Constants.VALIDATION_DATE_NAME,
					Constants.QUESTIONNAIRE_STATE_NAME
			);
			if (
					!rootLevelVariables.contains(variableName)
					&& metadataModel.getVariables().getVariable(variableName).getType() == VariableType.BOOLEAN
			) {
				value = convertBooleanValue(value);
			}
			rowValues[columnsMapping.get(variableName)] = value;
		}
	}

	public static String convertToVtlType(VariableType variableType) {
		if (variableType == null) {
			log.debug("null variable type given to convertToVtlType method, this should NEVER happen!");
			return STRING;
		}
		return variableType.getVtlType();
	}

	/** Compatible boolean values for "true */
	private static final Set<String> trueValues = Set.of("true", "1");
	/** Compatible boolean values for "false" */
	private static final Set<String> falseValues = Set.of("false", "0");

	/** Method to convert compatible boolean values to "true" or "false". */
	private static String convertBooleanValue(String value) {
		if (value != null) {
			if (trueValues.contains(value)) return "true";
			else if (falseValues.contains(value)) return "false";
			else return null;
		} else {
			return null;
		}
	}

    private int datasetWidth() {
        return columnsMapping.values().stream()
				.mapToInt(Integer::intValue)
                .max()
                .orElse(-1) + 1;
    }

}
