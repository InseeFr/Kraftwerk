package fr.insee.kraftwerk.core.outputs;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import fr.insee.kraftwerk.core.Constants;
import fr.insee.kraftwerk.core.metadata.Variable;
import fr.insee.kraftwerk.core.metadata.VariableType;
import fr.insee.kraftwerk.core.metadata.VariablesMap;
import fr.insee.vtl.model.Structured;

/**
 * Class to generate import scripts for the output tables. Some methods that
 * could be implemented later: R base script and Python with pandas script.
 */
public abstract class ImportScript {

	private static final String STRING_LENGTH = "255";
	protected final List<TableScriptInfo> tableScriptInfoList;
	protected static final String END_LINE = "\n";

	/** @see TableScriptInfo */
	protected ImportScript(List<TableScriptInfo> tableScriptInfoList) {
		this.tableScriptInfoList = tableScriptInfoList;
	}

	public abstract String generateScript();

	public static Map<String, Variable> getAllLength(Structured.DataStructure dataStructure,
			Map<String, VariablesMap> metadataVariables) {
		Map<String, Variable> result = new LinkedHashMap<>();
		// datastructure : noms complets
		// metadata : suffixe du nom
		// We loop with all variables in the current dataset we want to export
		for (String variableName : dataStructure.keySet()) {
			// We try to find it from the first datasets containing together all variables
			// (except VTL and Kraftwerk-created ones)

			for (Entry<String, VariablesMap> metadata : metadataVariables.entrySet()) {
				VariablesMap variablesMap = metadata.getValue();

				// We treat the identifiers
				if (variablesMap.getIdentifierNames().contains(variableName) && !result.containsKey(variableName)) {
					result.put(variableName,
							new Variable(variableName, variablesMap.getGroup(variableName), VariableType.STRING, "32"));
				}
				if (variablesMap.getDistinctVariableNamesAndFullyQualifiedNames().contains(variableName)) {
					variableName = addOrReplaceLength(result, variableName, variablesMap);
				} else if (!result.containsKey(variableName)) {
					result.put(variableName, new Variable(variableName,
							variablesMap.getGroup(Constants.ROOT_GROUP_NAME), VariableType.STRING, STRING_LENGTH));

				}
			}

		}
		return result;
	}

	private static String addOrReplaceLength(Map<String, Variable> result, String variableName,
			VariablesMap variablesMap) {
		variableName = getRootName(variableName);
		Variable variable = variablesMap.getVariable(variableName);

		final String newLengthString =getNewLength(variable, variableName);
		if (result.containsKey(variableName)
				&& isNewLengthUpperThanExisting(newLengthString, result.get(variableName).getSasFormat())) {
			// Variable already put in result, and not a float (if float exists, we do
			// nothing)
			result.replace(variableName, new Variable(variableName, result.get(variableName).getGroup(),
					result.get(variableName).getType(), newLengthString));
		}
		// Filter results are boolean, value "true" or "false"
		if (variableName.toUpperCase().contains(Constants.FILTER_RESULT_PREFIX)) {
			result.put(variableName, new Variable(variableName, variablesMap.getGroup(Constants.ROOT_GROUP_NAME),
					VariableType.BOOLEAN, "1"));
		}

		result.computeIfAbsent(variableName,
				v -> new Variable(v, variable.getGroup(), variable.getType(), newLengthString));

		return variableName;
	}

	private static String getNewLength(Variable variable, String variableName) {
		 String newLengthString = variable.getSasFormat();

			// We already got the variable, so we check to see if the lengths are different
			// -> take the maximum one then
			if (newLengthString == null && !variableName.toUpperCase().contains(Constants.FILTER_RESULT_PREFIX)) {
				newLengthString = STRING_LENGTH;
			}
			return newLengthString;
	}
	
	private static boolean isNewLengthUpperThanExisting(String newLengthString, String existingLengthString) {
		return newLengthString != null && isNotFloat(newLengthString) && isNotFloat(existingLengthString)
				&& Integer.parseInt(existingLengthString) < Integer.parseInt(newLengthString);
	}

	private static boolean isNotFloat(String length) {
		return !length.contains(".");
	}

	/** Return the variable name without the group in the prefixes. */
	static String getRootName(String name) {
		return name.substring(name.lastIndexOf('.') + 1);
	}

}
