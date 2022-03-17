package fr.insee.kraftwerk.metadata;

import fr.insee.kraftwerk.Constants;
import fr.insee.kraftwerk.utils.XmlFileReader;
import fr.insee.kraftwerk.utils.xsl.SaxonTransformer;
import lombok.extern.slf4j.Slf4j;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Elements;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

@Slf4j
public class DDIReader {


	/**
	 * This method apply the XSLT_STRUCTURED_VARIABLES transformation to the DDI,
	 * then reads the output xml to return the variables.
	 * The XML file generated is written at path given.
	 *
	 * @param DDIPath 				: Path to the DDI file.
	 * @param outputVariablesPath 	: Path to the variables XML file that will be created.
	 *
	 * @return The variables found in the DDI.
	 */
	public static VariablesMap getVariablesFromDDI(String DDIPath, String outputVariablesPath) {
		try {
			URL DDIUrl = Constants.convertToUrl(DDIPath);
			transformDDI(DDIUrl, outputVariablesPath);
			return getVariablesFromTransformed(outputVariablesPath);
		} catch (MalformedURLException e) {
			log.error(String.format("Error when converting file path '%s' to an URL.", DDIPath), e);
			return null;
		}
	}

	/**
	 * This method apply the XSLT_STRUCTURED_VARIABLES transformation to the DDI,
	 * then reads the output xml to return the variables.
	 * The XML file generated is written in the system temporary folder with the name 'variables.xml',
	 * and is deleted when the virtual machine terminates.
	 *
	 * @param DDIPath 	: Path to the DDI file.
	 *
	 * @return The variables found in the DDI.
	 */
	public static VariablesMap getVariablesFromDDI(String DDIPath){

		try {
			//
			URL DDIUrl = Constants.convertToUrl(DDIPath);

			// Path of the output 'variables.xml' temp file
			File variablesFile = File.createTempFile("variables",".xml");
			variablesFile.deleteOnExit();
			String variablesTempFilePath = variablesFile.getAbsolutePath();

			//
			transformDDI(DDIUrl, variablesTempFilePath);
			return getVariablesFromTransformed(variablesTempFilePath);
		}

		catch (MalformedURLException e) {
			log.error(String.format("Error when converting file path '%s' to an URL.", DDIPath), e);
			return null;
		}
		catch (IOException e) {
			log.error("Unable to write temp file.", e);
			return null;
		}
	}

	/**
	 * Parse the file and read its variables.
	 * @param variablesFilePath Path of the transformed xml file.
	 * @return The variables described in the file.
	 */
	public static VariablesMap getVariablesFromTransformed(String variablesFilePath) {
		return readVariables(variablesFilePath);
	}
	

	/**
	 * Apply the XSLT_STRUCTURED_VARIABLES transformation.
	 *
	 * @param DDIUrl 			: URL of the DDI file.
	 * @param variablesFilePath : Path of the 'variables.xml' file to be generated.
	 * */
	private static void transformDDI(URL DDIUrl, String variablesFilePath){
		SaxonTransformer saxonTransformer = new SaxonTransformer();
		saxonTransformer.xslTransform(DDIUrl, Constants.XSLT_STRUCTURED_VARIABLES, variablesFilePath);
	}

	/**
	 * Parse the transformed xml file (using XmlFileReader),
	 * and read the transformed xml to return a VariablesMap.
	 * @param variablesFilePath Path to the transformed xml file.
	 * @return The variables described in the file.
	 */
	private static VariablesMap readVariables(String variablesFilePath) {
		VariablesMap variablesMap = new VariablesMap();

		// Parse
		XmlFileReader reader = new XmlFileReader();
		Document document = reader.readXmlFile(variablesFilePath);
		Element root = document.getRootElement();

		// Get XML groups
		Elements groupElements = root.getChildElements("Group");

		// temporary save the root group name to normalize it
		String rootGroupName = null;

		for (int i=0; i<groupElements.size(); i++) {
			Element groupElement = groupElements.get(i);

			// Get the group name
			String groupName = groupElement.getAttributeValue("name");
			String parentGroupName = groupElement.getAttributeValue("parent");

			// Store the group
			Group group;
			if (parentGroupName == null) {
				rootGroupName = groupName;
				group = variablesMap.getRootGroup();
			} else {
				group = new Group(groupName, parentGroupName);
				variablesMap.putGroup(group);
			}

			// Variables in the group
			Elements variableElements = groupElement.getChildElements("Variable");
			for (int j=0; j<variableElements.size(); j++) {
				Element variableElement = variableElements.get(j);
				// Variable name and type
				String variableName = variableElement.getFirstChildElement("Name").getValue();
				VariableType variableType = VariableType.valueOf(
						variableElement.getFirstChildElement("Format").getValue());
				//
				Element valuesElement = variableElement.getFirstChildElement("Values");
				//
				Element mcqElement = variableElement.getFirstChildElement("MCQ");
				//
				if (valuesElement != null) {
					UcqVariable variable = new UcqVariable(variableName, group, variableType);
					Elements valueElements = valuesElement.getChildElements("Value");
					for (int k=0; k<valueElements.size(); k++) {
						Element valueElement = valueElements.get(k);
						variable.addModality(valueElement.getValue(), valueElement.getAttributeValue("label"));
					}
					variablesMap.putVariable(variable);
				} else if (mcqElement != null) {
					McqVariable variable = new McqVariable(variableName, group, variableType);
					variable.setMqcName(mcqElement.getValue());
					variable.setText(variableElement.getFirstChildElement("Label").getValue());
					variablesMap.putVariable(variable);
				} else {
					Variable variable = new Variable(variableName, group, variableType);
					variablesMap.putVariable(variable);
				}
			}
		}

		// Normalize the root group name
		if (rootGroupName == null) {
			log.debug("Failed to identify the root group while reading variables files: " + variablesFilePath);
		}
		for (String groupName : variablesMap.getSubGroupNames()) {
			Group group = variablesMap.getGroup(groupName);
			if (group.getParentName().equals(rootGroupName)) {
				group.setParentName(Constants.ROOT_GROUP_NAME);
			}
		}

		return variablesMap;
	}

}