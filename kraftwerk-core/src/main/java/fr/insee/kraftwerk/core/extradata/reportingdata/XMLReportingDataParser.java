package fr.insee.kraftwerk.core.extradata.reportingdata;

import fr.insee.kraftwerk.core.Constants;
import fr.insee.kraftwerk.core.exceptions.KraftwerkException;
import fr.insee.kraftwerk.core.rawdata.SurveyRawData;
import fr.insee.kraftwerk.core.utils.files.FileUtilsInterface;
import fr.insee.kraftwerk.core.utils.xml.XmlFileReader;
import lombok.extern.log4j.Log4j2;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Elements;

import java.nio.file.Path;
import java.time.DateTimeException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

@Log4j2
public class XMLReportingDataParser extends ReportingDataParser {

    private Document document;

    private static final String EMPTY_TAG = "___EMPTY___";

    public XMLReportingDataParser(FileUtilsInterface fileUtilsInterface) {
        super(fileUtilsInterface);
    }

    public void parseReportingData(ReportingData reportingData, SurveyRawData data, boolean withAllReportingData) throws KraftwerkException {
        Path filePath = reportingData.getFilepath();
        readFile(filePath);
        Element root;
        try {
            root = this.document.getRootElement();
        } catch (NullPointerException e) {
            throw new KraftwerkException(500,"Reporting data file not found");
        }

        String identificationConfiguration = getElementValueIfExists(root, "IdentificationConfiguration");

        // Read information on each survey unit
        List<Element> surveyUnitsNodeList = getSurveyUnitList(root);

        log.info("Read {} surveyUnit in file {}", surveyUnitsNodeList.size(), filePath);

        for (Element surveyUnitElement : surveyUnitsNodeList) {
            ReportingDataUE reportingDataUE = new ReportingDataUE();
            String identifier = getElementValueIfExists(surveyUnitElement, "Id");
            reportingDataUE.setIdentifier(identifier);

            reportingDataUE.setIdentificationConfiguration(identificationConfiguration);

            Element interviewerIdentifierElement = surveyUnitElement.getFirstChildElement("InterviewerId");
            String interviewerIdentifier = Constants.REPORTING_DATA_INTERVIEWER_ID_NULL_PLACEHOLDER + identifier;
            if (interviewerIdentifierElement != null) {
                interviewerIdentifier = interviewerIdentifierElement.getValue();
            }
            reportingDataUE.setInterviewerId(interviewerIdentifier);

            Element organizationalUnitIdentifierElement = surveyUnitElement
                    .getFirstChildElement("OrganizationalUnitId");
            if (organizationalUnitIdentifierElement != null) {
                String organizationalUnitIdentifier = organizationalUnitIdentifierElement.getValue();
                reportingDataUE.setOrganizationUnitId(organizationalUnitIdentifier);
            }

            // Get address values
            getAddress(surveyUnitElement, reportingDataUE);

            Element statesNode = surveyUnitElement.getFirstChildElement("States");
            Elements stateNodeList = (statesNode == null)
                    ? surveyUnitElement.getChildElements(EMPTY_TAG)
                    : statesNode.getChildElements("State");

            for (int j = 0; j < stateNodeList.size(); j++) {
                Element stateElement = stateNodeList.get(j);
                String type = getElementValueIfExists(stateElement,"type");
                String timestamp = getElementValueIfExists(stateElement,"date");
                if (!type.isEmpty() && !timestamp.isEmpty()) {
                    try {
                        reportingDataUE.addState(new State(type.toUpperCase(), Long.parseLong(timestamp)));
                    } catch (NumberFormatException ignored) {
                    }
                }
            }
            reportingDataUE.sortStates();

            // Get outcome values
            Element contactOutcomeElement = surveyUnitElement.getFirstChildElement("ContactOutcome");
            reportingDataUE.setContactOutcome(new ContactOutcome());
            if (contactOutcomeElement != null) {
                reportingDataUE.getContactOutcome()
                        .setOutcomeType(getElementValueIfExists(contactOutcomeElement, "outcomeType"));

                String date = getElementValueIfExists(contactOutcomeElement, "date");
                if (!date.isEmpty()) {
                    try { reportingDataUE.getContactOutcome().setDateEndContact(Long.parseLong(date)); }
                    catch (NumberFormatException ignored) {}
                }

                String attempts = getElementValueIfExists(contactOutcomeElement, "totalNumberOfContactAttempts");
                if (!attempts.isEmpty()) {
                    try { reportingDataUE.getContactOutcome().setTotalNumberOfContactAttempts(Integer.parseInt(attempts)); }
                    catch (NumberFormatException ignored) {}
                }
            }

            Element contactAttemptsNode = surveyUnitElement.getFirstChildElement("ContactAttempts");
            if (contactAttemptsNode != null) {
                Elements contactAttemptsElements = contactAttemptsNode.getChildElements("ContactAttempt");
                for (int k = 0; k < contactAttemptsElements.size(); k++) {
                    Element contactAttemptsElement = contactAttemptsElements.get(k);
                    String status = getElementValueIfExists(contactAttemptsElement, "status");
                    String timestamp = getElementValueIfExists(contactAttemptsElement, "date");

                    if (!status.isEmpty() && !timestamp.isEmpty()) {
                        try {
                            reportingDataUE.addContactAttempt(new ContactAttempt(status.toUpperCase(), Long.parseLong(timestamp)));
                        } catch (NumberFormatException ignored) {
                        }
                    }
                }
            }

            //Get identification
            getIdentification(surveyUnitElement, reportingDataUE);

            //Get comments
            getComments(surveyUnitElement, reportingDataUE);

            //Get survey validation date from states
            setSurveyValidationDate(reportingDataUE);

            //Get closing cause
            getClosingCause(surveyUnitElement, reportingDataUE);

            reportingData.addReportingDataUE(reportingDataUE);
        }
        integrateReportingDataIntoUE(data, reportingData, withAllReportingData, fileUtilsInterface);
        this.document = null;
    }


