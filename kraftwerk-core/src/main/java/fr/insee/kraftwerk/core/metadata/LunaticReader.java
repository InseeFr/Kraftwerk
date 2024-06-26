package fr.insee.kraftwerk.core.metadata;

import com.fasterxml.jackson.databind.JsonNode;
import fr.insee.kraftwerk.core.Constants;
import fr.insee.kraftwerk.core.metadata.CalculatedVariables.CalculatedVariable;
import fr.insee.kraftwerk.core.utils.JsonReader;
import fr.insee.kraftwerk.core.utils.files.FileUtilsInterface;
import lombok.extern.log4j.Log4j2;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

import static fr.insee.kraftwerk.core.Constants.FILTER_RESULT_PREFIX;
import static fr.insee.kraftwerk.core.Constants.MISSING_SUFFIX;

@Log4j2
public class LunaticReader {

	private static final String BINDING_DEPENDENCIES = "bindingDependencies";
	private static final String VARIABLES = "variables";
	private static final String EXCEPTION_MESSAGE = "Unable to read Lunatic questionnaire file: ";
	private static final String RESPONSE = "response";
	private static final String COMPONENTS = "components";
	private static final String COMPONENT_TYPE = "componentType";
	private static final String VALUE = "value";
	private static final String LABEL = "label";
	private static final String MISSING_RESPONSE = "missingResponse";
	private static final String LUNATIC_MODEL_VERSION= "lunaticModelVersion";

	private LunaticReader() {
		throw new IllegalStateException("Utility class");
	}

	/**
	 * Read the lunatic questionnaire given to get VTL expression of calculated
	 * variables.
	 *
	 * @param lunaticFile Path to a lunatic questionnaire file.
	 * @return A CalculatedVariables map.
	 */
	public static CalculatedVariables getCalculatedFromLunatic(Path lunaticFile, FileUtilsInterface fileUtilsInterface) {
		try {
			JsonNode rootNode = JsonReader.read(lunaticFile, fileUtilsInterface);
			String lunaticModelVersion = rootNode.get(LUNATIC_MODEL_VERSION).asText();
			boolean isLunaticV2 = compareVersions(lunaticModelVersion, "2.3.0") > 0;

			CalculatedVariables calculatedVariables = new CalculatedVariables();

			JsonNode variablesNode = rootNode.get(VARIABLES);
			variablesNode.forEach(variableNode -> {
				if (variableNode.get("variableType").asText().equals("CALCULATED")) {
					String formula = isLunaticV2 ? variableNode.get("expression").get(VALUE).asText()
							: variableNode.get("expression").asText();
					CalculatedVariable calculatedVariable = new CalculatedVariable(variableNode.get("name").asText(),
							formula);
					JsonNode dependantVariablesNode = variableNode.get(BINDING_DEPENDENCIES);
					if (dependantVariablesNode != null) {
						dependantVariablesNode.forEach(name -> calculatedVariable.addDependantVariable(name.asText()));
					}
					calculatedVariables.putVariable(calculatedVariable);
				}
			});

			return calculatedVariables;

		} catch (IOException e) {
			log.error(EXCEPTION_MESSAGE + lunaticFile);
			return new CalculatedVariables();
		}
	}

	/**
	 * Read the lunatic file to get the names of the _MISSING variables and the
	 * collected variables added by Eno which are not present in the DDI
	 *
	 * @param lunaticFile Path to a lunatic questionnaire file.
	 * @return A List of String.
	 */
	public static List<String> getMissingVariablesFromLunatic(Path lunaticFile, FileUtilsInterface fileUtilsInterface) {
		try {
			JsonNode rootNode = JsonReader.read(lunaticFile, fileUtilsInterface);
			List<String> variables = new ArrayList<>();
			List<String> varsEno = Arrays.asList(Constants.getEnoVariables());

			JsonNode variablesNode = rootNode.get(VARIABLES);
			variablesNode.forEach(variableNode -> variables.add(variableNode.get("name").asText()));
			return variables.stream()
					.filter(varToRead -> varToRead.endsWith(MISSING_SUFFIX) || varsEno.contains(varToRead)).toList();

		} catch (IOException e) {
			log.error(EXCEPTION_MESSAGE + lunaticFile);
			return Collections.emptyList();
		}
	}

