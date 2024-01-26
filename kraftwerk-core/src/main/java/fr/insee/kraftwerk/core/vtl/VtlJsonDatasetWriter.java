package fr.insee.kraftwerk.core.vtl;

import fr.insee.kraftwerk.core.Constants;
import fr.insee.kraftwerk.core.metadata.MetadataModel;
import fr.insee.kraftwerk.core.metadata.Variable;
import fr.insee.kraftwerk.core.metadata.VariableType;
import fr.insee.kraftwerk.core.rawdata.GroupData;
import fr.insee.kraftwerk.core.rawdata.GroupInstance;
import fr.insee.kraftwerk.core.rawdata.QuestionnaireData;
import fr.insee.kraftwerk.core.rawdata.SurveyRawData;
import lombok.extern.log4j.Log4j2;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

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
	private static final String STRING = "STRING";
	private final SurveyRawData surveyData;
	private final MetadataModel metadataModel;
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
		this.metadataModel = surveyData.getMetadataModel();
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
		jsonVtlIdentifier.put(NAME, Constants.ROOT_IDENTIFIER_NAME);
		jsonVtlIdentifier.put(TYPE, STRING);
		jsonVtlIdentifier.put(ROLE, IDENTIFIER);
		dataStructure.add(jsonVtlIdentifier);
		columnsMapping.put(Constants.ROOT_IDENTIFIER_NAME, variableNumber);
		variableNumber++;
		// Group identifiers
		for (String groupName : metadataModel.getSubGroupNames()) {
			// The group name is the identifier variable for the group
			JSONObject jsonVtlGroupIdentifier = new JSONObject();
			jsonVtlGroupIdentifier.put(NAME, groupName);
			jsonVtlGroupIdentifier.put(TYPE, STRING);
			jsonVtlGroupIdentifier.put(ROLE, IDENTIFIER);
			dataStructure.add(jsonVtlGroupIdentifier);
			columnsMapping.put(groupName, variableNumber);
			variableNumber++;
		}

		// Variables
		for (String variableName : metadataModel.getVariables().getVariableNames()) {
			Variable variable = metadataModel.getVariables().getVariable(variableName);
			JSONObject jsonVtlVariable = new JSONObject();
			jsonVtlVariable.put(NAME, variableName); // recent change (see GroupProcessing class)
			jsonVtlVariable.put(TYPE, convertToVtlType(variable.getType()));
			jsonVtlVariable.put(ROLE, "MEASURE");
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
			if (columnsMapping.get(variableName) != null) {
				String value = groupInstance.getValue(variableName);
				if (metadataModel.getVariables().getVariable(variableName).getType() == VariableType.BOOLEAN) {
					value = convertBooleanValue(value);
				}
				rowValues[columnsMapping.get(variableName)] = value;
			} else {
				log.debug(String.format("Variable named \"%s\" found in data object is unknown.", variableName));
			}
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

}
