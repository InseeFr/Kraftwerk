package fr.insee.kraftwerk.api.batch;

import fr.insee.kraftwerk.api.configuration.ConfigProperties;
import fr.insee.kraftwerk.api.configuration.MinioConfig;
import fr.insee.kraftwerk.api.configuration.VaultConfig;
import fr.insee.kraftwerk.api.services.MainService;
import fr.insee.kraftwerk.api.services.ReportingDataService;
import fr.insee.kraftwerk.core.utils.files.FileSystemImpl;
import fr.insee.kraftwerk.core.utils.files.FileUtilsInterface;
import fr.insee.kraftwerk.core.utils.files.MinioImpl;
import io.minio.MinioClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.env.Environment;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class KraftwerkBatch implements ApplicationRunner {
    ConfigProperties configProperties;
    MinioConfig minioConfig;
    FileUtilsInterface fileSystem;
    MinioClient minioClient;

    VaultConfig vaultConfig;
    ReportingDataService reportingDataService;
    MainService mainService;

    @Value("${fr.insee.postcollecte.files}")
    protected String defaultDirectory;

    private final Environment environment;

    //Arguments names (ex: --service=GENESIS)
    private static final String ARG_SERVICE = "service";
    private static final String ARG_WITH_DDI = "with-ddi";
    private static final String ARG_REPORTING_DATA = "reporting-data";
    private static final String ARG_REPORTING_DATA_FILE = "reporting-data-file-path";
    private static final String ARG_QUESTIONNAIREID = "questionnaireId";
    private static final String ARG_WITH_ENCRYPTION = "with-encryption";
    private static final String ARG_SINCE = "extract-json-since";

    public static final int BATCH_SIZE = 1000;

    private static final String ERROR_MESSAGE = "Kraftwerk service returned code %d with body %s";

    @Autowired
    public KraftwerkBatch(ConfigProperties configProperties,
                          MinioConfig minioConfig,
                          VaultConfig vaultConfig,
                          ReportingDataService reportingDataService,
                          MainService mainService,
                          Environment environment
    ){
        this.configProperties = configProperties;
        this.minioConfig = minioConfig;
        if(minioConfig.isEnable()){
            minioClient = MinioClient.builder().endpoint(minioConfig.getEndpoint()).credentials(minioConfig.getAccessKey(), minioConfig.getSecretKey()).build();
            fileSystem = new MinioImpl(minioClient, minioConfig.getBucketName());
        }else{
            fileSystem = new FileSystemImpl(configProperties.getDefaultDirectory());
        }
        this.vaultConfig = vaultConfig;
        this.reportingDataService = reportingDataService;
        this.mainService = mainService;
        this.environment = environment;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (!args.getOptionNames().isEmpty()) {
            try{
                ResponseEntity<Object> kraftwerkResponse = runBatchMode(args);
                if( kraftwerkResponse != null
                        && !kraftwerkResponse.getStatusCode().is2xxSuccessful()){
                    log.error(ERROR_MESSAGE.formatted(
                            kraftwerkResponse.getStatusCode().value(),
                            kraftwerkResponse.getBody()
                    ));
                    System.exit(1);
                }
                return;
            } catch (Exception e){
                if (!isTestEnvironment()) {
                    log.error(e.toString());
                    System.exit(1);
                }
                throw e;
            }
        }
        log.info("Launching Kraftwerk in API mode...");
    }

    private ResponseEntity<Object> runBatchMode(ApplicationArguments args){
        log.info("Launching Kraftwerk in CLI mode...");

        args.getOptionNames().forEach(option ->
                log.info("{} = {}", option, args.getOptionValues(option))
        );
        ArgsChecker argsChecker = getArgsChecker(args);
        argsChecker.checkArgs();

        //Run kraftwerk
        //Reporting data service
        if(argsChecker.isReportingData()){
            return launchReportingDataService(argsChecker);
        }
        //Main service
        if(argsChecker.isWithDDI()){
            return launchMainServiceWithDDI(argsChecker);
        }
        return launchMainServiceWithoutDDI(argsChecker);
    }

    private static ArgsChecker getArgsChecker(ApplicationArguments args) {
        ArgsChecker.ArgsCheckerBuilder argsCheckerBuilder = ArgsChecker.builder();
        for(String option : args.getOptionNames()) {
            switch(option) {
                case ARG_SERVICE:
                    argsCheckerBuilder.argServiceName(getOptionValue(option, args));
                    break;
                case ARG_REPORTING_DATA:
                    argsCheckerBuilder.argIsReportingData(getOptionValue(option, args));
                    break;
                case ARG_REPORTING_DATA_FILE:
                    argsCheckerBuilder.argReportingDataFilePath(getOptionValue(option, args));
                    break;
                case ARG_QUESTIONNAIREID:
                    argsCheckerBuilder.argQuestionnaireId(getOptionValue(option, args));
                    break;
                case ARG_WITH_ENCRYPTION:
                    argsCheckerBuilder.argWithEncryption(getOptionValue(option, args));
                    break;
                case ARG_WITH_DDI:
                    argsCheckerBuilder.argWithDDI(getOptionValue(option, args));
                    break;
                case ARG_SINCE:
                    argsCheckerBuilder.argSince(getOptionValue(option, args));
                    break;
                default:
                    log.warn("unknown option : {}", option);
                    break;
            }
        }
        return argsCheckerBuilder.build();
    }

    private static String getOptionValue(String option, ApplicationArguments args) {
        return !args.getOptionValues(option).isEmpty() ? args.getOptionValues(option).getFirst() : null;
    }

    private ResponseEntity<Object> launchMainServiceWithDDI(ArgsChecker argsChecker) {
        ResponseEntity<String> response = null;
        switch (argsChecker.getKraftwerkServiceType()) {
            case GENESIS -> response = mainService.mainGenesisByQuestionnaireId(
                        argsChecker.getQuestionnaireId(),
                        null,
                        1000,
                        argsChecker.isWithEncryption()
                );
            case JSON -> {
                return mainService.jsonExtraction(
                            argsChecker.getQuestionnaireId(),
                            null,
                            1000,
                            argsChecker.getSince()
                    );
            }
            case MAIN -> response = mainService.mainService(
                        argsChecker.getQuestionnaireId(),
                        false,
                        argsChecker.isWithEncryption()
            );
            case FILE_BY_FILE -> response = mainService.mainFileByFile(
                    argsChecker.getArgQuestionnaireId(),
                    false,
                    argsChecker.isWithEncryption()
            );
        }
        return getObjectResponseEntity(response);
    }

    private ResponseEntity<Object> launchMainServiceWithoutDDI(ArgsChecker argsChecker) {
        ResponseEntity<String> response = null;
        switch (argsChecker.getKraftwerkServiceType()) {
            case GENESIS -> response = mainService.mainGenesisLunaticOnlyByQuestionnaire(
                    argsChecker.getQuestionnaireId(),
                    null,
                    BATCH_SIZE,
                    argsChecker.isWithEncryption()
            );
            case MAIN -> response = mainService.mainService(
                    argsChecker.getQuestionnaireId(),
                    false,
                    argsChecker.isWithEncryption()
            );
            case JSON, FILE_BY_FILE -> {
                return launchMainServiceWithDDI(argsChecker);
            }
        }
        return getObjectResponseEntity(response);
    }

    private ResponseEntity<Object> launchReportingDataService(ArgsChecker argsChecker) {
        ResponseEntity<String> response = null;
        if (argsChecker.getKraftwerkServiceType() == KraftwerkServiceType.MAIN) {
            response = reportingDataService.processReportingData(
                    argsChecker.getQuestionnaireId(),
                    argsChecker.getReportingDataFilePath()
            );
        }
        if (argsChecker.getKraftwerkServiceType() == KraftwerkServiceType.GENESIS) {
            response = reportingDataService.processReportingDataGenesis(
                    argsChecker.getQuestionnaireId(),
                    argsChecker.getReportingDataFilePath(),
                    null
            );
        }
        return getObjectResponseEntity(response);
    }

    private ResponseEntity<Object> getObjectResponseEntity(ResponseEntity<String> response) {
        if(isTestEnvironment()){ //Mock
            return ResponseEntity.ok().build();
        }
        if(response != null){
            return ResponseEntity.status(response.getStatusCode()).body(response.getBody());
        }
        return ResponseEntity.internalServerError().body("Batch mode : null response");
    }

    private boolean isTestEnvironment() {
        for (String profile : environment.getActiveProfiles()) {
            if ("test".equalsIgnoreCase(profile)) {
                return true;
            }
        }
        return false;
    }
}