	/**
	 * Read the lunatic file to get the names of the FILTER_RESULT variables which
	 * are not present in the DDI
	 *
	 * @param lunaticFile Path to a lunatic questionnaire file.
	 * @return A List of String.
	 */
	public static List<String> getFilterResultFromLunatic(Path lunaticFile, FileUtilsInterface fileUtilsInterface) {
		try {
			JsonNode rootNode = JsonReader.read(lunaticFile, fileUtilsInterface);
			List<String> variables = new ArrayList<>();

			JsonNode variablesNode = rootNode.get(VARIABLES);
			variablesNode.forEach(variableNode -> variables.add(variableNode.get("name").asText()));
			return variables.stream().filter(variable -> variable.startsWith(FILTER_RESULT_PREFIX)).toList();

		} catch (IOException e) {
			log.error(EXCEPTION_MESSAGE + lunaticFile);
			return Collections.emptyList();
		}
	}

	public static String getLunaticModelVersion(Path lunaticFile, FileUtilsInterface fileUtilsInterface){
		try {
			JsonNode rootNode = JsonReader.read(lunaticFile, fileUtilsInterface);
			return rootNode.get(LUNATIC_MODEL_VERSION).toString();

		} catch (IOException e) {
			log.error(EXCEPTION_MESSAGE + lunaticFile);
			return "";
		}
	}

	/**
	 * This method extracts return the variables of a questionnaire without reading
	 * a DDI file. It should be used only when the DDI is not available.
	 *
	 * @param lunaticFile : Path to a Lunatic specification file.
	 * @return The variables found in the Lunatic specification.
	 */
	public static MetadataModel getMetadataFromLunatic(Path lunaticFile, FileUtilsInterface fileUtilsInterface) {
		JsonNode rootNode;
		try {
			rootNode = JsonReader.read(lunaticFile, fileUtilsInterface);
			List<String> variables = new ArrayList<>();
			JsonNode variablesNode = rootNode.get(VARIABLES);
			variablesNode.forEach(newVar -> variables.add(newVar.get("name").asText()));
			// Root group is created in VariablesMap constructor
			MetadataModel metadataModel = new MetadataModel();
			metadataModel.putSpecVersions(SpecType.LUNATIC,rootNode.get(LUNATIC_MODEL_VERSION).asText());
			Group rootGroup = metadataModel.getRootGroup();
			iterateOnComponents(rootNode, variables, metadataModel, rootGroup);

			// We iterate on root components to identify variables belonging to root group
			JsonNode rootComponents = rootNode.get(COMPONENTS);
			for (JsonNode comp : rootComponents) {
				addResponsesAndMissing(comp, rootGroup, variables, metadataModel);
			}
			// We add the remaining (not identified in any loops nor root) variables to the root group
			variables.forEach(
					varName -> metadataModel.getVariables().putVariable(new Variable(varName, rootGroup, VariableType.STRING)));
			return metadataModel;
		} catch (IOException e) {
			log.error(EXCEPTION_MESSAGE + lunaticFile);
			return null;
		}
	}

	/**
	 * This method iterates on an array of components to extract the variables present in loops and get their group.
	 * @param rootNode : node containing the components we want to iterate on
	 * @param variables : variables list to be completed
	 * @param metadataModel : metadata model of the questionnaire to be completed
	 * @param parentGroup : group of rootNode
	 */
	private static void iterateOnComponents(JsonNode rootNode, List<String> variables, MetadataModel metadataModel, Group parentGroup) {
		JsonNode componentsNode = rootNode.get(COMPONENTS);
		if (componentsNode.isArray()) {
			for (JsonNode component : componentsNode) {
				if (component.get(COMPONENT_TYPE).asText().equals("Loop")) {
					if (component.has("lines")) {
						processPrimaryLoop(variables, metadataModel, parentGroup, component);
					}
					if (component.has("iterations")) {
						Group group = processDependingLoop(variables, metadataModel, parentGroup, component);
						iterateOnComponents(component,variables, metadataModel,group);
					}
				}
			}
		}
	}

