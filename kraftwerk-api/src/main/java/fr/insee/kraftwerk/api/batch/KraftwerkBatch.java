package fr.insee.kraftwerk.api.batch;

import fr.insee.kraftwerk.api.configuration.ConfigProperties;
import fr.insee.kraftwerk.api.configuration.MinioConfig;
import fr.insee.kraftwerk.api.process.MainProcessing;
import fr.insee.kraftwerk.api.process.MainProcessingGenesis;
import fr.insee.kraftwerk.api.services.KraftwerkService;
import fr.insee.kraftwerk.core.exceptions.KraftwerkException;
import fr.insee.kraftwerk.core.utils.files.FileSystemImpl;
import fr.insee.kraftwerk.core.utils.files.FileUtilsInterface;
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
    MinioConfig minioConfig;
    FileUtilsInterface fileSystem;
    MinioClient minioClient;

    @Value("${fr.insee.postcollecte.files}")
    protected String defaultDirectory;

    @Value("${fr.insee.postcollecte.size-limit}")
    protected long limitSize;

    @Autowired
    public KraftwerkBatch(ConfigProperties configProperties, MinioConfig minioConfig) {
        this.configProperties = configProperties;
        this.minioConfig = minioConfig;
        if(minioConfig.isEnable()){
            minioClient = MinioClient.builder().endpoint(minioConfig.getEndpoint()).credentials(minioConfig.getAccessKey(), minioConfig.getSecretKey()).build();
            fileSystem = new MinioImpl(minioClient, minioConfig.getBucketName());
        }else{
            fileSystem = new FileSystemImpl(configProperties.getDefaultDirectory());
        }
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
                //(only for "runMainV2") 4. workers Number (NOTE : "1" by default if NOT PROVIDED)
                //(only for "runMainV2") 5. worker id (NOTE : "1" by default if NOT PROVIDED)
                //4. (or 6. for V2) Authentication token for Genesis
                KraftwerkServiceType kraftwerkServiceType = KraftwerkServiceType.valueOf(args[0]);
                boolean archiveAtEnd = Boolean.parseBoolean(args[1]);
                boolean withAllReportingData = Boolean.parseBoolean(args[2]);
                String inDirectory = args[3];
                //========= OPTIMISATIONS PERFS (START) ==========
                int workersNumber = 1; //initialisation to 1 by default
                int workerId = 1;  //initialisation to 1 by default
                if(args.length == 6) {
                    workersNumber = Integer.parseInt(args[4]); //note : checks have been previously made inside "checkArgs(args)"
                    workerId = Integer.parseInt(args[5]); //note : checks have been previously made inside "checkArgs(args)"
                }
                //========= OPTIMISATIONS PERFS (END) ==========

                //Kraftwerk service type related parameters
                boolean fileByFile = kraftwerkServiceType == KraftwerkServiceType.FILE_BY_FILE;
                boolean withDDI = kraftwerkServiceType != KraftwerkServiceType.LUNATIC_ONLY;
                if (kraftwerkServiceType != KraftwerkServiceType.MAIN) {
                    withAllReportingData = false;
                }
                if (kraftwerkServiceType == KraftwerkServiceType.GENESIS) {
                    archiveAtEnd = false;
                }
                //========= OPTIMISATIONS PERFS (START) ==========
                if (kraftwerkServiceType == KraftwerkServiceType.GENESISV2) {
                    archiveAtEnd = false;
                }
                //========= OPTIMISATIONS PERFS (END) ==========


                //Run kraftwerk
                if (kraftwerkServiceType == KraftwerkServiceType.GENESIS) {
                    MainProcessingGenesis mainProcessingGenesis = new MainProcessingGenesis(
                            configProperties,
                            fileSystem,
                            true);
                    mainProcessingGenesis.runMain(inDirectory,1000);
                }
                //========= OPTIMISATIONS PERFS (START) ==========
                else if (kraftwerkServiceType == KraftwerkServiceType.GENESISV2) {
                    MainProcessingGenesis mainProcessingGenesis = new MainProcessingGenesis(
                            configProperties,
                            fileSystem,
                            true);
                    mainProcessingGenesis.runMainV2(inDirectory,1000, workersNumber, workerId);
                }
                //========= OPTIMISATIONS PERFS (END) ==========
                else {
                    MainProcessing mainProcessing = new MainProcessing(
                            inDirectory,
                            fileByFile,
                            withAllReportingData,
                            withDDI,
                            defaultDirectory,
                            limitSize,
                            fileSystem);
                    mainProcessing.runMain();
                }

                //Archive
                if (Boolean.TRUE.equals(archiveAtEnd)) {
                    KraftwerkService kraftwerkService = new KraftwerkService(configProperties, minioConfig);
                    kraftwerkService.archive(inDirectory, fileSystem);
                }
                System.exit(0);
            }
        }catch (KraftwerkException ke) {
            log.error("Kraftwerk exception caught : Code {}, {}", ke.getStatus(), ke.getMessage());
            System.exit(1);
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
        //========= OPTIMISATIONS PERFS (START) ==========
        //We provide retro-compatibility, and authorize old (runMain) & new (runMainV2) process
        if(args.length != 4 && args.length != 6) {
            throw new IllegalArgumentException("Invalid number of arguments ! Got %s instead of 4 or 6 !".formatted(args.length));
        }
        //========= OPTIMISATIONS PERFS (START) ==========
        if(!args[1].equals("true") && !args[1].equals("false")){
            throw new IllegalArgumentException("Invalid archiveAtEnd boolean argument ! : %s".formatted(args[1]));
        }
        if(!args[2].equals("true") && !args[2].equals("false")){
            throw new IllegalArgumentException("Invalid withAllReportingData boolean argument ! %s".formatted(args[2]));
        }
        //========= OPTIMISATIONS PERFS (START) ==========
        if(args.length == 6) {
            int workersNb = 0;
            int workerIndex = 0;
            try {
                workersNb = Integer.parseInt(args[4]);
                workerIndex = Integer.parseInt(args[5]);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException(String.format("workersNb (%s) and/or workerIndex (%s) cannot be parsed as an integer !", args[4], args[5]));
            }
            if(workersNb < 1 || workersNb > 10){
                throw new IllegalArgumentException("Maximum number of workers is 10 ! (got %s)".formatted(args[4]));
            }
            if(workerIndex < 1 || workerIndex > workersNb){
                throw new IllegalArgumentException("workerId cannot be > workers number, which is inconsistant ! (got %s)".formatted(args[5]));
            }
        }
        //========= OPTIMISATIONS PERFS (END) ==========
    }
}
