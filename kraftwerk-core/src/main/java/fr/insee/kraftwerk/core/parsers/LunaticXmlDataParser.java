package fr.insee.kraftwerk.core.parsers;

import java.nio.file.Path;
import java.util.Arrays;

import fr.insee.kraftwerk.core.utils.XmlFileReader;
import fr.insee.kraftwerk.core.metadata.Group;
import fr.insee.kraftwerk.core.metadata.Variable;
import fr.insee.kraftwerk.core.metadata.VariableType;
import fr.insee.kraftwerk.core.metadata.VariablesMap;
import fr.insee.kraftwerk.core.rawdata.GroupData;
import fr.insee.kraftwerk.core.rawdata.GroupInstance;
import fr.insee.kraftwerk.core.rawdata.QuestionnaireData;
import fr.insee.kraftwerk.core.rawdata.SurveyRawData;
import lombok.extern.slf4j.Slf4j;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Elements;

@Slf4j
public class LunaticXmlDataParser implements DataParser {

	private Document document;

	/** Words used to filter VTL expressions in "calculated" elements. */
	private static final String[] forbiddenWords = {"cast", "isnull", "if "};

	/**
	 * Parse the XML file from the given path. The parsed object is set in the
	 * private attribute document.
	 *
	 * @param filePath Path to the XML file.
	 */
	public void readXmlFile(Path filePath) {
		XmlFileReader xmlFileReader = new XmlFileReader();
		document = xmlFileReader.readXmlFile(filePath.toString());
		if (document != null) {
			log.info("Successfully parsed Lunatic answers file: " + filePath);
		} else {
			log.warn("Failed to parse Lunatic answers file: " + filePath);
		}
	}

	/**
	 * Parse a Lunatic xml data file.
	 * "FILTER_RESULT" variables are added to the variables map.
	 * VTL expressions in the calculated elements are ignored.
	 */
	@Override
	public void parseSurveyData(SurveyRawData data) {

		Path filePath = data.getDataFilePath();
		readXmlFile(filePath);
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
			readCalculated(questionnaireNode, questionnaireData, data.getVariablesMap());

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
			String variableName = variableNode.getLocalName().toUpperCase();

			// Root variables
			if (variableNode.getFirstChildElement("COLLECTED").getAttribute("type") != null) {
				String value = variableNode.getFirstChildElement("COLLECTED").getValue();
				answers.putValue(variableName, value);
			}

			// Group variables // TODO : recursion etc.
			else {
				Elements valueNodes = variableNode.getFirstChildElement("COLLECTED").getChildElements();
				String groupName = variables.getVariable(variableName).getGroupName();
				GroupData groupData = answers.getSubGroup(groupName);
				for (int j = 0; j < valueNodes.size(); j++) {
					String value = valueNodes.get(j).getValue();
					groupData.putValue(value, variableName, j);
				}
			}
		}
	}

	/**
	 * Read data in the CALCULATED elements.
	 * "FILTER_RESULT" variables are added to the variables map.
	 * Values that are a vtl expression are filtered.
	 */
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
				String variableName = variableNode.getLocalName().toUpperCase();

				// Root variables
				if (variableNode.getAttribute("type") != null) {
					//
					if (variableName.startsWith("FILTER_RESULT_")) {
						variables.putVariable(new Variable(variableName, variables.getRootGroup(), VariableType.BOOLEAN));
					}
					//
					String value = variableNode.getValue();
					if(isNotVtlExpression(value)) {
						answers.putValue(variableName, value);
					}
				}

				// Group variables // TODO : recursion etc.
				else {
					Elements valueNodes = variableNode.getChildElements();
					//
					String groupName;
					if (variableName.startsWith("FILTER_RESULT_")) {
						String correspondingVariableName = variableName.replace("FILTER_RESULT_", "");
						if (variables.hasVariable(correspondingVariableName)) { // the variable is directly found
							Group group = variables.getVariable(correspondingVariableName).getGroup();
							groupName = group.getName();
							variables.putVariable(new Variable(variableName, group, VariableType.BOOLEAN));
						} else if (variables.hasMcq(correspondingVariableName)) { // otherwise it should be from a MCQ
							Group group = variables.getMcqGroup(correspondingVariableName);
							groupName = group.getName();
							variables.putVariable(new Variable(variableName, group, VariableType.BOOLEAN));
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
	 */
	private static boolean isNotVtlExpression(String value) {
		return !(stringContainsItemFromList(value, forbiddenWords));
	}

	/**
	 * Check if the string given contains any of the words listed in second parameter.
	 */
	public static boolean stringContainsItemFromList(String string, String[] items) {
	    return Arrays.stream(items).anyMatch(string::contains);
	}
	
}