	/**
	 * This method processes the primary loop and creates a group with the name of the first response
	 * @param variables : variables list to be completed
	 * @param metadataModel : metadata model of the questionnaire to be completed
	 * @param parentGroup : parent group of the loop
	 * @param component : loop component
	 */
	private static void processPrimaryLoop(List<String> variables, MetadataModel metadataModel, Group parentGroup, JsonNode component) {
		JsonNode primaryComponents = component.get(COMPONENTS);
		//We create a group only with the name of the first response
		//Then we add all the variables found in response to the newly created group
		String groupName = getFirstResponseName(primaryComponents);
		log.info("Creation of group :" + groupName);
		Group group = getNewGroup(metadataModel, groupName, parentGroup);
		for (JsonNode primaryComponent : primaryComponents) {
			addResponsesAndMissing(primaryComponent, group, variables, metadataModel);
		}
	}

	/**
	 * This method processes the loop depending on variables and creates a group with the name of loop dependencies
	 * @param variables : variables list to be completed
	 * @param metadataModel : metadata model of the questionnaire to be completed
	 * @param parentGroup : parent group of the loop
	 * @param component : loop component
	 * @return the group corresponding to the loop
	 */
	private static Group processDependingLoop(List<String> variables, MetadataModel metadataModel, Group parentGroup, JsonNode component) {
		JsonNode loopDependencies = component.get("loopDependencies");
		StringBuilder groupNameBuilder = new StringBuilder(loopDependencies.get(0).asText());
		for (int i = 1; i < loopDependencies.size(); i++) {
			groupNameBuilder.append("_").append(loopDependencies.get(i).asText());
		}
		String groupName = groupNameBuilder.toString();
		log.info("Creation of group :" + groupName);
		Group group = getNewGroup(metadataModel, groupName, parentGroup);
		iterateOnComponentsToFindResponses(component, variables, metadataModel, group);
		return group;
	}

	private static String getFirstResponseName(JsonNode components){
		for(JsonNode component : components){
			if (component.has(RESPONSE)){
				return component.get(RESPONSE).get("name").asText();
			}
		}
		return null;
	}

	/**
	 * Adds variables to the metadata model (it infers type of variables from the component type)
	 * Checks Lunatic version to adapt to the different ways of writing the JSON
	 *
	 * @param primaryComponent : a component of the questionnaire
	 * @param group : the group to which the variables belong
	 * @param variables : list of variables to be completed
	 * @param metadataModel : metadata model of the questionnaire to be completed
	 */
	private static void addResponsesAndMissing(JsonNode primaryComponent, Group group, List<String> variables, MetadataModel metadataModel) {
		//We read the name of the collected variables in response(s)
		//And we deduce the variable type by looking at the component that encapsulate the variable
		ComponentLunatic componentType = ComponentLunatic.fromJsonName(primaryComponent.get(COMPONENT_TYPE).asText());
		String variableName;
		boolean isLunaticV2 = compareVersions(metadataModel.getSpecVersions().get(SpecType.LUNATIC), "2.3.0") > 0;
		switch(componentType){
			case ComponentLunatic.DATE_PICKER, ComponentLunatic.CHECKBOX_BOOLEAN, ComponentLunatic.INPUT, ComponentLunatic.TEXT_AREA, ComponentLunatic.SUGGESTER:
				variableName = getVariableName(primaryComponent);
				metadataModel.getVariables().putVariable(new Variable(variableName, group, componentType.getType()));
				variables.remove(variableName);
				break;
			case ComponentLunatic.INPUT_NUMBER:
				variableName = getVariableName(primaryComponent);
				if (primaryComponent.get("decimals").asInt()==0){
					metadataModel.getVariables().putVariable(new Variable(variableName, group, VariableType.INTEGER));
					variables.remove(variableName);
					break;
				}
				metadataModel.getVariables().putVariable(new Variable(variableName, group, VariableType.NUMBER));
				variables.remove(variableName);
				break;
			case ComponentLunatic.DROPDOWN:
				variableName = getVariableName(primaryComponent);
				UcqVariable ucqVar = new UcqVariable(variableName, group, VariableType.STRING);
				JsonNode modalities = primaryComponent.get("options");
				for (JsonNode modality : modalities){
					ucqVar.addModality(modality.get(VALUE).asText(), modality.get(LABEL).asText());
				}
				metadataModel.getVariables().putVariable(ucqVar);
				variables.remove(variableName);
				break;
			case ComponentLunatic.RADIO, ComponentLunatic.CHECKBOX_ONE:
				variableName = getVariableName(primaryComponent);
				UcqVariable ucqVarOne = new UcqVariable(variableName, group, VariableType.STRING);
				JsonNode modalitiesOne = primaryComponent.get("options");
				for (JsonNode modality : modalitiesOne){
					if (isLunaticV2) {
						ucqVarOne.addModality(modality.get(VALUE).asText(), modality.get(LABEL).get(VALUE).asText());
						continue;
					}
					ucqVarOne.addModality(modality.get(VALUE).asText(), modality.get(LABEL).asText());
				}
				metadataModel.getVariables().putVariable(ucqVarOne);
				variables.remove(variableName);
				break;
			case ComponentLunatic.CHECKBOX_GROUP:
				processCheckboxGroup(primaryComponent, group, variables, metadataModel, isLunaticV2);
				break;
			case ComponentLunatic.PAIRWISE_LINKS:
				// In we case of a pairwiseLinks component we have to iterate on the components to find the responses
				// It is a nested component, but we treat it differently than the loops because it does not create a new level of information
				iterateOnComponentsToFindResponses(primaryComponent, variables, metadataModel, group);
				break;
			case ComponentLunatic.TABLE:
				iterateOnTableBody(primaryComponent, group, variables, metadataModel, isLunaticV2);
				break;
			case null:
				break;
		}
		//We also had the missing variable if it exists (only one missing variable even if multiple responses)
		addMissingVariable(primaryComponent, group, variables, metadataModel);
	}

