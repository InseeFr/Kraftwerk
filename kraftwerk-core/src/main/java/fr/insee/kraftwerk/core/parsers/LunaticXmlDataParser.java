package fr.insee.kraftwerk.core.parsers;

import java.util.Arrays;
import fr.insee.kraftwerk.core.Constants;
import fr.insee.kraftwerk.core.utils.XmlFileReader;
import fr.insee.kraftwerk.core.metadata.Group;
import fr.insee.kraftwerk.core.metadata.Variable;
import fr.insee.kraftwerk.core.metadata.VariableType;
import fr.insee.kraftwerk.core.metadata.VariablesMap;
import fr.insee.kraftwerk.core.rawdata.GroupData;
import fr.insee.kraftwerk.core.rawdata.GroupInstance;
import fr.insee.kraftwerk.core.rawdata.QuestionnaireData;
import fr.insee.kraftwerk.core.rawdata.SurveyRawData;
import fr.insee.kraftwerk.core.utils.XmlFileReader;
import lombok.extern.slf4j.Slf4j;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Elements;

import java.nio.file.Path;
import java.util.Arrays;

@Slf4j
public class LunaticXmlDataParser extends DataParser {

	/** Words used to filter VTL expressions in "calculated" elements.
	 * Deprecated since we don't read calculated values in the parser anymore. */
	@Deprecated
	private static final String[] forbiddenWords = {"cast", "isnull", "if "};

	/**
	 * Parser constructor.
	 * @param data The SurveyRawData to be filled by the parseSurveyData method.
	 *             The variables must have been previously set.
	 */
	public LunaticXmlDataParser(SurveyRawData data) {
		super(data);
	}

	/**
	 * Parse the XML file from the given path.
	 * @param filePath Path to the XML file.
	 * @return The parsed document.
	 */
	private Document readXmlFile(Path filePath) {
		XmlFileReader xmlFileReader = new XmlFileReader();
		Document document = xmlFileReader.readXmlFile(filePath);
		if (document != null) {
			log.info("Successfully parsed Lunatic answers file: " + filePath);
		} else {
			log.warn("Failed to parse Lunatic answers file: " + filePath);
		}
		return document;
	}

	/**
	 * Parse a Lunatic xml data file.
	 * Only "COLLECTED" and "EXTERNAL" variables are read.
	 * @param filePath Path to a Lunatic xml data file.
	 */
	@Override
	void parseDataFile(Path filePath) {

		Document document = readXmlFile(filePath);
		Elements questionnaireNodeList = document.getRootElement().getFirstChildElement("SurveyUnits")
				.getChildElements("SurveyUnit");

		for (int i = 0; i < questionnaireNodeList.size(); i++) {

			// Xml questionnaire node
			Element questionnaireNode = questionnaireNodeList.get(i);

			// Init the questionnaire data object
			QuestionnaireData questionnaireData = new QuestionnaireData();

			// Root identifier
			questionnaireData.setIdentifier(questionnaireNode.getFirstChildElement("Id").getValue());

			readCollected(questionnaireNode, questionnaireData, data.getVariablesMap());
			readExternal(questionnaireNode, questionnaireData, data.getVariablesMap());
			readCalculated(questionnaireNode, questionnaireData, data.getVariablesMap()); TODO: remove this line

			data.addQuestionnaire(questionnaireData);
		}
	}

	/**
	 * Read data in the COLLECTED elements.
	 */
	private void readCollected(Element questionnaireNode, QuestionnaireData questionnaireData,
											VariablesMap variables) {

		// Xml collected variables nodes
		Elements collectedVariablesNodes = questionnaireNode.getFirstChildElement("Data")
				.getFirstChildElement("COLLECTED").getChildElements();

		// Data object
		GroupInstance answers = questionnaireData.getAnswers();

		for (Element variableNode : collectedVariablesNodes) {

			// Variable name
			String variableName = variableNode.getLocalName();

			//
			Element collectedNode = variableNode.getFirstChildElement("COLLECTED");

			// Root variables // TODO: "_MISSING" variables management
			if (collectedNode.getAttribute("type") != null) {
				if(! collectedNode.getAttribute("type").getValue().equals("null")) {
					String value = variableNode.getFirstChildElement("COLLECTED").getValue();
					answers.putValue(variableName, value);
				}
			}

			// Group variables // TODO : recursion etc.
			else {
				Elements valueNodes = collectedNode.getChildElements();
				if(variables.hasVariable(variableName)) { // TODO: "_MISSING" variables management
					String groupName = variables.getVariable(variableName).getGroupName();
					GroupData groupData = answers.getSubGroup(groupName);
					for (int j = 0; j < valueNodes.size(); j++) {
						Element valueNode = valueNodes.get(j);
						if(! valueNode.getAttribute("type").getValue().equals("null")) {
							String value = valueNodes.get(j).getValue();
							groupData.putValue(value, variableName, j);
						}
					}
				}
			}
		}
	}

