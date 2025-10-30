package fr.insee.kraftwerk.api.batch;

import lombok.Builder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;

@Builder
@Getter
@Slf4j
public class ArgsChecker {

    //String raw args
    private final String argServiceName;
    private final String argQuestionnaireId;
    private final String argIsReportingData;
    private final String argReportingDataFilePath;
    private final String argWithDDI;
    private final String argWithEncryption;
    private final String argSince;

    //Typed args
    private KraftwerkServiceType kraftwerkServiceType;
    private String questionnaireId;
    private boolean isReportingData;
    private String reportingDataFilePath;
    private boolean withEncryption;
    private boolean isFileByFile;
    private boolean withDDI;
    private LocalDateTime since;

    /**
     * Throws a IllegalArgumentException if the arguments are not valid (ex: unparseable boolean)
     * @throws IllegalArgumentException if invalid argument
     */
    void checkArgs() throws IllegalArgumentException{
        checkServiceName();
        checkQuestionnaireId();
        checkArgIsReportingData();
        checkArgWithEncryption();
        checkArgWithDDI();

        if(kraftwerkServiceType == KraftwerkServiceType.JSON){
            checkArgSince();
        }

        this.isFileByFile = this.kraftwerkServiceType == KraftwerkServiceType.FILE_BY_FILE;
    }

    private void checkServiceName() {
        try {
            this.kraftwerkServiceType = KraftwerkServiceType.valueOf(this.argServiceName);
        }catch (IllegalArgumentException iae) {
            throw new IllegalArgumentException("Invalid service argument ! : %s, must be one of %s".formatted(this.argServiceName, KraftwerkServiceType.values()));
        }
    }

    private void checkQuestionnaireId() {
        if(argQuestionnaireId != null){
            this.questionnaireId = argQuestionnaireId;
            return;
        }
        throw new IllegalArgumentException("No questionnaireId !");
    }

    private void checkArgIsReportingData() {
        //If "argIsReportingData" is not supplied in args, "isReportingData" is set to false by default
        if(this.argIsReportingData == null) {
            this.isReportingData = false;
            return;
        }
        if(isNotBoolean(this.argIsReportingData)){
            throw new IllegalArgumentException("Invalid reportingData boolean argument ! : %s".formatted(this.argIsReportingData));
        }
        this.isReportingData = Boolean.parseBoolean(this.argIsReportingData);
        if(this.argReportingDataFilePath == null){
            throw new IllegalArgumentException("No reporting data file argument provided !");
        }
        this.reportingDataFilePath = argReportingDataFilePath;
    }


    private void checkArgWithEncryption() {
        //If "argWithEncryption" is not supplied in args, "withEncryption" is set to false by default
        if(this.argWithEncryption == null) {
            this.withEncryption = false;
            return;
        }
        if(isNotBoolean(this.argWithEncryption)){
            throw new IllegalArgumentException("Invalid argWithEncryption boolean argument ! : %s".formatted(this.argWithEncryption));
        }
        this.withEncryption = Boolean.parseBoolean(this.argWithEncryption);
    }

    private void checkArgWithDDI() {
        //true by default
        if(this.argWithDDI == null) {
            this.withDDI = true;
            return;
        }
        if(isNotBoolean(this.argWithDDI)){
            throw new IllegalArgumentException("Invalid withDDI boolean argument ! : %s".formatted(this.argWithDDI));
        }
        this.withDDI = Boolean.parseBoolean(this.argWithDDI);
    }

    private void checkArgSince() {
        if(this.argSince != null){
            try {
                this.since = LocalDateTime.parse(argSince);
            }catch (DateTimeParseException e){
                log.error(e.toString());
                throw new IllegalArgumentException("Invalid since argument ! : %s, should be YYYY-MM-DDThh:mm:ss");
            }
        }
    }

    private static boolean isNotBoolean(String argToCheck){
        return !"true".equals(argToCheck) && !"false".equals(argToCheck);
    }
}