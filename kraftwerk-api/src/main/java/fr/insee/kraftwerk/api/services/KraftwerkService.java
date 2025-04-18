package fr.insee.kraftwerk.api.services;


import fr.insee.kraftwerk.api.configuration.ConfigProperties;
import fr.insee.kraftwerk.api.configuration.MinioConfig;
import fr.insee.kraftwerk.core.Constants;
import fr.insee.kraftwerk.core.exceptions.KraftwerkException;
import fr.insee.kraftwerk.core.sequence.ControlInputSequence;
import fr.insee.kraftwerk.core.utils.files.FileSystemImpl;
import fr.insee.kraftwerk.core.utils.files.FileUtilsInterface;
import fr.insee.kraftwerk.core.utils.files.MinioImpl;
import io.minio.MinioClient;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.nio.file.Path;

@RestController
@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "Success"),
		@ApiResponse(responseCode = "400", description = "Bad Request"),
		@ApiResponse(responseCode = "413", description = "Request Entity Too Large"),
		@ApiResponse(responseCode = "404", description = "Not Found"),
		@ApiResponse(responseCode = "500", description = "Internal server error") })
public class KraftwerkService {


	protected static final String INDIRECTORY_EXAMPLE = "LOG-2021-x12-web";
	protected static final String JSON = ".json";
	
	@Value("${fr.insee.postcollecte.csv.output.quote}")
	private String csvOutputsQuoteChar;
	
	@Value("${fr.insee.postcollecte.files}")
	protected String defaultDirectory;

	@Value("${fr.insee.postcollecte.size-limit}")
	protected long limitSize;

	MinioConfig minioConfig;


	protected ControlInputSequence controlInputSequence ;

	@Autowired
	public KraftwerkService(ConfigProperties configProperties, MinioConfig minioConfig){
		this.minioConfig = minioConfig;
		FileUtilsInterface fileUtilsInterface;
		if(minioConfig != null && minioConfig.isEnable()){
			MinioClient minioClient = MinioClient.builder().endpoint(minioConfig.getEndpoint()).credentials(minioConfig.getAccessKey(), minioConfig.getSecretKey()).build();
			fileUtilsInterface = new MinioImpl(minioClient, minioConfig.getBucketName());
		}else{
			fileUtilsInterface = new FileSystemImpl(configProperties.getDefaultDirectory());
		}
		if (StringUtils.isNotEmpty(csvOutputsQuoteChar)) {
			Constants.setCsvOutputQuoteChar(csvOutputsQuoteChar.trim().charAt(0));
		}
		controlInputSequence = new ControlInputSequence(configProperties.getDefaultDirectory(), fileUtilsInterface);
	}
	
	public ResponseEntity<String> archive(String inDirectoryParam, FileUtilsInterface fileUtilsInterface) {
		Path inDirectory;
		try {
			inDirectory = controlInputSequence.getInDirectory(inDirectoryParam);
		} catch (KraftwerkException e) {
			return ResponseEntity.status(e.getStatus()).body(e.getMessage());
		}

		/* Step 4.4 : move differential data to a secondary folder */
		try {
			fileUtilsInterface.archiveInputFiles(controlInputSequence.getUserInputs(inDirectory, fileUtilsInterface));
		} catch (KraftwerkException e) {
			return ResponseEntity.status(e.getStatus()).body(e.getMessage());
		}

		/* Step 4.5 : rename in-place "kraftwerk.json" ->  "kraftwerk-<timestamp>.json" */
		fileUtilsInterface.renameInputFile(inDirectory);

		//delete temp directory
		Path tempOutputPath = FileUtilsInterface.transformToTemp(inDirectory);
		try {
			fileUtilsInterface.deleteDirectory(tempOutputPath);
		} catch (KraftwerkException e) {
			return ResponseEntity.status(e.getStatus()).body(e.getMessage());
		}
		return ResponseEntity.ok(inDirectoryParam);
	}

}
