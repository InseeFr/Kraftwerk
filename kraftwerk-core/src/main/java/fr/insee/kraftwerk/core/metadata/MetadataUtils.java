package fr.insee.kraftwerk.core.metadata;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import fr.insee.kraftwerk.core.Constants;
import fr.insee.kraftwerk.core.inputs.ModeInputs;
import fr.insee.kraftwerk.core.inputs.UserInputs;

public class MetadataUtils {

	
	public static VariablesMap getMetadata(UserInputs userInputs, String dataMode){
		Map<String, VariablesMap> metadataVariables = getMetadata(userInputs.getModeInputsMap());
		return metadataVariables.get(dataMode);
	}

	
	public static Map<String, VariablesMap> getMetadata(Map<String, ModeInputs> modeInputsMap){
		Map<String, VariablesMap> metadataVariables = new LinkedHashMap<>();
		modeInputsMap.forEach((k, v) -> putToMetadataVariable(k,v,metadataVariables));
		return metadataVariables;
	}
	
	

	private static void putToMetadataVariable(String dataMode, ModeInputs modeInputs, Map<String, VariablesMap> metadataVariables ) {
		// Step 1 : we add the variables read in the DDI
		VariablesMap variables = DDIReader.getVariablesFromDDI(modeInputs.getDdiUrl());
		// Step 2 : we add the "_MISSING" variables from lunatic file if they exist
		if (modeInputs.getLunaticFile() != null) {
			List<String> missingVars = LunaticReader.getMissingVariablesFromLunatic(modeInputs.getLunaticFile());
			for (String missingVar : missingVars) {
				String correspondingVariableName = missingVar.replace(Constants.MISSING_SUFFIX, "");
				Group group;
				if (variables.hasVariable(correspondingVariableName)) {
					group = variables.getVariable(correspondingVariableName).getGroup();
				} else {
					group = variables.getGroup(variables.getGroupNames().get(0));
					// No information from the DDI about variable
					// It has been arbitrarily associated with the root group
				}
				variables.putVariable(new Variable(missingVar, group, VariableType.STRING));
			}
		}
		metadataVariables.put(dataMode, variables);
	}
}
