package fr.insee.kraftwerk.api.services;


import fr.insee.bpm.exceptions.MetadataParserException;
import fr.insee.kraftwerk.api.configuration.ConfigProperties;
import fr.insee.kraftwerk.api.configuration.MinioConfig;
import fr.insee.kraftwerk.api.process.MainProcessing;
import fr.insee.kraftwerk.api.process.MainProcessingGenesis;
import fr.insee.kraftwerk.core.exceptions.KraftwerkException;
import fr.insee.kraftwerk.core.utils.files.FileSystemImpl;
import fr.insee.kraftwerk.core.utils.files.FileUtilsInterface;
import fr.insee.kraftwerk.core.utils.files.MinioImpl;
import io.minio.MinioClient;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;


@RestController
@Slf4j
@Tag(name = "${tag.main}")
public class MainService extends KraftwerkService {

	ConfigProperties configProperties;
	MinioClient minioClient;
	boolean useMinio;


	@Autowired
	public MainService(ConfigProperties configProperties, MinioConfig minioConfig) {
        super(minioConfig);
        this.configProperties = configProperties;
		this.minioConfig = minioConfig;
		useMinio = false;
		if(minioConfig == null){
			log.warn("Minio config null !");
		}
		if(minioConfig != null && minioConfig.isEnable()){
			minioClient = MinioClient.builder().endpoint(minioConfig.getEndpoint()).credentials(minioConfig.getAccessKey(), minioConfig.getSecretKey()).build();
			useMinio = true;
		}
	}

	@PutMapping(value = "/main")
	@Operation(operationId = "main", summary = "${summary.main}", description = "${description.main}")
	public ResponseEntity<String> mainService(
			@Parameter(description = "${param.inDirectory}", required = true, example = INDIRECTORY_EXAMPLE) @RequestBody String inDirectoryParam,
			@Parameter(description = "${param.archiveAtEnd}", required = false) @RequestParam(defaultValue = "false") boolean archiveAtEnd,
			@Parameter(description = "${param.withAllReportingData}", required = false) @RequestParam(defaultValue = "true") boolean withAllReportingData
			) {
		boolean fileByFile = false;
		boolean withDDI = true;
		FileUtilsInterface fileUtilsInterface;
		if(Boolean.TRUE.equals(useMinio)){
			fileUtilsInterface = new MinioImpl(minioClient, minioConfig.getBucketName());
		}else{
			fileUtilsInterface = new FileSystemImpl();
		}

		MainProcessing mp = new MainProcessing(inDirectoryParam, fileByFile,withAllReportingData,withDDI, defaultDirectory, limitSize, fileUtilsInterface);
		try {
			mp.runMain();
		} catch (KraftwerkException e) {
			return ResponseEntity.status(e.getStatus()).body(e.getMessage());
		}

		/* Step 4.3- 4.4 : Archive */
		if (Boolean.TRUE.equals(archiveAtEnd)) archive(inDirectoryParam,fileUtilsInterface);

		return ResponseEntity.ok(inDirectoryParam);
	}

	@PutMapping(value = "/main/file-by-file")
	@Operation(operationId = "main", summary = "${summary.fileByFile}", description = "${description.fileByFile}")
	public ResponseEntity<String> mainFileByFile(
			@Parameter(description = "${param.inDirectory}", required = true, example = INDIRECTORY_EXAMPLE) @RequestBody String inDirectoryParam,
			@Parameter(description = "${param.archiveAtEnd}", required = false) @RequestParam(defaultValue = "false") boolean archiveAtEnd
	) {
		boolean fileByFile = true;
		boolean withAllReportingData = false;
		boolean withDDI = true;
		FileUtilsInterface fileUtilsInterface;
		if(Boolean.TRUE.equals(useMinio)){
			fileUtilsInterface = new MinioImpl(minioClient, minioConfig.getBucketName());
		}else{
			fileUtilsInterface = new FileSystemImpl();
		}

		MainProcessing mp = new MainProcessing(inDirectoryParam, fileByFile,withAllReportingData,withDDI, defaultDirectory, limitSize, fileUtilsInterface);
		try {
			mp.runMain();
		} catch (KraftwerkException e) {
			return ResponseEntity.status(e.getStatus()).body(e.getMessage());
		}
			/* Step 4.3- 4.4 : Archive */
		if (Boolean.TRUE.equals(archiveAtEnd)) archive(inDirectoryParam, fileUtilsInterface);

		return ResponseEntity.ok(inDirectoryParam);
	}

	@PutMapping(value = "/main/lunatic-only")
	@Operation(operationId = "mainLunaticOnly", summary = "${summary.mainLunaticOnly}", description = "${description.mainLunaticOnly}")
	public ResponseEntity<String> mainLunaticOnly(
			@Parameter(description = "${param.inDirectory}", required = true, example = INDIRECTORY_EXAMPLE) @RequestBody String inDirectoryParam,
			@Parameter(description = "${param.archiveAtEnd}", required = false) @RequestParam(defaultValue = "false") boolean archiveAtEnd
	) {
		boolean withDDI = false;
		boolean fileByFile = false;
		boolean withAllReportingData = false;
		FileUtilsInterface fileUtilsInterface;
		if(Boolean.TRUE.equals(useMinio)){
			fileUtilsInterface = new MinioImpl(minioClient, minioConfig.getBucketName());
		}else{
			fileUtilsInterface = new FileSystemImpl();
		}

		MainProcessing mp = new MainProcessing(inDirectoryParam, fileByFile,withAllReportingData,withDDI, defaultDirectory, limitSize, fileUtilsInterface);
		try {
			mp.runMain();
		} catch (KraftwerkException e) {
			return ResponseEntity.status(e.getStatus()).body(e.getMessage());
		}
			/* Step 4.3- 4.4 : Archive */
		if (Boolean.TRUE.equals(archiveAtEnd)) archive(inDirectoryParam, fileUtilsInterface);

		return ResponseEntity.ok(inDirectoryParam);
	}

	@PutMapping(value = "/main/genesis")
	@Operation(operationId = "mainGenesis", summary = "${summary.mainGenesis}", description = "${description.mainGenesis}")
	public ResponseEntity<String> mainGenesis(
			@Parameter(description = "${param.idCampaign}", required = true, example = INDIRECTORY_EXAMPLE) @RequestBody String idCampaign) {
		FileUtilsInterface fileUtilsInterface;
		if(Boolean.TRUE.equals(useMinio)){
			fileUtilsInterface = new MinioImpl(minioClient, minioConfig.getBucketName());
		}else{
			fileUtilsInterface = new FileSystemImpl();
		}

		MainProcessingGenesis mpGenesis = new MainProcessingGenesis(configProperties, fileUtilsInterface);

		try {
			mpGenesis.runMain(idCampaign);
		} catch (KraftwerkException e) {
			return ResponseEntity.status(e.getStatus()).body(e.getMessage());
		} catch (IOException e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
		}
		return ResponseEntity.ok(idCampaign);
	}


}