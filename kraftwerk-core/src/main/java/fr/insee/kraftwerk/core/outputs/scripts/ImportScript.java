package fr.insee.kraftwerk.core.outputs.scripts;

import fr.insee.kraftwerk.core.Constants;
import fr.insee.kraftwerk.core.metadata.Variable;
import fr.insee.kraftwerk.core.metadata.VariableType;
import fr.insee.kraftwerk.core.metadata.VariablesMap;
import fr.insee.vtl.model.Structured;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Class to generate import scripts for the csv output tables.
 * Some methods that could be implemented later: R base script and Python with pandas script.
 */
public abstract class ImportScript {

	final List<TableScriptInfo> tableScriptInfoList;
	static final String END_LINE = "\n";

	/** @see TableScriptInfo */
	protected ImportScript(List<TableScriptInfo> tableScriptInfoList) {
		this.tableScriptInfoList = tableScriptInfoList;
	}

	public abstract String generateScript();

	static Map<String, Variable> getAllLength(Structured.DataStructure dataStructure,
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
					result.put(variableName, new Variable(variableName, variablesMap.getGroup(variableName),VariableType.STRING, "32"));
				}
				if (variablesMap.getDistinctVariableNamesAndFullyQualifiedNames().contains(variableName)) {

					variableName = getRootName(variableName);
					Variable variable = variablesMap.getVariable(variableName);

					String newLengthString = variable.getSasFormat();

					// We already got the variable, so we check to see if the lengths are different -> take the maximum one then
					if (newLengthString == null && !variableName.toUpperCase().contains(Constants.FILTER_RESULT_PREFIX)) {
						if (result.containsKey(variableName)) {
							result.replace(variableName, new Variable(variableName,
									result.get(variableName).getGroup(), VariableType.STRING, "255"));
						} else {
							result.put(variableName, new Variable(variableName,
									variablesMap.getGroup(Constants.ROOT_GROUP_NAME), VariableType.STRING, "255"));
						}
					} else {
						if (result.containsKey(variableName)) {
							String existingLengthString = result.get(variableName).getSasFormat();
							if (newLengthString!=null && !newLengthString.contains(".") && !existingLengthString.contains(".")) {
								// Variable already put in result, and not a float (if float exists, we do nothing)
								int newLength = Integer.parseInt(newLengthString);
								if (Integer.parseInt(existingLengthString) < newLength) {
									// name, Group group, VariableType type, int length
									result.replace(variableName,
											new Variable(variableName, result.get(variableName).getGroup(),
													result.get(variableName).getType(), newLengthString));
								}
							}
						} else {
							// Filter results are boolean, value "true" or "false"
							if (variableName.toUpperCase().contains(Constants.FILTER_RESULT_PREFIX)) {
								result.put(variableName, new Variable(variableName,
										variablesMap.getGroup(Constants.ROOT_GROUP_NAME), VariableType.BOOLEAN, "1"));
							} else {
								// new Variable, we keep it like that
								result.put(variableName, new Variable(variableName, variable.getGroup(), variable.getType(),
										variable.getSasFormat()));
							}

						}
					}
				} else {
					if (!result.containsKey(variableName)) {
						result.put(variableName, new Variable(variableName,
								variablesMap.getGroup(Constants.ROOT_GROUP_NAME), VariableType.STRING, "255"));
					}
				}
			}

		}
		return result;
	}

	/** Return the variable name without the group in the prefixes. */
	static String getRootName(String name) {
		return name.substring(name.lastIndexOf('.') + 1);
	}

}
