package fr.insee.kraftwerk.core.extradata.reportingdata;

import java.nio.file.Path;

import fr.insee.kraftwerk.core.rawdata.SurveyRawData;
import fr.insee.kraftwerk.core.utils.XmlFileReader;
import lombok.extern.slf4j.Slf4j;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Elements;

@Slf4j
public class XMLReportingDataParser extends ReportingDataParser {

	private Document document;

	public void parseReportingData(ReportingData reportingData, SurveyRawData data) {

		Path filePath = reportingData.getFilepath();
		readFile(filePath);

		Element root = document.getRootElement();

		Element surveyUnitsNode = root.getFirstChildElement("SurveyUnits");

		Elements surveyUnitsNodeList = surveyUnitsNode.getChildElements("SurveyUnit");

		for (int i = 0; i < surveyUnitsNodeList.size(); i++) {

			Element surveyUnitNode = surveyUnitsNodeList.get(i);

			ReportingDataUE reportingDataUE = new ReportingDataUE();

			// Identifier
			Element identifierNode = surveyUnitNode.getFirstChildElement("Id");
			String identifier = identifierNode.getValue();
			reportingDataUE.setIdentifier(identifier);

			// Survey's states
			Elements stateNodeList = surveyUnitNode.getFirstChildElement("States").getChildElements("State");
			for (int j = 0; j < stateNodeList.size(); j++) {
				Element stateNode = stateNodeList.get(j);
				String type = stateNode.getFirstChildElement("type").getValue().toUpperCase();
				String timestamp = stateNode.getFirstChildElement("date").getValue();
				reportingDataUE.addState(new State(type, Long.parseLong(timestamp)));

			}
			reportingData.addReportingDataUE(reportingDataUE);
		}
		integrateReportingDataIntoUE(data, reportingData);
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
		document = xmlFileReader.readXmlFile(filePath);
		if (document != null) {
			log.info("Successfully parsed Coleman answers file: " + filePath);
		} else {
			log.warn("Failed to parse Coleman answers file: " + filePath);
		}
	}

}
