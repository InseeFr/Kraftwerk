package fr.insee.kraftwerk.core.outputs;

import java.util.LinkedHashMap;
import java.util.Map;

import fr.insee.kraftwerk.core.Constants;
import fr.insee.kraftwerk.core.metadata.UcqVariable;
import fr.insee.kraftwerk.core.metadata.Variable;
import fr.insee.kraftwerk.core.metadata.VariableType;
import fr.insee.kraftwerk.core.metadata.VariablesMap;
import fr.insee.vtl.model.Structured.DataStructure;
import lombok.AllArgsConstructor;
import lombok.Getter;

/** POJO class to store information needed to write a script for a CSV table. */
@AllArgsConstructor
public class TableScriptInfo {

	/** The name of the dataset in the destination language. */
	@Getter
	String tableName;
	/** The CSV file name of the table. */
	@Getter
	String csvFileName;
	/** The data structure (containing variable names and types) of the table. */
	@Getter
	DataStructure dataStructure;

	/** The data structure (containing variable length) of the table. */
	@Getter
	Map<String, VariablesMap> metadataVariables;

	public Map<String, Variable> getAllLength(DataStructure dataStructure,
			Map<String, VariablesMap> metadataVariables) {
		Map<String, Variable> result = new LinkedHashMap<String, Variable>();
		// datastructure : noms complets
		// metadata : suffixe du nom
		// We loop with all variables in the current dataset we want to export
		for (String variableName : dataStructure.keySet()) {
			// We try to find it from the first datasets containing together all variables
			// (except VTL and Kraftwerk-created ones)

			for (String datasetName : metadataVariables.keySet()) {
				VariablesMap variablesMap = metadataVariables.get(datasetName);

				// We treat the identifiers

				if (variablesMap.getIdentifierNames().contains(variableName)) {
					Variable idGroupVariable = new Variable(variableName, variablesMap.getGroup(variableName),
							VariableType.STRING, "32");
					if (!result.containsKey(variableName)) {
						result.put(variableName, idGroupVariable);
					}
				}
				if (variablesMap.getFullyQualifiedNames().contains(variableName)
						|| variablesMap.getVariableNames().contains(variableName)) {
						Variable variable = variablesMap.getVariable(getRootName(variableName));
						variableName = getRootName(variableName);
						String newLengthString = variable.getLength();
						// We already got the variable, so we check to see if the lengthes are different -> take the maximum one then
					if (newLengthString == null && !variableName.toUpperCase().contains(Constants.FILTER_RESULT_PREFIX)) {
					} else {
						if (result.containsKey(variableName)) {
							String existingLengthString = result.get(variableName).getLength();
							if (!newLengthString.contains(".") && !existingLengthString.contains(".")) {
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
									variable.getLength()));
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
	public String getRootName(String name) {
		return name.substring(name.lastIndexOf('.') + 1);
	}

}
