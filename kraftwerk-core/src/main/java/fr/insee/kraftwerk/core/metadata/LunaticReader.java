package fr.insee.kraftwerk.core.metadata;

import com.fasterxml.jackson.databind.JsonNode;
import fr.insee.kraftwerk.core.Constants;
import fr.insee.kraftwerk.core.metadata.CalculatedVariables.CalculatedVariable;
import fr.insee.kraftwerk.core.utils.JsonFileReader;
import lombok.extern.log4j.Log4j2;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static fr.insee.kraftwerk.core.Constants.FILTER_RESULT_PREFIX;
import static fr.insee.kraftwerk.core.Constants.MISSING_SUFFIX;

@Log4j2
public class LunaticReader {

	private static final String BINDING_DEPENDENCIES = "bindingDependencies";
	private static final String VARIABLES = "variables";


	private static final String EXCEPTION_MESSAGE = "Unable to read Lunatic questionnaire file: ";

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
					String formula = isLunaticV2 ? variableNode.get("expression").get("value").asText()
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
			JsonNode componentsNode = rootNode.get("components");
			// Root group is created in VariablesMap constructor
			MetadataModel metadataModel = new MetadataModel();
			if (componentsNode.isArray()) {
				int i = 1;
				for (JsonNode component : componentsNode) {
					if (component.get("componentType").asText().equals("Loop")) {
						// No imbricated loops so the parent is the root group
						Group group = getNewGroup(metadataModel, i);
						i++;
						iterateOnBindingsDependencies(variables, metadataModel.getVariables(), component, group);
						iterateOnComponents(rootNode, variables, metadataModel, group);
					}
				}
			}

			// We get the root group
			Group rootGroup = metadataModel.getGroup(metadataModel.getGroupNames().getFirst());
			variables.forEach(
					varName -> metadataModel.getVariables().putVariable(new Variable(varName, rootGroup, VariableType.STRING)));
			return metadataModel;
		} catch (IOException e) {
			log.error(EXCEPTION_MESSAGE + lunaticFile);
			return null;
		}
	}

	private static Group getNewGroup(MetadataModel metadataModel, int i) {
		Group group = new Group(String.format("BOUCLE%d", i), Constants.ROOT_GROUP_NAME);
		metadataModel.putGroup(group);
		return group;
	}

	private static Group getNewGroup(MetadataModel metadataModel, String newName) {
		Group group = new Group(newName, Constants.ROOT_GROUP_NAME);
		metadataModel.putGroup(group);
		return group;
	}

	private static void iterateOnComponents(JsonNode rootNode, List<String> variables, MetadataModel metadataModel,
											Group group) {
		JsonNode loopComponentsNode = rootNode.get("components");
		if (loopComponentsNode.isArray()) {
			for (JsonNode componentInLoop : loopComponentsNode) {
				if (componentInLoop.get("componentType").asText().equals("Loop")) {
					Group subgroup = getNewGroup(metadataModel, group.getName().concat(componentInLoop.asText()));
					iterateOnBindingsDependencies(variables, metadataModel.getVariables(), componentInLoop, subgroup);
					iterateOnComponents(componentInLoop, variables, metadataModel, subgroup);
				} else {
					iterateOnBindingsDependencies(variables, metadataModel.getVariables(), componentInLoop, group);
				}
			}
		}
	}


	private static void iterateOnBindingsDependencies(List<String> variables, VariablesMap variablesMap,
													  JsonNode component, Group group) {
		if (component.has(BINDING_DEPENDENCIES)) {
			JsonNode loopVariables = component.get(BINDING_DEPENDENCIES);
			loopVariables.forEach(variable -> {
				if (variables.contains(variable.asText())) {
					variablesMap.putVariable(new Variable(variable.asText(), group, VariableType.STRING));
					variables.remove(variable.asText());
				}
			});
		}
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

}