	/**
	 * Process a checkbox group to create a boolean variable for each response
	 * @param checkboxComponent : component representing a checkbox group
	 * @param group : group to which the variables belong
	 * @param variables : list of variables to be completed
	 * @param metadataModel : metadata model of the questionnaire to be completed
	 * @param isLunaticV2 : true if the Lunatic version is 2.3 or higher
	 */
	private static void processCheckboxGroup(JsonNode checkboxComponent, Group group, List<String> variables, MetadataModel metadataModel, boolean isLunaticV2) {
		String variableName;
		JsonNode responses = checkboxComponent.get("responses");
		List<String> responsesName= new ArrayList<>();
		for (JsonNode response : responses){
			responsesName.add(getVariableName(response));
		}
		String questionName = findLongestCommonPrefix(responsesName);
		for (JsonNode response : responses){
			variableName = getVariableName(response);
			McqVariable mcqVariable = new McqVariable(variableName, group, VariableType.BOOLEAN);
			if (isLunaticV2) mcqVariable.setText(response.get(LABEL).get(VALUE).asText());
			if (!isLunaticV2) mcqVariable.setText(response.get(LABEL).asText());
			mcqVariable.setInQuestionGrid(true);
			mcqVariable.setQuestionItemName(questionName);
			metadataModel.getVariables().putVariable(mcqVariable);
			variables.remove(variableName);
		}
	}

	/**
	 * Iterate on the components in the body of a table to find the responses
	 * @param tableComponent : component representing a table
	 * @param group : group to which the variables belong
	 * @param variables : list of variables to be completed
	 * @param metadataModel : metadata model of the questionnaire to be completed
	 * @param isLunaticV2 : true if the Lunatic version is 2.3 or higher
	 */
	private static void iterateOnTableBody(JsonNode tableComponent, Group group, List<String> variables, MetadataModel metadataModel, boolean isLunaticV2) {
		// In we case of a table component we have to iterate on the body components to find the responses
		// The body is a nested array of arrays
		// In Lunatic 2.2 and lower the body is called cells
		JsonNode body = isLunaticV2 ? tableComponent.get("body") : tableComponent.get("cells");
		for(JsonNode arr : body){
			if (arr.isArray()){
				for (JsonNode cell : arr){
					if (cell.has(COMPONENT_TYPE)) {
						addResponsesAndMissing(cell, group, variables, metadataModel);
					}
				}
			}
		}
	}

