package fr.insee.kraftwerk.core.parsers;

import fr.insee.kraftwerk.core.utils.XmlFileReader;

import java.nio.file.Path;

import fr.insee.kraftwerk.core.rawdata.GroupData;
import fr.insee.kraftwerk.core.rawdata.GroupInstance;
import fr.insee.kraftwerk.core.rawdata.QuestionnaireData;
import fr.insee.kraftwerk.core.rawdata.SurveyRawData;
import lombok.extern.slf4j.Slf4j;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Elements;

/**
 * Implementation of DataParser to read data from Coleman.
 *
 */
@Slf4j
public class XformsDataParser implements DataParser {

	private Document document;

	/**
	 * Read a Coleman survey data XML file and fills the SurveyRawData object given.
	 * The XML file is before hand flattened using the XSLT_FLATTEN_COLEMAN_FILE.
	 * The flattened file is written in the output folder. The method only reads
	 * values with the attribute type="nouvelle". Values with attribute
	 * type="ancienne" are ignored. Variables which are not in the DDI are ignored.
	 *
	 * @param data The SurveyRawData object to be filled, the variables must have
	 *             already been set.
	 */
	public void parseSurveyData(SurveyRawData data) {

		Path filePath = data.getDataFilePath();
		readFile(filePath);

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

			// Survey answers

			// Root variables
			Elements variableNodeList = questionnaireNode.getFirstChildElement("InformationsPersonnalisees")
					.getChildElements("Variable");
			for (int j = 0; j < variableNodeList.size(); j++) {
				Element variableNode = variableNodeList.get(j);
				String variableName = variableNode.getAttributeValue("idVariable").toUpperCase();
				if (data.getVariablesMap().getVariables().containsKey(variableName)) {
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
						String variableName = variableNode.getAttributeValue("idVariable").toUpperCase();
						if (data.getVariablesMap().getVariables().containsKey(variableName)) {
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

		// Free memory
		document = null;
	}

	/**
	 * Parse the XML file from the given path. The parsed object is set in the
	 * private attribute document.
	 *
	 * @param filePath Path to the XML file.
	 */
	private void readFile(Path filePath) {
		XmlFileReader xmlFileReader = new XmlFileReader();
		document = xmlFileReader.readXmlFile(filePath.toString());
		if (document != null) {
			log.info("Successfully parsed Coleman answers file: " + filePath);
		} else {
			log.warn("Failed to parse Coleman answers file: " + filePath);
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
