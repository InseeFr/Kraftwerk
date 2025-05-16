package fr.insee.kraftwerk.api.services;

import fr.insee.kraftwerk.api.configuration.ConfigProperties;
import fr.insee.kraftwerk.api.configuration.MinioConfig;
import fr.insee.kraftwerk.api.process.FolderSystem;
import fr.insee.kraftwerk.api.process.ReportingDataProcessing;
import fr.insee.kraftwerk.core.data.model.Mode;
import fr.insee.kraftwerk.core.exceptions.KraftwerkException;
import fr.insee.kraftwerk.core.utils.files.FileSystemImpl;
import fr.insee.kraftwerk.core.utils.files.FileUtilsInterface;
import fr.insee.kraftwerk.core.utils.files.MinioImpl;
import io.minio.MinioClient;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@Tag(name = "${tag.reportingdata}")
public class ReportingDataService extends KraftwerkService{
    ConfigProperties configProperties;
    FileUtilsInterface fileUtilsInterface;

    @Value("${fr.insee.postcollecte.files}")
    private String defaultDirectory;

    public ReportingDataService(ConfigProperties configProperties, MinioConfig minioConfig) {
        super(configProperties, minioConfig);
        this.configProperties = configProperties;
        if(minioConfig != null && minioConfig.isEnable()){
            MinioClient minioClient = MinioClient.builder().endpoint(minioConfig.getEndpoint()).credentials(minioConfig.getAccessKey(), minioConfig.getSecretKey()).build();
            fileUtilsInterface = new MinioImpl(minioClient, minioConfig.getBucketName());
        }else{
            fileUtilsInterface = new FileSystemImpl(configProperties.getDefaultDirectory());
        }
    }

    @PutMapping(value = "/reportingdata/main")
    @Operation(operationId = "reportingdata", summary = "${summary.reportingData}", description = "${description.reportingData}")
    public ResponseEntity<String> processReportingData(
            @Parameter(description = "${param.campaignId}", required = true, example = INDIRECTORY_EXAMPLE)
            @RequestBody String campaignId,
            @Parameter(description = "${param.reportingDataFilePath}", required = true)
            @RequestParam String reportingDataFilePath
    ){
        FolderSystem folderSystem = FolderSystem.MAIN;
        return launchReportingDataProcessing(reportingDataFilePath, campaignId, folderSystem, null);
    }

    @PutMapping(value = "/reportingdata/genesis")
    @Operation(operationId = "reportingdata", summary = "${summary.reportingDataGenesis}", description = "$" +
            "{description.reportingDataGenesis}")
    public ResponseEntity<String> processReportingDataGenesis(
            @Parameter(description = "${param.campaignId}", required = true, example = INDIRECTORY_EXAMPLE)
            @RequestBody String campaignId,
            @RequestParam String reportingDataFileName,
            @Parameter(description = "${param.dataMode}", required = true)
            @RequestParam Mode mode
    ){
        FolderSystem folderSystem = FolderSystem.GENESIS;
        return launchReportingDataProcessing("reporting/"+reportingDataFileName, campaignId, folderSystem, mode);
    }

    private ResponseEntity<String> launchReportingDataProcessing(String reportingDataFilePath, String campaignId,
                                                                 FolderSystem folderSystem, @Nullable Mode mode) {
        ReportingDataProcessing reportingDataProcessing = new ReportingDataProcessing();
        try {
            if(folderSystem.equals(FolderSystem.MAIN)){
                reportingDataProcessing.runProcessMain(fileUtilsInterface,
                        defaultDirectory,
                        campaignId,
                        reportingDataFilePath
                );
                return ResponseEntity.ok("Reporting data processed");
            }
            if (mode == null){
                return ResponseEntity.badRequest().body("Mode not specified !");
            }
            reportingDataProcessing.runProcessGenesis(
                    fileUtilsInterface,
                    mode,
                    defaultDirectory,
                    campaignId,
                    reportingDataFilePath
            );
            return ResponseEntity.ok("Reporting data processed");
        }catch (KraftwerkException e){
            return ResponseEntity.status(e.getStatus()).body(e.getMessage());
        }
    }
}