    private void readFile(Path filePath) {
        XmlFileReader xmlFileReader = new XmlFileReader(fileUtilsInterface);
        this.document = xmlFileReader.readXmlFile(filePath);
        if (this.document != null) {
            log.info("Successfully parsed Coleman/Moog answers file: {}", filePath);
        } else {
            log.warn("Failed to parse Coleman/Moog answers file: {}", filePath);
        }
    }

    private List<Element> getSurveyUnitList(Element root) {
        List<Element> surveyUnitList = new ArrayList<>();

        Element partitioningsNode = root.getFirstChildElement("Partitionings");
        if (partitioningsNode == null) {
            // Survey unit list directly in root
            getSurveyUnitsFromElement(root, surveyUnitList);
        } else {
            //  Survey unit list divided into partitionings
            for (Element partitioningElement : partitioningsNode.getChildElements("Partitioning")) {
                getSurveyUnitsFromElement(partitioningElement, surveyUnitList);
            }
        }

        return surveyUnitList;
    }

    private void getSurveyUnitsFromElement(Element element, List<Element> surveyUnitList) {
        Element surveyUnitsNode = element.getFirstChildElement("SurveyUnits");
        if (surveyUnitsNode == null) return;
        for (Element surveyUnitElement : surveyUnitsNode.getChildElements("SurveyUnit")) {
            surveyUnitList.add(surveyUnitElement);
        }
    }

    private void getAddress(Element surveyUnitElement, ReportingDataUE reportingDataUE) {
        Element addressElement = surveyUnitElement.getFirstChildElement("InseeSampleIdentiers");

        if (addressElement != null) {
            reportingDataUE.setInseeSampleIdentifier(new InseeSampleIdentifier());
            reportingDataUE.getInseeSampleIdentifier().setRges(getElementValueIfExists(addressElement,"Rges"));
            reportingDataUE.getInseeSampleIdentifier().setNumfa(getElementValueIfExists(addressElement,"Numfa"));
            reportingDataUE.getInseeSampleIdentifier().setSsech(getElementValueIfExists(addressElement,"Ssech"));
            reportingDataUE.getInseeSampleIdentifier().setLe(getElementValueIfExists(addressElement,"Le"));
            reportingDataUE.getInseeSampleIdentifier().setEc(getElementValueIfExists(addressElement,"Ec"));
            reportingDataUE.getInseeSampleIdentifier().setBs(getElementValueIfExists(addressElement,"Bs"));
            reportingDataUE.getInseeSampleIdentifier().setNoi(getElementValueIfExists(addressElement,"Noi"));
            reportingDataUE.getInseeSampleIdentifier().setNograp(getElementValueIfExists(addressElement, "Nograp"));
            reportingDataUE.getInseeSampleIdentifier().setNolog(getElementValueIfExists(addressElement, "Nolog"));
            reportingDataUE.getInseeSampleIdentifier().setNole(getElementValueIfExists(addressElement, "Nole"));
            reportingDataUE.getInseeSampleIdentifier().setAutre(getElementValueIfExists(addressElement, "Autre"));
        }
    }

