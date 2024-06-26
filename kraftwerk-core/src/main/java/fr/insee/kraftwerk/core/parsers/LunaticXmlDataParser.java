package fr.insee.kraftwerk.core.parsers;

import fr.insee.kraftwerk.core.Constants;
import fr.insee.kraftwerk.core.metadata.*;
import fr.insee.kraftwerk.core.rawdata.GroupData;
import fr.insee.kraftwerk.core.rawdata.GroupInstance;
import fr.insee.kraftwerk.core.rawdata.QuestionnaireData;
import fr.insee.kraftwerk.core.rawdata.SurveyRawData;
import fr.insee.kraftwerk.core.utils.xml.XmlFileReader;
import fr.insee.kraftwerk.core.utils.files.FileUtilsInterface;
import lombok.extern.log4j.Log4j2;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Elements;

import java.nio.file.Path;
import java.util.Arrays;

/**
 * Parser add FILTER_RESULT to variablesMap
 *
 */
@Log4j2
public class LunaticXmlDataParser extends DataParser {

	/**
	 * Words used to filter VTL expressions in "calculated" elements.
	 */
	private static final String[] forbiddenWords = { "cast", "isnull", "if " };

	/**
	 * Parser constructor.
	 * 
	 * @param data The SurveyRawData to be filled by the parseSurveyData method. The
	 *             variables must have been previously set.
	 */
	public LunaticXmlDataParser(SurveyRawData data, FileUtilsInterface fileUtilsInterface) {
		super(data, fileUtilsInterface);
	}

	/**
	 * Parse the XML file from the given path.
	 * 
	 * @param filePath Path to the XML file.
	 * @return The parsed document.
	 */
	private Document readXmlFile(Path filePath) {
		XmlFileReader xmlFileReader = new XmlFileReader(fileUtilsInterface);
		Document document = xmlFileReader.readXmlFile(filePath);
		if (document == null) {
			log.warn("Failed to parse Lunatic answers file: " + filePath);
		}
		return document;
	}

	private void parseDataFile(Path filePath, Path lunaticFile) {

		Document document = readXmlFile(filePath);
		log.debug("Begin to parse {} ", filePath);
		if (document != null) {
			Elements questionnaireNodeList = document.getRootElement().getFirstChildElement("SurveyUnits")
					.getChildElements("SurveyUnit");
			String questionnaireModelId = null;
			if (lunaticFile != null) {
				questionnaireModelId = LunaticReader.getQuestionnaireModelId(lunaticFile, fileUtilsInterface);
			}

			for (int i = 0; i < questionnaireNodeList.size(); i++) {

				// Xml questionnaire node
				Element questionnaireNode = questionnaireNodeList.get(i);

				if (lunaticFile == null || checkLunaticQuestionnaire(questionnaireModelId, questionnaireNode)) {

					// Init the questionnaire data object
					QuestionnaireData questionnaireData = new QuestionnaireData();

					// Root identifier
					questionnaireData.setIdentifier(questionnaireNode.getFirstChildElement("Id").getValue());
					data.getIdSurveyUnits().add(questionnaireNode.getFirstChildElement("Id").getValue());

					readCollected(questionnaireNode, questionnaireData, data.getMetadataModel().getVariables());
					readExternal(questionnaireNode, questionnaireData, data.getMetadataModel());
					// Remove this method when all questionnaires will use Lunatic V2 format
					readCalculated(questionnaireNode, questionnaireData, data.getMetadataModel().getVariables());

					data.addQuestionnaire(questionnaireData);
				}
			}
			log.info("Successfully parsed Lunatic answers file: {}", filePath);
		}
	}

	/**
	 * Parse a Lunatic xml data file. Only "COLLECTED" and "EXTERNAL" variables are
	 * read.
	 * 
	 * @param filePath Path to a Lunatic xml data file.
	 */
	@Override
	void parseDataFile(Path filePath) {
		parseDataFile(filePath, null);
	}

