package fr.insee.kraftwerk.core.metadata;

import fr.insee.bpm.exceptions.MetadataParserException;
import fr.insee.bpm.metadata.model.Group;
import fr.insee.bpm.metadata.model.MetadataModel;

import fr.insee.bpm.metadata.model.SpecType;
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
public class MetadataUtils {

	private MetadataUtils(){
		throw new IllegalStateException("Utility class");
	}

	public static Map<String, MetadataModel> getMetadata(Map<String, ModeInputs> modeInputsMap, FileUtilsInterface fileUtilsInterface){
		Map<String, MetadataModel> metadataModels = new LinkedHashMap<>();
		modeInputsMap.forEach((k, v) -> putToMetadataModels(k,v,metadataModels, fileUtilsInterface));
		return metadataModels;
	}

	private static void putToMetadataModels(String dataMode, ModeInputs modeInputs, Map<String, MetadataModel> metadataModels, FileUtilsInterface fileUtilsInterface) {
		// Step 1 : we add the variables read in the DDI
		MetadataModel metadataModel = new MetadataModel();
		try {
			metadataModel = DDIReader.getMetadataFromDDI(modeInputs.getDdiUrl(), fileUtilsInterface.readFile(modeInputs.getDdiUrl()));
		} catch (MetadataParserException e) {
			log.error(e.getMessage());
		}
		// Step 2 : we add the variables that are only present in the Lunatic file
		if (modeInputs.getLunaticFile() != null) {
			log.info("Adding variables from Lunatic file : {}", modeInputs.getLunaticFile().getFileName());
			// We read and store lunaticModelVersion
            metadataModel.putSpecVersions(SpecType.LUNATIC,LunaticReader.getLunaticModelVersion(fileUtilsInterface.readFile(modeInputs.getLunaticFile().toString())));
			// First we add the collected _MISSING variables
			List<String> missingVars = LunaticReader.getMissingVariablesFromLunatic(fileUtilsInterface.readFile(modeInputs.getLunaticFile().toString()));
			for (String missingVar : missingVars) {
				addLunaticVariable(metadataModel, missingVar, Constants.MISSING_SUFFIX, VariableType.STRING);
			}
			// Then we add calculated FILTER_RESULT_ variables
			List<String> filterResults = LunaticReader.getFilterResultFromLunatic(fileUtilsInterface.readFile(modeInputs.getLunaticFile().toString()));
			for (String filterResult : filterResults) {
				addLunaticVariable(metadataModel, filterResult, Constants.FILTER_RESULT_PREFIX, VariableType.BOOLEAN);
			}
		}
		if (metadataModel.getVariables().getVariable(Constants.LIENS) != null) {
			for (int k=1;k<Constants.MAX_LINKS_ALLOWED;k++) {
				Variable varLien = new Variable(Constants.LIEN+k, metadataModel.getGroup(Constants.BOUCLE_PRENOMS), VariableType.INTEGER);
				metadataModel.getVariables().putVariable(varLien);
			}
		}
		// Step 3 : we add reporting data group if there is any reporting data
		if(modeInputs.getReportingDataFile() != null){
			metadataModel.getGroups().put(Constants.REPORTING_DATA_GROUP_NAME, new Group(Constants.REPORTING_DATA_GROUP_NAME));
		}
		metadataModels.put(dataMode, metadataModel);
	}

	public static void addLunaticVariable(MetadataModel metadata, String missingVar, String prefixOrSuffix, VariableType varType) {
		String correspondingVariableName = missingVar.replace(prefixOrSuffix, "");
		Group group;
		if (metadata.getVariables().hasVariable(correspondingVariableName)) { // the variable is directly found
			group = metadata.getVariables().getVariable(correspondingVariableName).getGroup();
		} else if (metadata.getVariables().isInQuestionGrid(correspondingVariableName)) { // otherwise, it should be from a question grid
			group = metadata.getVariables().getQuestionGridGroup(correspondingVariableName);
		} else {
			group = metadata.getGroup(metadata.getGroupNames().getFirst());
			log.warn("No information from the DDI about question named {}. It has been arbitrarily associated with group {}.", correspondingVariableName, group.getName());
		}
		metadata.getVariables().putVariable(new Variable(missingVar, group, varType));
	}

	public static Map<String, MetadataModel> getMetadataFromLunatic(Map<String, ModeInputs> modeInputsMap, FileUtilsInterface fileUtilsInterface) {
		Map<String, MetadataModel> metadataModels = new LinkedHashMap<>();
		modeInputsMap.forEach((k, v) -> putToMetadataVariableFromLunatic(k,v,metadataModels, fileUtilsInterface));
		return metadataModels;
	}

	private static void putToMetadataVariableFromLunatic(String dataMode, ModeInputs modeInputs, Map<String, MetadataModel> metadataModels, FileUtilsInterface fileUtilsInterface) {
		MetadataModel metadataModel = LunaticReader.getMetadataFromLunatic(fileUtilsInterface.readFile(modeInputs.getLunaticFile().toString()));
		// We add the variables for pairwise links
		if (metadataModel.getVariables().getVariable(Constants.LIENS) != null) {
			// We identify the group containing the individuals
			// The solution is not pretty (hoping that the group name contains "PRENOM")
			// It is meant to be temporary until we have a better way to identify the group containing the individuals
			String groupContainingIndividuals = metadataModel.getGroupNames().stream()
					.filter(g -> g.contains("PRENOM"))
					.findFirst()
					.orElse(metadataModel.getGroupNames().getFirst());
			for (int k=1;k<Constants.MAX_LINKS_ALLOWED;k++) {
				Variable varLien = new Variable(Constants.LIEN+k, metadataModel.getGroup(groupContainingIndividuals), VariableType.INTEGER);
				metadataModel.getVariables().putVariable(varLien);
			}
		}
		metadataModels.put(dataMode, metadataModel);
	}
}
