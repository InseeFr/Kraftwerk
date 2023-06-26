package fr.insee.kraftwerk.core.extradata.reportingdata;

import java.nio.file.Path;

import fr.insee.kraftwerk.core.exceptions.NullException;
import fr.insee.kraftwerk.core.rawdata.SurveyRawData;
import fr.insee.kraftwerk.core.utils.XmlFileReader;
import lombok.extern.log4j.Log4j2;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Elements;

@Log4j2
public class XMLReportingDataParser extends ReportingDataParser {
 
  private Document document;
  
  public void parseReportingData(ReportingData reportingData, SurveyRawData data) throws NullException {
    Path filePath = reportingData.getFilepath();
    readFile(filePath);
	Element root = null;
    try{
    	root = this.document.getRootElement();
    } catch (NullPointerException e) {
    	throw new NullException();
    }
    // Read information on each survey unit

    Element surveyUnitsNode = root.getFirstChildElement("SurveyUnits");
    Elements surveyUnitsNodeList = surveyUnitsNode.getChildElements("SurveyUnit");
	log.info("Read {} surveyUnit in file {}", surveyUnitsNodeList.size(),filePath);

    for (int i = 0; i < surveyUnitsNodeList.size(); i++) {
      Element surveyUnitElement = surveyUnitsNodeList.get(i);
      ReportingDataUE reportingDataUE = new ReportingDataUE();
      Element identifierElement = surveyUnitElement.getFirstChildElement("Id");
      String identifier = identifierElement.getValue();
      reportingDataUE.setIdentifier(identifier);
      Element interviewerIdentifierElement = surveyUnitElement.getFirstChildElement("InterviewerId");
      String interviewerIdentifier = interviewerIdentifierElement.getValue();
      reportingDataUE.setInterviewerId(interviewerIdentifier);
      Element organizationalUnitIdentifierElement = surveyUnitElement
        .getFirstChildElement("OrganizationalUnitId");
      // Get adress values
      String organizationalUnitIdentifier = organizationalUnitIdentifierElement.getValue();
      reportingDataUE.setOrganizationUnitId(organizationalUnitIdentifier);
      Element adressElement = surveyUnitElement.getFirstChildElement("InseeSampleIdentiers");
      reportingDataUE.setInseeSampleIdentifier(new InseeSampleIdentifier());
      reportingDataUE.getInseeSampleIdentifier().setRges(adressElement.getFirstChildElement("Rges").getValue());
      reportingDataUE.getInseeSampleIdentifier().setNumfa(adressElement.getFirstChildElement("Numfa").getValue());
      reportingDataUE.getInseeSampleIdentifier().setSsech(adressElement.getFirstChildElement("Ssech").getValue());
      reportingDataUE.getInseeSampleIdentifier().setLe(adressElement.getFirstChildElement("Le").getValue());
      reportingDataUE.getInseeSampleIdentifier().setEc(adressElement.getFirstChildElement("Ec").getValue());
      reportingDataUE.getInseeSampleIdentifier().setBs(adressElement.getFirstChildElement("Bs").getValue());
      reportingDataUE.getInseeSampleIdentifier().setNoi(adressElement.getFirstChildElement("Noi").getValue());
      Elements stateNodeList = surveyUnitElement.getFirstChildElement("States").getChildElements("State");
      for (int j = 0; j < stateNodeList.size(); j++) {
        Element stateElement = stateNodeList.get(j);
        String type = stateElement.getFirstChildElement("type").getValue().toUpperCase();
        String timestamp = stateElement.getFirstChildElement("date").getValue();
        reportingDataUE.addState(new State(type, Long.parseLong(timestamp)));
      } 
      reportingDataUE.sortStates();
      // Get outcome values
      Element contactOutcomeElement = surveyUnitElement.getFirstChildElement("ContactOutcome");
      reportingDataUE.setContactOutcome(new ContactOutcome());
      if (contactOutcomeElement != null) {
        reportingDataUE.getContactOutcome()
          .setOutcomeType(contactOutcomeElement.getFirstChildElement("outcomeType").getValue());
        reportingDataUE.getContactOutcome().setDateEndContact(
            Long.parseLong(contactOutcomeElement.getFirstChildElement("date").getValue()));
        reportingDataUE.getContactOutcome().setTotalNumberOfContactAttempts(
            Integer.parseInt(contactOutcomeElement.getFirstChildElement("totalNumberOfContactAttempts").getValue()));
      } 
      Elements contactAttemptsElements = surveyUnitElement.getFirstChildElement("ContactAttempts")
        .getChildElements("ContactAttempt");
      if (contactAttemptsElements != null)
        for (int k = 0; k < contactAttemptsElements.size(); k++) {
          Element contactAttemptsElement = contactAttemptsElements.get(k);
          String status = contactAttemptsElement.getFirstChildElement("status").getValue().toUpperCase();
          String timestamp = contactAttemptsElement.getFirstChildElement("date").getValue();
          reportingDataUE.addContactAttempts(new ContactAttempt(status, Long.parseLong(timestamp)));
        }  
      reportingData.addReportingDataUE(reportingDataUE);
    } 
    integrateReportingDataIntoUE(data, reportingData);
    this.document = null;
  }
  
  private void readFile(Path filePath) {
    XmlFileReader xmlFileReader = new XmlFileReader();
    this.document = xmlFileReader.readXmlFile(filePath);
    if (this.document != null) {
      log.info("Successfully parsed Coleman answers file: " + filePath);
    } else {
      log.warn("Failed to parse Coleman answers file: " + filePath);
    } 
  }
}