	/**
	 * Add the missing variable defined in the component if present
	 * @param component : a questionnaire component
	 * @param group : group to which the variables belong
	 * @param variables : list of variables to be completed
	 * @param metadataModel : metadata model of the questionnaire to be completed
	 */
	private static void addMissingVariable(JsonNode component, Group group, List<String> variables, MetadataModel metadataModel) {
		if (component.has(MISSING_RESPONSE)){
			String missingVariable = component.get(MISSING_RESPONSE).get("name").asText();
			metadataModel.getVariables().putVariable(new Variable(missingVariable, group, VariableType.STRING));
			variables.remove(missingVariable);
		}
	}

	/**
	 * Get the name of the variable collected by a component
	 * @param component : a questionnaire component
	 * @return the name of the variable
	 */
	private static String getVariableName(JsonNode component) {
		return component.get(RESPONSE).get("name").asText();
	}

	private static void iterateOnComponentsToFindResponses(JsonNode node, List<String> variables, MetadataModel metadataModel, Group group) {
		JsonNode components = node.get(COMPONENTS);
		if (components.isArray()){
			for (JsonNode component : components) {
				addResponsesAndMissing(component, group, variables, metadataModel);
			}
		}
	}

	private static Group getNewGroup(MetadataModel metadataModel, String newName, Group parentGroup) {
		Group group = new Group(String.format("%s_%s",Constants.LOOP_NAME_PREFIX,newName), parentGroup.getName());
		metadataModel.putGroup(group);
		return group;
	}

	/**
	 * Read the lunatic file and returns a String containing the questionnaire model
	 * id
	 *
	 * @param lunaticFile : Path to a Lunatic specification file.
	 * @return the questionnaire model id
	 */
	public static String getQuestionnaireModelId(Path lunaticFile, FileUtilsInterface fileUtilsInterface) {
		JsonNode rootNode;
		try {
			rootNode = JsonReader.read(lunaticFile, fileUtilsInterface);
			return rootNode.get("id").asText();
		} catch (IOException e) {
			log.error(EXCEPTION_MESSAGE + lunaticFile);
			return null;
		}
	}

	/**
	 * Find the common part of a list of strings that differs only at the end
	 *
	 * @param similarStrings : list of strings
	 * @return the common prefix
	 */
	public static String findLongestCommonPrefix(List<String> similarStrings) {
		int minLength = similarStrings.getFirst().length();
		for(String str : similarStrings){
			if (str.length()<minLength){
				minLength = str.length();
			}
		}
		String commonPrefix="";
		for(int i=1;i<minLength;i++){
			boolean isCommon=true;
			String stringToTest = similarStrings.getFirst().substring(0,i);
			for (String str : similarStrings){
				if (!str.startsWith(stringToTest)){
					isCommon=false;
					break;
				}
			}
			if (isCommon){
				commonPrefix = stringToTest;
			} else {
				break;
			}
		}

		return commonPrefix;
	}

	/**
	 * Compare two versions of the form x.y.z
	 *
	 * @param version1 : version of the form x.y.z
	 * @param version2 : version of the form x.y.z
	 * @return 1 if version1 is greater, 0 if they are equal, -1 if version2 is greater.
	 */
	public static int compareVersions(String version1, String version2) {
		int comparisonResult = 0;

		String[] version1Splits = version1.split("\\.");
		String[] version2Splits = version2.split("\\.");
		int maxLengthOfVersionSplits = Math.max(version1Splits.length, version2Splits.length);

		for (int i = 0; i < maxLengthOfVersionSplits; i++){
			Integer v1 = i < version1Splits.length ? Integer.parseInt(version1Splits[i]) : 0;
			Integer v2 = i < version2Splits.length ? Integer.parseInt(version2Splits[i]) : 0;
			int compare = v1.compareTo(v2);
			if (compare != 0) {
				comparisonResult = compare;
				break;
			}
		}
		return comparisonResult;
	}

}
