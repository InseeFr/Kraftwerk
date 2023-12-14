package fr.insee.kraftwerk.core.extradata.reportingdata;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import fr.insee.kraftwerk.core.Constants;
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
  
  public void parseReportingData(ReportingData reportingData, SurveyRawData data, boolean withAllReportingData) throws NullException {
    Path filePath = reportingData.getFilepath();
    readFile(filePath);
	Element root;
    try{
    	root = this.document.getRootElement();
    } catch (NullPointerException e) {
    	throw new NullException();
    }
    // Read information on each survey unit
    List<Element> surveyUnitsNodeList = getSurveyUnitList(root);

	log.info("Read {} surveyUnit in file {}", surveyUnitsNodeList.size(),filePath);

    for (int i = 0; i < surveyUnitsNodeList.size(); i++) {
      Element surveyUnitElement = surveyUnitsNodeList.get(i);
      ReportingDataUE reportingDataUE = new ReportingDataUE();
      Element identifierElement = surveyUnitElement.getFirstChildElement("Id");
      String identifier = identifierElement.getValue();
      reportingDataUE.setIdentifier(identifier);

      Element interviewerIdentifierElement = surveyUnitElement.getFirstChildElement("InterviewerId");
      String interviewerIdentifier = Constants.REPORTING_DATA_INTERVIEWER_ID_NULL_PLACEHOLDER + identifier;
      if(interviewerIdentifierElement != null){
        interviewerIdentifier = interviewerIdentifierElement.getValue();
      }
      reportingDataUE.setInterviewerId(interviewerIdentifier);

      Element organizationalUnitIdentifierElement = surveyUnitElement
        .getFirstChildElement("OrganizationalUnitId");
      if(organizationalUnitIdentifierElement != null) {
        String organizationalUnitIdentifier = organizationalUnitIdentifierElement.getValue();
        reportingDataUE.setOrganizationUnitId(organizationalUnitIdentifier);
      }

      // Get address values
      getAddress(surveyUnitElement, reportingDataUE);

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

      Element contactAttemptsNode = surveyUnitElement.getFirstChildElement("ContactAttempts");
      if(contactAttemptsNode != null) {
        Elements contactAttemptsElements = contactAttemptsNode.getChildElements("ContactAttempt");
        for (int k = 0; k < contactAttemptsElements.size(); k++) {
          Element contactAttemptsElement = contactAttemptsElements.get(k);
          String status = contactAttemptsElement.getFirstChildElement("status").getValue().toUpperCase();
          String timestamp = contactAttemptsElement.getFirstChildElement("date").getValue();
          reportingDataUE.addContactAttempt(new ContactAttempt(status, Long.parseLong(timestamp)));
        }
      }

        //Get identification
      getIdentification(surveyUnitElement,reportingDataUE);

      //Get comments
      getComments(surveyUnitElement, reportingDataUE);

      reportingData.addReportingDataUE(reportingDataUE);
    }
    integrateReportingDataIntoUE(data, reportingData, withAllReportingData);
    this.document = null;
  }


  private void readFile(Path filePath) {
    XmlFileReader xmlFileReader = new XmlFileReader();
    this.document = xmlFileReader.readXmlFile(filePath);
    if (this.document != null) {
      log.info("Successfully parsed Coleman/Moog answers file: " + filePath);
    } else {
      log.warn("Failed to parse Coleman/Moog answers file: " + filePath);
    } 
  }

  private List<Element> getSurveyUnitList(Element root){
    List<Element> surveyUnitList = new ArrayList<>();

    Element partitioningsNode = root.getFirstChildElement("Partitionings");
    if(partitioningsNode == null){
      // Survey unit list directly in root
      Element surveyUnitsNode = root.getFirstChildElement("SurveyUnits");
      for(Element surveyUnitElement : surveyUnitsNode.getChildElements("SurveyUnit"))
        surveyUnitList.add(surveyUnitElement);
    }else{
      //  Survey unit list divided into partitionings
      for(Element partitioningElement : partitioningsNode.getChildElements("Partitioning")){
        Element surveyUnitsNode = partitioningElement.getFirstChildElement("SurveyUnits");
        for(Element surveyUnitElement : surveyUnitsNode.getChildElements("SurveyUnit"))
          surveyUnitList.add(surveyUnitElement);
      }
    }

    return surveyUnitList;
  }

  private void getAddress(Element surveyUnitElement, ReportingDataUE reportingDataUE) {
    Element addressElement = surveyUnitElement.getFirstChildElement("InseeSampleIdentiers");

    if(addressElement != null) {
      reportingDataUE.setInseeSampleIdentifier(new InseeSampleIdentifier());
      reportingDataUE.getInseeSampleIdentifier().setRges(addressElement.getFirstChildElement("Rges").getValue());
      reportingDataUE.getInseeSampleIdentifier().setNumfa(addressElement.getFirstChildElement("Numfa").getValue());
      reportingDataUE.getInseeSampleIdentifier().setSsech(addressElement.getFirstChildElement("Ssech").getValue());
      reportingDataUE.getInseeSampleIdentifier().setLe(addressElement.getFirstChildElement("Le").getValue());
      reportingDataUE.getInseeSampleIdentifier().setEc(addressElement.getFirstChildElement("Ec").getValue());
      reportingDataUE.getInseeSampleIdentifier().setBs(addressElement.getFirstChildElement("Bs").getValue());
      reportingDataUE.getInseeSampleIdentifier().setNoi(addressElement.getFirstChildElement("Noi").getValue());
    }
  }

  private void getIdentification(Element surveyUnitElement, ReportingDataUE reportingDataUE) {
    Element identificationElement = surveyUnitElement.getFirstChildElement("Identification");
    reportingDataUE.setIdentification(new Identification());
    if (identificationElement != null) {
      if (identificationElement.getFirstChildElement(Constants.IDENTIFICATION_NAME) != null)
        reportingDataUE.getIdentification().setIdentification(identificationElement.getFirstChildElement(Constants.IDENTIFICATION_NAME).getValue());

      if (identificationElement.getFirstChildElement(Constants.ACCESS_NAME) != null)
        reportingDataUE.getIdentification().setAccess(identificationElement.getFirstChildElement(Constants.ACCESS_NAME).getValue());

      if (identificationElement.getFirstChildElement(Constants.SITUATION_NAME) != null)
        reportingDataUE.getIdentification().setSituation(identificationElement.getFirstChildElement(Constants.SITUATION_NAME).getValue());

      if (identificationElement.getFirstChildElement(Constants.CATEGORY_NAME) != null)
        reportingDataUE.getIdentification().setCategory(identificationElement.getFirstChildElement(Constants.CATEGORY_NAME).getValue());

      if (identificationElement.getFirstChildElement(Constants.OCCUPANT_NAME) != null)
        reportingDataUE.getIdentification().setOccupant(identificationElement.getFirstChildElement(Constants.OCCUPANT_NAME).getValue());
    }
  }

  private void getComments(Element surveyUnitElement, ReportingDataUE reportingDataUE) {
    Element commentsNode = surveyUnitElement.getFirstChildElement("Comments");
    if(commentsNode != null) {
      Elements commentsElements = commentsNode.getChildElements("Comment");
      for (int k = 0; k < commentsElements.size(); k++) {
        Element commentsElement = commentsElements.get(k);
        String status = commentsElement.getFirstChildElement("type").getValue();
        String value = commentsElement.getFirstChildElement("value").getValue();
        reportingDataUE.addComment(new Comment(status, value));
      }
    }
  }

}
