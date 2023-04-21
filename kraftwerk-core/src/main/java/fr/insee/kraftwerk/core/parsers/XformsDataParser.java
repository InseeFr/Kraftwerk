package fr.insee.kraftwerk.core.parsers;

import java.nio.file.Path;

import fr.insee.kraftwerk.core.rawdata.GroupData;
import fr.insee.kraftwerk.core.rawdata.GroupInstance;
import fr.insee.kraftwerk.core.rawdata.QuestionnaireData;
import fr.insee.kraftwerk.core.rawdata.SurveyRawData;
import fr.insee.kraftwerk.core.utils.XmlFileReader;
import lombok.extern.log4j.Log4j2;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Elements;

/**
 * Implementation of DataParser to read Xforms data files.
 */
@Log4j2
public class XformsDataParser extends DataParser {

	/**
	 * Parser constructor.
	 * @param data The SurveyRawData to be filled by the parseSurveyData method.
	 *             The variables must have been previously set.
	 */
	public XformsDataParser(SurveyRawData data) {
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
			log.info("Successfully parsed Xforms answers file: " + filePath);
		} else {
			log.warn("Failed to parse Xforms answers file: " + filePath);
		}
		return document;
	}

	/**
	 * Read a Xforms survey data file and fills the SurveyRawData object.
	 * The method only reads values with the attribute type="nouvelle". Values with attribute
	 * type="ancienne" are ignored. Variables which are not in the DDI are ignored.
	 *
	 * @param filePath Path to a Xforms data file.
	 */
	@Override
	void parseDataFile(Path filePath) {

		Document document = readXmlFile(filePath);

		if(document!=null) {
			Element root = document.getRootElement();
			Element questionnairesNode = root.getFirstChildElement("Questionnaires");
			Elements questionnairesNodeList = questionnairesNode.getChildElements("Questionnaire");

			for (int i = 0; i < questionnairesNodeList.size(); i++) {

				QuestionnaireData questionnaireData = new QuestionnaireData();
				GroupInstance answers = questionnaireData.getAnswers();

				Element questionnaireNode = questionnairesNodeList.get(i);

				// Identifier
				Element identifierNode = questionnaireNode.getFirstChildElement("InformationsGenerales")
						.getFirstChildElement("UniteEnquetee").getFirstChildElement("Identifiant");
				String identifier = identifierNode.getValue();
				questionnaireData.setIdentifier(identifier);
				data.getIdSurveyUnits().add(identifier);

				// Survey answers

				// Root variables
				Elements variableNodeList = questionnaireNode.getFirstChildElement("InformationsPersonnalisees")
						.getChildElements("Variable");
				for (int j = 0; j < variableNodeList.size(); j++) {
					Element variableNode = variableNodeList.get(j);
					String variableName = variableNode.getAttributeValue("idVariable");
					if (data.getVariablesMap().hasVariable(variableName)) {
						String value = getNodeValue(variableNode);
						answers.putValue(variableName, value);
					} else {
						if (i == 0) {
							log.info(String.format(
									"Root variable \"%s\" not expected, corresponding values will be ignored.", variableName));
						}
					}

				}

				// Root groups TODO : implement recursions for groups in groups etc.
				Elements groupNodeList = questionnaireNode.getFirstChildElement("InformationsPersonnalisees")
						.getChildElements("Groupe");

				for (Element groupNode : groupNodeList) {
					// Get the group instances
					Elements groupInstanceNodeList = groupNode.getChildElements("Groupe");
					// Get the group name of the instances
					String groupName = groupInstanceNodeList.get(0).getAttributeValue("typeGroupe");

					GroupData groupData = answers.getSubGroup(groupName);

					for (Element groupInstanceNode : groupInstanceNodeList) {
						String groupInstanceId = groupInstanceNode.getAttributeValue("idGroupe");
						GroupInstance groupInstance = groupData.getInstance(groupInstanceId);
						Elements groupVariableNodeList = groupInstanceNode.getChildElements("Variable");
						for (Element variableNode : groupVariableNodeList) {
							String variableName = variableNode.getAttributeValue("idVariable");
							if (data.getVariablesMap().hasVariable(variableName)) {
								String value = getNodeValue(variableNode);
								groupInstance.putValue(variableName, value);
							} else {
								if (i == 0 && groupInstanceId.endsWith("1")) {
									log.info(String.format(
											"Variable \"%s\" not expected in group \"%s\", corresponding values will be ignored.",
											variableName, groupName));
								}

							}

						}
					}
				}

				data.addQuestionnaire(questionnaireData);
			}
		}
	}

	/**
	 * Return the value of a "Variable" node. In Coleman files, theses nodes are
	 * like this: <Variable idVariable="[variableLabel]"> <Valeur type="ancienne"/>
	 * <Valeur type="nouvelle">[value]</Valeur> </Variable>
	 *
	 * @param variableNode : A nu.xom.Element at the "Variable" level.
	 *
	 * @return The Sting value of the node.
	 */
	private String getNodeValue(Element variableNode) {

		// Method using XPath
		return variableNode.query("./Valeur[@type=\"nouvelle\"]").get(0).getValue();

		// Method iterating on children nodes
		/*
		 * final String[] value = new String[1];
		 * variableNode.getChildElements("Valeur").forEach( valueNode -> {
		 * if(valueNode.getAttributeValue("type").equals("nouvelle")){ value[0] =
		 * valueNode.getValue(); } } ); return value[0];
		 */

		// TODO: test efficiency of both methods
	}

}
