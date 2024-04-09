package fr.insee.kraftwerk.core.metadata;

import com.fasterxml.jackson.databind.JsonNode;
import fr.insee.kraftwerk.core.Constants;
import fr.insee.kraftwerk.core.metadata.CalculatedVariables.CalculatedVariable;
import fr.insee.kraftwerk.core.utils.JsonFileReader;
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
	public static CalculatedVariables getCalculatedFromLunatic(Path lunaticFile) {
		try {
			JsonNode rootNode = JsonFileReader.read(lunaticFile);

			String lunaticModelVersion = rootNode.get("lunaticModelVersion").toString();
			boolean isLunaticV2 = JsonFileReader.compareVersions(lunaticModelVersion.replace("\"", ""), "2.3.0") > 0;

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
	public static List<String> getMissingVariablesFromLunatic(Path lunaticFile) {
		try {
			JsonNode rootNode = JsonFileReader.read(lunaticFile);
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
	public static List<String> getFilterResultFromLunatic(Path lunaticFile) {
		try {
			JsonNode rootNode = JsonFileReader.read(lunaticFile);
			List<String> variables = new ArrayList<>();

			JsonNode variablesNode = rootNode.get(VARIABLES);
			variablesNode.forEach(variableNode -> variables.add(variableNode.get("name").asText()));
			return variables.stream().filter(variable -> variable.startsWith(FILTER_RESULT_PREFIX)).toList();

		} catch (IOException e) {
			log.error(EXCEPTION_MESSAGE + lunaticFile);
			return Collections.emptyList();
		}
	}

	/**
	 * This method extracts return the variables of a questionnaire without reading
	 * a DDI file. It should be used only when the DDI is not available.
	 *
	 * @param lunaticFile : Path to a Lunatic specification file.
	 * @return The variables found in the Lunatic specification.
	 */
	public static MetadataModel getMetadataFromLunatic(Path lunaticFile) {
		JsonNode rootNode;
		try {
			rootNode = JsonFileReader.read(lunaticFile);
			List<String> variables = new ArrayList<>();
			JsonNode variablesNode = rootNode.get(VARIABLES);
			variablesNode.forEach(newVar -> variables.add(newVar.get("name").asText()));
			// Root group is created in VariablesMap constructor
			MetadataModel metadataModel = new MetadataModel();
			Group rootGroup = metadataModel.getRootGroup();
			iterateOnComponents(rootNode, variables, metadataModel, rootGroup);

			//Case of FILTER_RESULT
			List<String> filterResultsVariables = variables.stream().filter(variable -> variable.startsWith(FILTER_RESULT_PREFIX)).toList();
			filterResultsVariables.forEach(filterVar -> {

			});

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
	 * @param rootNode
	 * @param variables
	 * @param metadataModel
	 * @param parentGroup
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
	 * @param variables
	 * @param metadataModel
	 * @param parentGroup
	 * @param component
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
	 * @param variables
	 * @param metadataModel
	 * @param parentGroup
	 * @param component
	 * @return
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
	 * Compatible with Lunatic v3+
	 *
	 * @param primaryComponent
	 * @param group
	 * @param variables
	 * @param metadataModel
	 */
	private static void addResponsesAndMissing(JsonNode primaryComponent, Group group, List<String> variables, MetadataModel metadataModel) {
		//We read the name of the collected variables in response(s)
		//And we deduce the variable type by looking at the component that encapsulate the variable
		ComponentLunatic componentType = ComponentLunatic.fromJsonName(primaryComponent.get(COMPONENT_TYPE).asText());
		String variableName="";
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
					ucqVarOne.addModality(modality.get(VALUE).asText(), modality.get(LABEL).get(VALUE).asText());
				}
				metadataModel.getVariables().putVariable(ucqVarOne);
				variables.remove(variableName);
				break;
			case ComponentLunatic.CHECKBOX_GROUP:
				JsonNode responses = primaryComponent.get("responses");
				List<String> responsesName= new ArrayList<>();
				for (JsonNode response : responses){
					responsesName.add(getVariableName(response));
				}
				String questionName = findLongestCommonPrefix(responsesName);
				for (JsonNode response : responses){
					variableName = getVariableName(response);
					McqVariable mcqVariable = new McqVariable(variableName, group, VariableType.BOOLEAN);
					mcqVariable.setText(response.get(LABEL).get(VALUE).asText());
					mcqVariable.setInQuestionGrid(true);
					mcqVariable.setQuestionItemName(questionName);
					metadataModel.getVariables().putVariable(mcqVariable);
					variables.remove(variableName);
				}
				break;
			case ComponentLunatic.PAIRWISE_LINKS:
				// In we case of a pairwiseLinks component we have to iterate on the components to find the responses
				// It is a nested component but we treat differently than the loops because it does not create a new level of information
				iterateOnComponentsToFindResponses(primaryComponent, variables, metadataModel, group);
				break;
			case ComponentLunatic.TABLE:
				iterateOnTableBody(primaryComponent, group, variables, metadataModel);
				break;
			case null:
				break;
		}
		//We also had the missing variable if it exists (only one missing variable even if multiple responses)
		addMissingVariable(primaryComponent, group, variables, metadataModel);
	}

	private static void iterateOnTableBody(JsonNode primaryComponent, Group group, List<String> variables, MetadataModel metadataModel) {
		// In we case of a table component we have to iterate on the body components to find the responses
		// The body is a nested array of arrays
		JsonNode body = primaryComponent.get("body");
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

	private static void addMissingVariable(JsonNode primaryComponent, Group group, List<String> variables, MetadataModel metadataModel) {
		if (primaryComponent.has(MISSING_RESPONSE)){
			String missingVariable = primaryComponent.get(MISSING_RESPONSE).get("name").asText();
			metadataModel.getVariables().putVariable(new Variable(missingVariable, group, VariableType.STRING));
			variables.remove(missingVariable);
		}
	}

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
	public static String getQuestionnaireModelId(Path lunaticFile) {
		JsonNode rootNode;
		try {
			rootNode = JsonFileReader.read(lunaticFile);
			return rootNode.get("id").asText();
		} catch (IOException e) {
			log.error(EXCEPTION_MESSAGE + lunaticFile);
			return null;
		}
	}

	public static String findLongestCommonPrefix(List<String> strs) {
		int minLength = strs.getFirst().length();
		for(String str : strs){
			if (str.length()<minLength){
				minLength = str.length();
			}
		}
		String result="";
		for(int i=1;i<minLength;i++){
			boolean isCommon=true;
			String stringToTest = strs.getFirst().substring(0,i);
			for (String str : strs){
				if (!str.startsWith(stringToTest)){
					isCommon=false;
					break;
				}
			}
			if (isCommon){
				result = stringToTest;
			} else {
				break;
			}
		}

		return result;
	}

}