	/**
	 * Parse a Lunatic xml data file. Only "COLLECTED" and "EXTERNAL" variables are
	 * read.
	 * 
	 * @param filePath Path to a Lunatic xml data file.
	 */
	@Override
	void parseDataFileWithoutDDI(Path filePath, Path lunaticFile) {
		parseDataFile(filePath, lunaticFile);
	}

	private boolean checkLunaticQuestionnaire(String questionnaireModelId, Element questionnaireNode) {
		return questionnaireNode.getFirstChildElement("QuestionnaireModelId").getValue().equals(questionnaireModelId);
	}

	/**
	 * Read data in the COLLECTED elements.
	 */
	private void readCollected(Element questionnaireNode, QuestionnaireData questionnaireData, VariablesMap variables) {

		//Check collected data tag presence
		if(questionnaireNode.getFirstChildElement("Data").getFirstChildElement(Constants.COLLECTED) == null){
			log.warn("No collected data for survey unit " + questionnaireData.getIdentifier());
			return;
		}

		// Xml collected variables nodes
		Elements collectedVariablesNodes = questionnaireNode.getFirstChildElement("Data")
				.getFirstChildElement(Constants.COLLECTED).getChildElements();

		// Data object
		GroupInstance answers = questionnaireData.getAnswers();

		for (Element variableNode : collectedVariablesNodes) {

			// Variable name
			String variableName = variableNode.getLocalName();
			Element collectedNode = variableNode.getFirstChildElement(Constants.COLLECTED);

			// Root variables
			if (nodeExistsWithCompleteAttribute(collectedNode)) {
				String value = variableNode.getFirstChildElement(Constants.COLLECTED).getValue();
				updateMaxLength(variables, variableName, value);
				answers.putValue(variableName, value);
			}

			// Group variables // TODO : recursion etc.
			else if (collectedNode != null) {
				addGroupVariables(variables, variableName, answers, collectedNode, true);
			}
		}

	}

	private void updateMaxLength(VariablesMap variables, String variableName, String value) {
		if ((variables.getVariable(variableName) != null)
				&& value.length() > variables.getVariable(variableName).getMaxLengthData()) {
			variables.getVariable(variableName).setMaxLengthData(value.length());
		}
	}

	private boolean nodeExistsWithCompleteAttribute(Element currentNode) {
		return currentNode != null && currentNode.getAttribute("type") != null
				&& !currentNode.getAttribute("type").getValue().equals("null");
	}

	/**
	 * Read data in the EXTERNAL elements. "External" variables are always in the
	 * root group.
	 */
	private void readExternal(Element questionnaireNode, QuestionnaireData questionnaireData, MetadataModel metadataModel) {

		Element externalNode = questionnaireNode.getFirstChildElement("Data").getFirstChildElement("EXTERNAL");
		if (externalNode == null)
			return;

		Elements externalVariableNodes = externalNode.getChildElements();
		if (externalVariableNodes == null)
			return;

		for (Element externalVariableNode : externalVariableNodes) {
			if (externalVariableNode == null)
				return;

			VariablesMap variables = metadataModel.getVariables();

			if (nodeExistsWithCompleteAttribute(externalVariableNode)) {
				String variableName = externalVariableNode.getLocalName();
				String value = externalVariableNode.getValue();
				questionnaireData.putValue(value, variableName);
				if (!variables.hasVariable(variableName)) {
					variables.putVariable(new Variable(variableName, metadataModel.getRootGroup(), VariableType.STRING));
					log.warn(String.format(
							"EXTERNAL variable \"%s\" was not found in DDI and has been added, with type STRING.",
							variableName));
				}
			}
			// Group variables
			else {
				addGroupVariables(variables, externalVariableNode.getLocalName(), questionnaireData.getAnswers(),
						externalVariableNode, false);
			}
		}

	}

