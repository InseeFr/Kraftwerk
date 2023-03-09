package fr.insee.kraftwerk.core.parsers;

import java.nio.file.Path;
import java.util.Arrays;

import fr.insee.kraftwerk.core.Constants;
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

/**
 * Parser add FILTER_RESULT to variablesMap
 *
 */
@Slf4j
public class LunaticXmlDataParser extends DataParser {

	/** Words used to filter VTL expressions in "calculated" elements.
	 */
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
		if (document == null) {
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
		log.debug("Begin to parse {} ", filePath);
		if (document!=null) {
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
				// Remove this method when all questionnaires will use Lunatic V2 format
				readCalculated(questionnaireNode, questionnaireData, data.getVariablesMap());

				data.addQuestionnaire(questionnaireData);
			}
			log.info("Successfully parsed Lunatic answers file: {}",filePath );
		}
	}

	/**
	 * Read data in the COLLECTED elements. To bo be removed when all questionnaires will use Lunatic V2.
	 */
	private void readCollected(Element questionnaireNode, QuestionnaireData questionnaireData,
											VariablesMap variables) {

		// Xml collected variables nodes
		Elements collectedVariablesNodes = questionnaireNode.getFirstChildElement("Data")
				.getFirstChildElement(Constants.COLLECTED).getChildElements();

		// Data object
		GroupInstance answers = questionnaireData.getAnswers();

		for (Element variableNode : collectedVariablesNodes) {

			// Variable name
			String variableName = variableNode.getLocalName();

			//
			Element collectedNode = variableNode.getFirstChildElement(Constants.COLLECTED);

			// Root variables
			if (collectedNode.getAttribute("type") != null) {
				if(! collectedNode.getAttribute("type").getValue().equals("null")) {
					String value = variableNode.getFirstChildElement(Constants.COLLECTED).getValue();
					answers.putValue(variableName, value);
				}
			}

			// Group variables // TODO : recursion etc.
			else {
				Elements valueNodes = collectedNode.getChildElements();
				if(variables.hasVariable(variableName)) {
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
				if (externalVariableNode.getAttribute("type") != null) {
					if (!externalVariableNode.getAttribute("type").getValue().equals("null")) {
						String variableName = externalVariableNode.getLocalName();
						String value = externalVariableNode.getValue();
						questionnaireData.putValue(value, variableName);
						if (!variables.hasVariable(variableName)) {
							variables.putVariable(
									new Variable(variableName, variables.getRootGroup(), VariableType.STRING));
							log.warn(String.format(
									"EXTERNAL variable \"%s\" was not found in DDI and has been added, with type STRING.",
									variableName));
						}
					}
				}
			}
		}
	}

	/**
	 * Read data in the CALCULATED elements.
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
				String variableName = variableNode.getLocalName();

				// Root variables
				if (variableNode.getAttribute("type") != null) {
					if (!variableNode.getAttribute("type").getValue().equals("null")) {
						String value = variableNode.getValue();
						if(isNotVtlExpression(value)) {
							answers.putValue(variableName, value);
						}
					}
				}

				// Group variables
				else {
					Elements valueNodes = variableNode.getChildElements();
					String groupName;

					if (variables.getVariable(variableName) != null) {
					groupName = variables.getVariable(variableName).getGroupName();
					} else {
						groupName = Constants.ROOT_GROUP_NAME;
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
	 * @see fr.insee.kraftwerk.core.metadata.LunaticReader
	 * @see fr.insee.kraftwerk.core.dataprocessing.CalculatedProcessing
	 */
	private static boolean isNotVtlExpression(String value) {
		return Arrays.stream(forbiddenWords).noneMatch(value::contains);
	}
	
}