    private void getIdentification(Element surveyUnitElement, ReportingDataUE reportingDataUE) {
        Element identificationElement = surveyUnitElement.getFirstChildElement("Identification");
        reportingDataUE.setIdentification(new ReportingIdentification());
        if (identificationElement == null) {
            return;
        }

        reportingDataUE.getIdentification().setIdentification(getElementValueIfExists(identificationElement, "identification"));
        reportingDataUE.getIdentification().setAccess(getElementValueIfExists(identificationElement, "access"));
        reportingDataUE.getIdentification().setSituation(getElementValueIfExists(identificationElement, "situation"));
        reportingDataUE.getIdentification().setOccupant(getElementValueIfExists(identificationElement, "occupant"));
        reportingDataUE.getIdentification().setCategory(getElementValueIfExists(identificationElement, "category"));
        reportingDataUE.getIdentification().setIndividualStatus(getElementValueIfExists(identificationElement, "individualStatus"));
        reportingDataUE.getIdentification().setInterviewerCanProcess(getElementValueIfExists(identificationElement, "interviewerCanProcess"));

    }

    private void getComments(Element surveyUnitElement, ReportingDataUE reportingDataUE) {
        Element commentsNode = surveyUnitElement.getFirstChildElement("Comments");
        if (commentsNode == null) {
            return;
        }

        Elements commentsElements = commentsNode.getChildElements("Comment");
        for (int k = 0; k < commentsElements.size(); k++) {
            Element commentsElement = commentsElements.get(k);
            String status = getElementValueIfExists(commentsElement, "type");
            String value = getElementValueIfExists(commentsElement, "value");
            reportingDataUE.addComment(new Comment(status, value));
        }
    }

    private void setSurveyValidationDate(ReportingDataUE reportingDataUE) {
        if (reportingDataUE.getStates().isEmpty()) {
            return;
        }

        State validationState = null;
        for (State contactState : reportingDataUE.getStates()) {
            if (contactState.isValidationState() && contactState.isPriorTo(validationState)) {
                validationState = contactState;
            }
        }
        if (validationState != null)
            reportingDataUE.setSurveyValidationDateTimeStamp(validationState.getTimestamp());
    }

    private void getClosingCause(Element surveyUnitElement, ReportingDataUE reportingDataUE) {
        Element closingCauseNode = surveyUnitElement.getFirstChildElement("ClosingCause");
        if (closingCauseNode == null) {
            return;
        }

        ReportingDataClosingCause reportingDataClosingCause = new ReportingDataClosingCause();

        //Type
        Element typeNode = closingCauseNode.getFirstChildElement("type");
        if(typeNode != null){
            try{
                reportingDataClosingCause.setClosingCauseValue(
                        ClosingCauseValue.valueOf(typeNode.getValue())
                );
            }catch (IllegalArgumentException e){
                log.warn(
                        "Invalid closing cause type for interrogation {} ! " +
                        "Found {} instead of one of {}",
                    reportingDataUE.getIdentifier(), typeNode.getValue(), ClosingCauseValue.values()
                );
            }
        }

        //Date
        //Expects a Long Unix milliseconds timestamp
        Element dateNode = closingCauseNode.getFirstChildElement("date");
        if (dateNode != null) {
            try {
                reportingDataClosingCause.setClosingCauseDate(
                        LocalDateTime.ofInstant(
                                Instant.ofEpochMilli(
                                        Long.parseLong(dateNode.getValue())
                                ),
                                ZoneId.of("UTC")
                        )
                );
            }catch (NumberFormatException nfe){
                log.warn("Invalid closing cause date format for interrogation {} !" +
                        "Found {} while it requires a UNIX millisecond timestamp",
                        reportingDataUE.getIdentifier(), dateNode.getValue()
                        );
            }
            catch (DateTimeException e){
                log.warn("Error during closing cause date for interrogation {} ! " + e,
                        reportingDataUE.getIdentifier()
                        );
            }
        }
        reportingDataUE.setReportingDataClosingCause(reportingDataClosingCause);
    }

    private static String getElementValueIfExists(Element parent, String childName) {
        if (parent == null) return null;
        Element child = parent.getFirstChildElement(childName);
        if (child == null) return null;
        String value = child.getValue();
        if (value == null) return null;
        value = value.trim();
        return value.isEmpty() ? null : value;
    }
}