	/**
	 * Read data in the EXTERNAL elements.
	 * "External" variables are always in the root group.
	 */
	private void readExternal(Element questionnaireNode, QuestionnaireData questionnaireData,
							   VariablesMap variables) {

		Element externalNode = questionnaireNode.getFirstChildElement("Data")
				.getFirstChildElement("EXTERNAL");

		if (externalNode != null) {

			Elements externalVariableNodes = externalNode.getChildElements();

			for (Element externalVariableNode : externalVariableNodes) {
				if(! externalVariableNode.getAttribute("type").getValue().equals("null")) {
					String variableName = externalVariableNode.getLocalName();
					String value = externalVariableNode.getValue();
					questionnaireData.putValue(value, variableName);
				}
			}
		}
	}

	/**
	 * Read data in the CALCULATED elements.
	 * "FILTER_RESULT" variables are added to the variables map.
	 * Values that are a vtl expression are filtered.
	 */
	@Deprecated
	private void readCalculated(Element questionnaireNode, QuestionnaireData questionnaireData,
											 VariablesMap variables) {

		// Xml collected variables nodes
		Element calculatedNode = questionnaireNode.getFirstChildElement("Data")
				.getFirstChildElement("CALCULATED");

		if(calculatedNode != null) {
			Elements calculatedVariablesNodes = calculatedNode.getChildElements();

			// Data object
			GroupInstance answers = questionnaireData.getAnswers();

			for (Element variableNode : calculatedVariablesNodes) {

				// Variable name
				String variableName = variableNode.getLocalName();

				// Root variables
				if (variableNode.getAttribute("type") != null) {
					if (!variableNode.getAttribute("type").getValue().equals("null")) {
						//
						if (variableName.startsWith("FILTER_RESULT_")) {
							variables.putVariable(new Variable(variableName, variables.getRootGroup(), VariableType.BOOLEAN, "5"));
						}
						//
						String value = variableNode.getValue();
						if(isNotVtlExpression(value)) {
							answers.putValue(variableName, value);
						}
					}
				}

				// Group variables // TODO : recursion etc.
				else {
					Elements valueNodes = variableNode.getChildElements();
					//
					String groupName;
					if (variableName.startsWith(Constants.FILTER_RESULT_PREFIX)) {
						String correspondingVariableName = variableName.replace(Constants.FILTER_RESULT_PREFIX, "");
						if (variables.hasVariable(correspondingVariableName)) { // the variable is directly found
							Group group = variables.getVariable(correspondingVariableName).getGroup();
							groupName = group.getName();
							variables.putVariable(new Variable(variableName, group, VariableType.BOOLEAN, "1"));
						} else if (variables.hasMcq(correspondingVariableName)) { // otherwise, it should be from a MCQ
							Group group = variables.getMcqGroup(correspondingVariableName);
							groupName = group.getName();
							variables.putVariable(new Variable(variableName, group, VariableType.BOOLEAN, "1"));
						} else {
							Group group = variables.getGroup(variables.getGroupNames().get(0));
							groupName = group.getName(); //TODO : make the log appear only one time per variable (not at each questionnaire occurrence).
							log.warn(String.format(
									"No information from the DDI about question named \"%s\".",
									correspondingVariableName));
							log.warn(String.format(
									"\"%s\" has been arbitrarily associated with group \"%s\".",
									variableName, groupName));
						}
					} else {
						groupName = variables.getVariable(variableName).getGroupName();
					}
					//
					GroupData groupData = answers.getSubGroup(groupName);
					for (int j = 0; j < valueNodes.size(); j++) {
						String value = valueNodes.get(j).getValue();
						if(isNotVtlExpression(value)) {
							groupData.putValue(value, variableName, j);
						}
					}
				}
			}
		}
	}

	/**
	 * Check if the given value is a VTL expression using the 'forbiddenWords' attribute.
	 * Deprecated since we don't read calculated values in the parser anymore.
	 * @see fr.insee.kraftwerk.core.metadata.LunaticReader
	 * @see fr.insee.kraftwerk.core.dataprocessing.CalculatedProcessing
	 */
	@Deprecated
	private static boolean isNotVtlExpression(String value) {
		return Arrays.stream(forbiddenWords).noneMatch(value::contains);
	}
	
}
