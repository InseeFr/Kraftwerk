package fr.insee.kraftwerk.core.dataprocessing;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import fr.insee.kraftwerk.core.Constants;
import fr.insee.kraftwerk.core.metadata.VariablesMap;
import fr.insee.kraftwerk.core.vtl.VtlBindings;
import fr.insee.kraftwerk.core.vtl.VtlMacros;
import fr.insee.kraftwerk.core.vtl.VtlScript;

/**
 * This processing class is designed to create one dataset per group existing in metadata.
 */
public class InformationLevelsProcessing extends DataProcessing {

    public InformationLevelsProcessing(VtlBindings vtlBindings) {
        super(vtlBindings);
    }

    @Override
    public String getStepName() {
        return "INFORMATION LEVELS";
    }

	/**
	 * The binding name is the multimode dataset.
	 * The method generates VTL instructions to create one dataset per group of variables.
	 *
	 * NOTE: for now, only works with at most one level of group under root group.
	 */
    @Override
    protected VtlScript generateVtlInstructions(String bindingName) {

    	VtlScript vtlScript = new VtlScript();
		
		VariablesMap multimodeVariablesMap = vtlBindings.getDatasetVariablesMap(bindingName);

		// Root dataset
		StringBuilder rootInstructions = new StringBuilder();

		String rootMeasures = VtlMacros.toVtlSyntax(multimodeVariablesMap.getGroupVariableNames(Constants.ROOT_GROUP_NAME));
		rootInstructions.append(String.format("%s := %s [keep %s, %s, %s];",
				Constants.ROOT_GROUP_NAME, bindingName, Constants.ROOT_IDENTIFIER_NAME, rootMeasures, Constants.MODE_VARIABLE_NAME));

		vtlScript.add(rootInstructions.toString());

		// To delete duplicates, to be eventually reviewed with a better VTL solution
		vtlScript.add(Constants.ROOT_GROUP_NAME + " := union(" + Constants.ROOT_GROUP_NAME + ", " + Constants.ROOT_GROUP_NAME +");");

		// TODO Retirer les ATTEMPTS du root ?
		 
		// Group datasets
		for (String groupName : multimodeVariablesMap.getSubGroupNames()) {
			StringBuilder groupInstructions = new StringBuilder();

			// First init the dataset using measure names, that are fully qualified name
			List<String> groupVariableNames = new ArrayList<>(multimodeVariablesMap.getGroupVariableNames(groupName));
			List<String> groupMeasureNames = groupVariableNames.stream()
					.map(multimodeVariablesMap::getFullyQualifiedName).collect(Collectors.toList());

			String groupMeasures = VtlMacros.toVtlSyntax(groupMeasureNames);
			groupInstructions.append(String.format("%s := %s [keep %s, %s, %s, %s];",
					groupName, bindingName, Constants.ROOT_IDENTIFIER_NAME, groupName, groupMeasures, Constants.MODE_VARIABLE_NAME));
			// Epmpty lines are created to produce group level tables and need to be removed
			groupInstructions.append(String.format("%s := %s [filter %s<>\"\"];",
					groupName, groupName, groupName));


			vtlScript.add(groupInstructions.toString());

			// To delete duplicates
			vtlScript.add(String.format("%1$s := union(%1$s,%1$s);",groupName));

			// Then rename fully qualified names into simple names
			for (int j=0; j< groupVariableNames.size(); j++) {
				String variableName = groupVariableNames.get(j);
				String fullyQualifiedName = groupMeasureNames.get(j);
				vtlScript.add(String.format("%s := %s [rename %s to %s];",
						groupName, groupName, fullyQualifiedName, variableName));
			}
		}

		return vtlScript;
    }

}
