package fr.insee.kraftwerk.core.outputs;

import java.util.LinkedHashMap;
import java.util.Map;

import fr.insee.kraftwerk.core.Constants;
import fr.insee.kraftwerk.core.metadata.Group;
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

	public Map<String, Variable> getAllLength(DataStructure dataStructure, Map<String, VariablesMap> metadataVariables) {
		System.out.println(tableName);
		System.out.println("tableName");
		System.out.println(dataStructure.keySet());
		Map<String, Variable> result = new LinkedHashMap<String, Variable>();

		for (String variableName : dataStructure.keySet()) {

		for (String datasetName : metadataVariables.keySet()) {
			
				if (metadataVariables.get(datasetName).hasVariable(variableName) && !variableName.contains(Constants.FILTER_RESULT_PREFIX)) {

					int newLength = metadataVariables.get(datasetName).getVariable(variableName).getLength();
					if (result.containsKey(variableName)) {
						int existingLength = result.get(variableName).getLength();
						if (existingLength < newLength) {

							// name, Group group, VariableType type, int length
						}
						result.replace(variableName, new Variable(variableName, result.get(variableName).getGroup(),
								result.get(variableName).getType(), newLength));
					} else {
						Variable variable = metadataVariables.get(datasetName).getVariable(variableName);
						result.put(variableName, new Variable(variableName, variable.getGroup(), variable.getType(),
								variable.getLength()));
					}
				}
			}
		}

		return result;

	}

}