	private void addGroupVariables(VariablesMap variables, String variableName, GroupInstance answers, Element node,
			boolean collected) {

		if (!variables.hasVariable(variableName)) {
			log.debug("Variable not defined : {}", variableName);
			return;
		}
		manageTcmLiens(variableName, answers, node);
		
		String groupName = variables.getVariable(variableName).getGroupName();
		GroupData groupData = answers.getSubGroup(groupName);
		Elements valueNodes = node.getChildElements();

		for (int j = 0; j < valueNodes.size(); j++) {
			Element valueNode = valueNodes.get(j);
			if (nodeExistsWithCompleteAttribute(valueNode)) {
				String value = valueNodes.get(j).getValue();
				if (collected) {
					updateMaxLength(variables, variableName, value);
				}
				groupData.putValue(value, variableName, j);
			}
		}

	}

	private void manageTcmLiens(String variableName, GroupInstance answers, Element node) {
		if (variableName.equals(Constants.LIENS)) {
			String groupName = Constants.BOUCLE_PRENOMS;
			GroupData groupData = answers.getSubGroup(groupName);
			Elements individualNodes = node.getChildElements();
			
			for (int j = 0; j < individualNodes.size(); j++) {
				Element individualNode = individualNodes.get(j);
				Elements valueNodes = individualNode.getChildElements();

				for (int k=1;k <Constants.MAX_LINKS_ALLOWED;k++) {
					String value = Constants.NO_PAIRWISE_VALUE;
					if (k<=valueNodes.size()) { 
						value=Constants.SAME_AXIS_VALUE;
						Element valueNode = valueNodes.get(k-1);
						if (nodeExistsWithCompleteAttribute(valueNode)) {
							value = valueNodes.get(k-1).getValue();
						}
					}
					groupData.putValue(value, Constants.LIEN+k, j);
				}
			}
		}
	}

	/**
	 * Read data in the CALCULATED elements. Values that are a vtl expression are
	 * filtered. To bo be removed when all questionnaires will use Lunatic V2.
	 */
	private void readCalculated(Element questionnaireNode, QuestionnaireData questionnaireData,
			VariablesMap variables) {

		// Xml collected variables nodes
		Elements calculatedVariablesNodes = getCalculatedElements(questionnaireNode);
		if (calculatedVariablesNodes == null)
			return;

		// Data object
		GroupInstance answers = questionnaireData.getAnswers();

		for (Element variableNode : calculatedVariablesNodes) {

			// Variable name
			String variableName = variableNode.getLocalName();

			// Root variables
			if (nodeExistsWithCompleteAttribute(variableNode)) {
				String value = variableNode.getValue();
				if (isNotVtlExpression(value)) {
					answers.putValue(variableName, value);
				}
			}

			// Group variables
			else {
				Elements valueNodes = variableNode.getChildElements();
				String groupName = getGroupName(variables, variableName);
				GroupData groupData = answers.getSubGroup(groupName);
				for (int j = 0; j < valueNodes.size(); j++) {
					String value = valueNodes.get(j).getValue();
					if (isNotVtlExpression(value)) {
						groupData.putValue(value, variableName, j);
					}
				}
			}
		}

	}

	private String getGroupName(VariablesMap variables, String variableName) {
		String groupName;

		if (variables.getVariable(variableName) != null) {
			groupName = variables.getVariable(variableName).getGroupName();
		} else {
			groupName = Constants.ROOT_GROUP_NAME;
		}
		return groupName;
	}

	private Elements getCalculatedElements(Element questionnaireNode) {
		Element calculatedNode = questionnaireNode.getFirstChildElement("Data").getFirstChildElement("CALCULATED");
		if (calculatedNode == null)
			return null;
		return calculatedNode.getChildElements();
	}

	/**
	 * Check if the given value is a VTL expression using the 'forbiddenWords'
	 * attribute.
	 * 
	 * @see fr.insee.kraftwerk.core.metadata.LunaticReader
	 * @see fr.insee.kraftwerk.core.dataprocessing.CalculatedProcessing
	 */
	private static boolean isNotVtlExpression(String value) {
		return Arrays.stream(forbiddenWords).noneMatch(value::contains);
	}

}
