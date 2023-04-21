package fr.insee.kraftwerk.core.vtl;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import fr.insee.kraftwerk.core.Constants;
import fr.insee.kraftwerk.core.metadata.Variable;
import fr.insee.kraftwerk.core.metadata.VariableType;
import fr.insee.kraftwerk.core.metadata.VariablesMap;
import fr.insee.kraftwerk.core.rawdata.GroupData;
import fr.insee.kraftwerk.core.rawdata.GroupInstance;
import fr.insee.kraftwerk.core.rawdata.QuestionnaireData;
import fr.insee.kraftwerk.core.rawdata.SurveyRawData;
import lombok.extern.log4j.Log4j2;

/**
 * Class to write temporary VTL datasets. These datasets are JSON files that has
 * the format expected by VTL modules.
 */
@Log4j2
public class VtlJsonDatasetWriter {

	private final SurveyRawData surveyData;
	private final VariablesMap variablesMap;
	private final String datasetName;

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
	public VtlJsonDatasetWriter(SurveyRawData surveyData, String datasetName) {
		this.surveyData = surveyData;
		this.variablesMap = surveyData.getVariablesMap();
		this.datasetName = datasetName;
	}

	/**
	 * Write the given survey data in a temporary JSON file '[datasetName].json'.
	 *
	 * @return The path to get the temporary dataset file created.
	 */
	@SuppressWarnings("unchecked")
	public String writeVtlJsonDataset() {

		JSONObject jsonVtlDataset = new JSONObject();

		// dataStructure
		jsonVtlDataset.put("dataStructure", jsonDataStructure());

		// dataPoints
		jsonVtlDataset.put("dataPoints", jsonDataPoints());

		// Write the temporary dataset file
		try {
			File datasetFile = File.createTempFile(datasetName, ".json");
			datasetFile.deleteOnExit();
			String tempPath = datasetFile.getAbsolutePath();
			Files.write(Paths.get(tempPath), jsonVtlDataset.toJSONString().getBytes(StandardCharsets.UTF_8), StandardOpenOption.TRUNCATE_EXISTING);
			return tempPath;
		} catch (IOException e) {
			log.error(String.format("Unable to write the temporary file '%s.json'.", datasetName), e);
			return null;
		}
	}

	@SuppressWarnings("unchecked")
	private JSONArray jsonDataStructure() {

		JSONArray dataStructure = new JSONArray();

		Integer variableNumber = 0;

		// Root level identifier
		JSONObject jsonVtlIdentifier = new JSONObject();
		jsonVtlIdentifier.put("name", Constants.ROOT_IDENTIFIER_NAME);
		jsonVtlIdentifier.put("type", "STRING");
		jsonVtlIdentifier.put("role", "IDENTIFIER");
		dataStructure.add(jsonVtlIdentifier);
		columnsMapping.put(Constants.ROOT_IDENTIFIER_NAME, variableNumber);
		variableNumber++;
		// Group identifiers
		for (String groupName : variablesMap.getSubGroupNames()) {
			// The group name is the identifier variable for the group
			JSONObject jsonVtlGroupIdentifier = new JSONObject();
			jsonVtlGroupIdentifier.put("name", groupName);
			jsonVtlGroupIdentifier.put("type", "STRING");
			jsonVtlGroupIdentifier.put("role", "IDENTIFIER");
			dataStructure.add(jsonVtlGroupIdentifier);
			columnsMapping.put(groupName, variableNumber);
			variableNumber++;
		}

		// Variables
		for (String variableName : variablesMap.getVariableNames()) {
			Variable variable = variablesMap.getVariable(variableName);
			JSONObject jsonVtlVariable = new JSONObject();
			//jsonVtlVariable.put("name", variablesMap.getFullyQualifiedName(variableName));
			jsonVtlVariable.put("name", variableName); // recent change (see GroupProcessing class)
			jsonVtlVariable.put("type", convertToVtlType(variable.getType()));
			jsonVtlVariable.put("role", "MEASURE");
			dataStructure.add(jsonVtlVariable);
			columnsMapping.put(variableName, variableNumber);
			variableNumber++;
		}

		return dataStructure;
	}

	@SuppressWarnings("unchecked")
	private JSONArray jsonDataPoints() {

		JSONArray dataPoints = new JSONArray();

		for (QuestionnaireData questionnaireData : surveyData.getQuestionnaires()) {
			GroupInstance rootInstance = questionnaireData.getAnswers();

			String[] rowValues = new String[columnsMapping.size()];
			Arrays.fill(rowValues, null); // NOTE: recent change here to differentiate empty string and non-response
			// (previous implementation was: fill with empty strings ("")

			// Root level identifier
			rowValues[0] = questionnaireData.getIdentifier();
			
			// Root variables values
			for (String variableName : rootInstance.getVariableNames()) {
				if (columnsMapping.get(variableName) != null) {
					String value = rootInstance.getValue(variableName);
					if (variablesMap.getVariable(variableName).getType() == VariableType.BOOLEAN) { // TODO: document me
						value = convertBooleanValue(value);
					}
					rowValues[columnsMapping.get(variableName)] = value;
				} else {
					log.debug(String.format("Variable named \"%s\" found in data object is unknown.", variableName));
				}
			}

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
						for (String variableName : groupInstance.getVariableNames()) {
							if (columnsMapping.get(variableName) != null) {
								String value = groupInstance.getValue(variableName);
								if (variablesMap.getVariable(variableName).getType() == VariableType.BOOLEAN) { // TODO: document me
									value = convertBooleanValue(value);
								}
								groupRowValues[columnsMapping.get(variableName)] = value;
							} else {
								log.debug(String.format("Variable named \"%s\" found in data object is unknown.", variableName));
							}
						}

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

	public static String convertToVtlType(VariableType variableType) {
		if (variableType == null) {
			log.debug("null variable type given to convertToVtlType method, this should NEVER happen!");
			return "STRING";
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

}
