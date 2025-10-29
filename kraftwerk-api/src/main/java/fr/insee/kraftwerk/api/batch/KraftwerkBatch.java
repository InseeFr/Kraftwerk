package fr.insee.kraftwerk.api.batch;

import fr.insee.kraftwerk.api.client.GenesisClient;
import fr.insee.kraftwerk.api.configuration.ConfigProperties;
import fr.insee.kraftwerk.api.configuration.MinioConfig;
import fr.insee.kraftwerk.api.configuration.VaultConfig;
import fr.insee.kraftwerk.api.process.MainProcessing;
import fr.insee.kraftwerk.api.process.MainProcessingGenesisLegacy;
import fr.insee.kraftwerk.api.services.KraftwerkService;
import fr.insee.kraftwerk.core.exceptions.KraftwerkException;
import fr.insee.kraftwerk.core.utils.KraftwerkExecutionContext;
import fr.insee.kraftwerk.core.utils.files.FileSystemImpl;
import fr.insee.kraftwerk.core.utils.files.FileUtilsInterface;
import fr.insee.kraftwerk.core.utils.files.MinioImpl;
import io.minio.MinioClient;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class KraftwerkBatch implements ApplicationRunner {

    ConfigProperties configProperties;
    MinioConfig minioConfig;
    FileUtilsInterface fileSystem;
    MinioClient minioClient;

    VaultConfig vaultConfig;

    //old JVM arguments list : "GENESIS false false TESTCAMPAIGN300000 3 2"
    //new JVM arguments list : "--service=GENESIS --campaignId=TESTCAMPAIGN300000 --archive=false --reporting-data=false --with-encryption=false --workers-nb=3 --worker-index=2"
    private static final String ARG_SERVICE = "service";
    private static final String ARG_ARCHIVE = "archive";
    private static final String ARG_REPORTING_DATA = "reporting-data";
    private static final String ARG_CAMPAIGNID = "campaignId";
    private static final String ARG_WORKERS_NB = "workers-nb";
    private static final String ARG_WORKER_INDEX = "worker-index";
    private static final String ARG_WITH_ENCRYPTION = "with-encryption";


    @Value("${fr.insee.postcollecte.files}")
    protected String defaultDirectory;

    @Value("${fr.insee.postcollecte.size-limit}")
    protected long limitSize;

    @Autowired
    public KraftwerkBatch(ConfigProperties configProperties, MinioConfig minioConfig, VaultConfig vaultConfig) {
        this.configProperties = configProperties;
        this.minioConfig = minioConfig;
        if(minioConfig.isEnable()){
            minioClient = MinioClient.builder().endpoint(minioConfig.getEndpoint()).credentials(minioConfig.getAccessKey(), minioConfig.getSecretKey()).build();
            fileSystem = new MinioImpl(minioClient, minioConfig.getBucketName());
        }else{
            fileSystem = new FileSystemImpl(configProperties.getDefaultDirectory());
        }
        this.vaultConfig = vaultConfig;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        log.info("KraftwerkBatch executed with options:");
        try {
            if (!args.getOptionNames().isEmpty()) {
                cliModeLaunched("Launching Kraftwerk in CLI mode...");

                args.getOptionNames().forEach(option ->
                        log.info("{} = {}", option, args.getOptionValues(option))
                );
                ArgsChecker.ArgsCheckerBuilder argsCheckerBuilder = ArgsChecker.builder();
                for(String option : args.getOptionNames()) {
                    switch(option) {
                        case ARG_SERVICE:
                            argsCheckerBuilder.argServiceName(getValue(option, args));
                            break;
                        case ARG_ARCHIVE:
                            argsCheckerBuilder.argIsArchive(getValue(option, args));
                            break;
                        case ARG_REPORTING_DATA:
                            argsCheckerBuilder.argIsReportingData(getValue(option, args));
                            break;
                        case ARG_CAMPAIGNID:
                            argsCheckerBuilder.argCampaignId(getValue(option, args));
                            break;
                        case ARG_WORKERS_NB:
                            argsCheckerBuilder.argWorkersNb(getValue(option, args));
                            break;
                        case ARG_WORKER_INDEX:
                            argsCheckerBuilder.argWorkerIndex(getValue(option, args));
                            break;
                        case ARG_WITH_ENCRYPTION:
                            argsCheckerBuilder.argWithEncryption(getValue(option, args));
                            break;
                        default:
                            log.warn("unknown option : {}", option);
                            break;
                    }
                }
                ArgsChecker argsChecker = argsCheckerBuilder.build();
                argsChecker.checkArgs();

                //Run kraftwerk
                KraftwerkExecutionContext kraftwerkExecutionContext = new KraftwerkExecutionContext(
                        argsChecker.getCampaignId(),
                        argsChecker.isFileByFile(),
                        argsChecker.isWithDDI(),
                        argsChecker.isWithEncryption(),
                        limitSize
                );

                switch (argsChecker.getServiceName()) {
                    case KraftwerkServiceType.GENESIS: {
                        MainProcessingGenesis mainProcessingGenesis = getMainProcessingGenesis(kraftwerkExecutionContext);
                        mainProcessingGenesis.runMain(argsChecker.getCampaignId(),1000,
                                argsChecker.getWorkersNb(), argsChecker.getWorkerIndex());
                    } break;
                    default: {
                        MainProcessing mainProcessing = getMainProcessing(kraftwerkExecutionContext);
                        mainProcessing.runMain();
                    }
                }

                //Archive
                if (argsChecker.isArchive()) {
                    KraftwerkService kraftwerkService = getKraftwerkService();
                    kraftwerkService.archive(argsChecker.getCampaignId(), fileSystem);
                }

                //NOTE : "System.exit(0);" prevents doing clean Unit Tests..
                return;

            }
        } catch (KraftwerkException ke) {
            log.error("Kraftwerk exception caught : Code {}, {}", ke.getStatus(), ke.getMessage());
            //NOTE : "System.exit(1);" prevents doing clean Unit Tests..
            return;
        }catch(Exception e){
            log.error(e.toString());
            //NOTE : "System.exit(1);" prevents doing clean Unit Tests..
            return;
        }
        log.info("Launching Kraftwerk in API mode...");
    }


    private static String getValue(String option, ApplicationArguments args) {
        return !args.getOptionValues(option).isEmpty() ? args.getOptionValues(option).getFirst() : null;
    }


    /**
     * Used for some checks on Unit Tests
     */
    void cliModeLaunched(String logString) {
        log.info(logString);
    }

    @NotNull MainProcessingGenesis getMainProcessingGenesis(KraftwerkExecutionContext kraftwerkExecutionContext) {
        return new MainProcessingGenesis(configProperties,
                new GenesisClient(new RestTemplateBuilder(), configProperties),
                fileSystem, kraftwerkExecutionContext);
    }


    @NotNull MainProcessing getMainProcessing(KraftwerkExecutionContext kraftwerkExecutionContext) {
        return new MainProcessing(kraftwerkExecutionContext, defaultDirectory, fileSystem);
    }


    @NotNull KraftwerkService getKraftwerkService() {
        return new KraftwerkService(configProperties, minioConfig);
    }


}
