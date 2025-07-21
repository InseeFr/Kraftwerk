package fr.insee.kraftwerk.api.batch;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class ArgsChecker {

    private final String argServiceName;
    private final String argCampaignId;
    private final String argIsArchive;
    private final String argIsReportingData;
    private final String argWithEncryption;
    private final String argWorkersNb;
    private final String argWorkerIndex;

    private KraftwerkServiceType serviceName;
    private String campaignId;
    private boolean isArchive;
    private boolean isReportingData;
    private boolean withEncryption;
    private boolean isFileByFile;
    private boolean withDDI;
    private int workersNb;
    private int workerIndex;

    /**
     * Throws a IllegalArgumentException if the arguments are not valid (ex: unparseable boolean)
     * KraftwerkServiceType is already checked by "valueOf()"
     * @throws IllegalArgumentException if invalid argument
     */
    void checkArgs() throws IllegalArgumentException{
        checkArgNumber();
        checkServiceName();
        checkArgIsArchive();
        checkArgIsReportingData();
        checkArgWithEncryption();

        this.workersNb = toInteger(this.argWorkersNb, "argWorkersNb");
        this.workerIndex = toInteger(this.argWorkerIndex, "argWorkerIndex");

        checkWorkersConfig();

        //Kraftwerk service type related parameters
        this.isFileByFile = this.serviceName == KraftwerkServiceType.FILE_BY_FILE;
        this.withDDI = this.serviceName != KraftwerkServiceType.LUNATIC_ONLY;
        if (this.serviceName != KraftwerkServiceType.MAIN) {
            this.isReportingData = false;
        }
        if (this.serviceName == KraftwerkServiceType.GENESIS) {
            this.isArchive = false;
        }
    }


    void checkArgNumber() {
        //Note : We consider that if "argWorkersNb" or "argWorkerIndex" are not filled, their values are set to "1"
        if(this.argServiceName == null || this.argCampaignId == null ) {
            throw new IllegalArgumentException("Invalid number of arguments");
        }
        this.campaignId = this.argCampaignId;
    }

    void checkServiceName() {
        try {
            this.serviceName = KraftwerkServiceType.valueOf(this.argServiceName);
        }catch (IllegalArgumentException iae) {
            throw new IllegalArgumentException("Invalid argServiceName argument ! : %s".formatted(this.argServiceName));
        }
    }


    void checkArgIsArchive() {
        //If "argIsArchive" is not supplied in args, "isArchive" is set to false by default
        if(this.argIsArchive == null) {
            this.isArchive = false;
            return;
        }
        if(!isBoolean(this.argIsArchive)){
            throw new IllegalArgumentException("Invalid argIsArchive boolean argument ! : %s".formatted(this.argIsArchive));
        }
        this.isArchive = Boolean.parseBoolean(this.argIsArchive);
    }


    void checkArgIsReportingData() {
        //If "argIsReportingData" is not supplied in args, "isReportingData" is set to false by default
        if(this.argIsReportingData == null) {
            this.isReportingData = false;
            return;
        }
        if(!isBoolean(this.argIsReportingData)){
            throw new IllegalArgumentException("Invalid argIsReportingData boolean argument ! : %s".formatted(this.argIsReportingData));
        }
        this.isReportingData = Boolean.parseBoolean(this.argIsReportingData);
    }


    void checkArgWithEncryption() {
        //If "argWithEncryption" is not supplied in args, "withEncryption" is set to false by default
        if(this.argWithEncryption == null) {
            this.withEncryption = false;
            return;
        }
        if(!isBoolean(this.argWithEncryption)){
            throw new IllegalArgumentException("Invalid argWithEncryption boolean argument ! : %s".formatted(this.argWithEncryption));
        }
        this.withEncryption = Boolean.parseBoolean(this.argWithEncryption);
    }


    void checkWorkersConfig() {
        if(this.workersNb < 1 || this.workersNb > 10){
            throw new IllegalArgumentException("workers must be between 1 and 10 ! (got %s)".formatted(this.workersNb));
        }
        if(this.workerIndex < 1 || this.workerIndex > workersNb){
            throw new IllegalArgumentException("workerId cannot be > workers number, which is inconsistant ! (got %s for %s workers)".formatted(this.workerIndex, this.workersNb));
        }
    }


    private static boolean isBoolean(String argToCheck){
        return "true".equals(argToCheck) || "false".equals(argToCheck);
    }


    private static int toInteger(String argToCheck, String argName){
        if(argToCheck == null) {
            return 1;
        }
        try {
            return Integer.parseInt(argToCheck);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(String.format("arg (%s) cannot be parsed as an integer !", argName));
        }
    }

}
