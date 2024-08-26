package fr.insee.kraftwerk.core.metadata;

import fr.insee.bpm.exceptions.MetadataParserException;
import fr.insee.bpm.metadata.model.Group;
import fr.insee.bpm.metadata.model.MetadataModel;
import fr.insee.bpm.metadata.model.Variable;
import fr.insee.bpm.metadata.model.VariableType;
import fr.insee.bpm.metadata.reader.ddi.DDIReader;
import fr.insee.bpm.metadata.reader.lunatic.LunaticReader;

import fr.insee.kraftwerk.core.Constants;
import fr.insee.kraftwerk.core.inputs.ModeInputs;
import fr.insee.kraftwerk.core.utils.files.FileUtilsInterface;
import lombok.extern.log4j.Log4j2;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Log4j2
public class MetadataUtilsGenesis {

	private MetadataUtilsGenesis() {
		throw new IllegalStateException("Utility class");
	}

	public static Map<String, MetadataModel> getMetadata(Map<String, ModeInputs> modeInputsMap, FileUtilsInterface fileUtilsInterface) throws MetadataParserException {
		Map<String, MetadataModel> metadataModels = new LinkedHashMap<>();
		for (Map.Entry<String, ModeInputs> entry : modeInputsMap.entrySet()) {
			String k = entry.getKey();
			ModeInputs v = entry.getValue();
			putToMetadataVariable(k, v, metadataModels, fileUtilsInterface);
		}
		return metadataModels;
	}

	private static void putToMetadataVariable(String dataMode, ModeInputs modeInputsGenesis, Map<String, MetadataModel> metadataModels, FileUtilsInterface fileUtilsInterface) throws MetadataParserException {
		// Step 1 : we add the variables read in the DDI
		MetadataModel metadataModel = DDIReader.getMetadataFromDDI(modeInputsGenesis.getDdiUrl(), fileUtilsInterface.readFile(modeInputsGenesis.getDdiUrl()));

		// Step 2 : we add the variables that are only present in the Lunatic file
		if (modeInputsGenesis.getLunaticFile() != null) {
			// First we add the collected _MISSING variables
			List<String> missingVars = LunaticReader.getMissingVariablesFromLunatic(fileUtilsInterface.readFile(modeInputsGenesis.getLunaticFile().toString()));
			for (String missingVar : missingVars) {
				addLunaticVariable(metadataModel, missingVar, Constants.MISSING_SUFFIX, VariableType.STRING);
			}
			// Then we add calculated FILTER_RESULT_ variables
			List<String> filterResults = LunaticReader.getFilterResultFromLunatic(fileUtilsInterface.readFile(modeInputsGenesis.getLunaticFile().toString()));
			for (String filterResult : filterResults) {
				addLunaticVariable(metadataModel, filterResult, Constants.FILTER_RESULT_PREFIX, VariableType.BOOLEAN);
			}
		}
		metadataModels.put(dataMode, metadataModel);
	}

	public static void addLunaticVariable(MetadataModel metadataModel, String missingVar, String prefixOrSuffix, VariableType varType) {
		String correspondingVariableName = missingVar.replace(prefixOrSuffix, "");
		Group group;
		if (metadataModel.getVariables().hasVariable(correspondingVariableName)) { // the variable is directly found
			group = metadataModel.getVariables().getVariable(correspondingVariableName).getGroup();
		} else if (metadataModel.getVariables().isInQuestionGrid(correspondingVariableName)) { // otherwise, it should be from a question grid
			group = metadataModel.getVariables().getQuestionGridGroup(correspondingVariableName);
		} else {
			group = metadataModel.getGroup(metadataModel.getGroupNames().getFirst());
			log.warn(String.format(
					"No information from the DDI about question named \"%s\".",
					correspondingVariableName));
			log.warn(String.format(
					"\"%s\" has been arbitrarily associated with group \"%s\".",
					missingVar, group.getName()));
		}
		metadataModel.getVariables().putVariable(new Variable(missingVar, group, varType));
	}

	public static Map<String, MetadataModel> getMetadataFromLunatic(Map<String, ModeInputs> modeInputsMap, FileUtilsInterface fileUtilsInterface) {
		Map<String, MetadataModel> metadataModels = new LinkedHashMap<>();
		modeInputsMap.forEach((k, v) -> putToMetadataVariableFromLunatic(k,v,metadataModels, fileUtilsInterface));
		return metadataModels;
	}

	private static void putToMetadataVariableFromLunatic(String dataMode, ModeInputs modeInputs, Map<String, MetadataModel> metadataModels, FileUtilsInterface fileUtilsInterface) {
		MetadataModel metadataModel = LunaticReader.getMetadataFromLunatic(fileUtilsInterface.readFile(modeInputs.getLunaticFile().toString()));
		metadataModels.put(dataMode, metadataModel);
	}
}
