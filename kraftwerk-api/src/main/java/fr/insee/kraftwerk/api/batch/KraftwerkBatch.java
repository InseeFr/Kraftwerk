package fr.insee.kraftwerk.api.batch;

import fr.insee.kraftwerk.api.configuration.ConfigProperties;
import fr.insee.kraftwerk.api.configuration.MinioConfig;
import fr.insee.kraftwerk.api.configuration.VaultConfig;
import fr.insee.kraftwerk.api.process.MainProcessing;
import fr.insee.kraftwerk.api.process.MainProcessingGenesis;
import fr.insee.kraftwerk.api.services.KraftwerkService;
import fr.insee.kraftwerk.core.utils.files.MinioImpl;
import io.minio.MinioClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class KraftwerkBatch implements CommandLineRunner {

    ConfigProperties configProperties;
    VaultConfig vaultConfig;
    MinioConfig minioConfig;
    MinioClient minioClient;

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
        }
        this.vaultConfig = vaultConfig;
    }

    @Override
    public void run(String... args) {
        try {
            //If .jar launched with cli args
            if (args.length > 0) {
                log.info("Launching Kraftwerk in CLI mode...");

                //Check arguments
                checkArgs(args);

                //Parse arguments
                //0. Service to use (MAIN,FILEBYFILE,GENESIS,LUNATIC_ONLY)
                //1. Archive at end of execution (false or true)
                //2. Integrate all reporting datas (false or true)
                //3. Campaign name
                KraftwerkServiceType kraftwerkServiceType = KraftwerkServiceType.valueOf(args[0]);
                boolean archiveAtEnd = Boolean.parseBoolean(args[1]);
                boolean withAllReportingData = Boolean.parseBoolean(args[2]);
                String inDirectory = args[3];

                //Kraftwerk service type related parameters
                boolean fileByFile = kraftwerkServiceType == KraftwerkServiceType.FILE_BY_FILE;
                boolean withDDI = kraftwerkServiceType != KraftwerkServiceType.LUNATIC_ONLY;
                if (kraftwerkServiceType != KraftwerkServiceType.MAIN) {
                    withAllReportingData = false;
                }
                if (kraftwerkServiceType == KraftwerkServiceType.GENESIS) {
                    archiveAtEnd = false;
                }


                //Run kraftwerk
                if (kraftwerkServiceType == KraftwerkServiceType.GENESIS) {
                    MainProcessingGenesis mainProcessingGenesis = new MainProcessingGenesis(configProperties,
                            new MinioImpl(minioClient, minioConfig.getBucketName()), vaultConfig);
                    mainProcessingGenesis.runMain(inDirectory,false, null); //TODO Use kraftwerk level encryption as
                    // batch
                    // parameter ?
                } else {
                    MainProcessing mainProcessing = new MainProcessing(inDirectory, fileByFile, withAllReportingData, withDDI, defaultDirectory, limitSize, new MinioImpl(minioClient, minioConfig.getBucketName()));
                    mainProcessing.runMain();
                }

                //Archive
                if (Boolean.TRUE.equals(archiveAtEnd)) {
                    KraftwerkService kraftwerkService = new KraftwerkService(minioConfig);
                    kraftwerkService.archive(inDirectory, new MinioImpl(minioClient, minioConfig.getBucketName()));
                }
                System.exit(0);
            }
        }catch(Exception e){
            log.error(e.toString());
            System.exit(1);
        }
        log.info("Launching Kraftwerk in API mode...");
    }

    /**
     * Throws a IllegalArgumentException if the arguments are not valid (ex: unparseable boolean)
     * KraftwerkServiceType is already checked by valueOf
     * @param args list of CLI arguments
     * @throws IllegalArgumentException if invalid argument
     */
    private static void checkArgs(String[] args) throws IllegalArgumentException{
        if(args.length != 4) {
            throw new IllegalArgumentException("Invalid number of arguments ! Got %s instead of 4 !".formatted(args.length));
        }
        if(!args[1].equals("true") && !args[1].equals("false")){
            throw new IllegalArgumentException("Invalid archiveAtEnd boolean argument ! : %s".formatted(args[1]));
        }
        if(!args[2].equals("true") && !args[2].equals("false")){
            throw new IllegalArgumentException("Invalid withAllReportingData boolean argument ! %s".formatted(args[2]));
        }
    }
}
