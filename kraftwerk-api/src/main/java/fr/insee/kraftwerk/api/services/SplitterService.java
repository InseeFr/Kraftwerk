package fr.insee.kraftwerk.api.services;

import fr.insee.kraftwerk.api.configuration.MinioConfig;
import fr.insee.kraftwerk.core.utils.files.FileSystemImpl;
import fr.insee.kraftwerk.core.utils.files.FileSystemType;
import fr.insee.kraftwerk.core.utils.files.FileUtilsInterface;
import fr.insee.kraftwerk.core.utils.files.MinioImpl;
import fr.insee.kraftwerk.core.utils.xml.XmlSplitter;
import io.minio.MinioClient;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "${tag.splitter}")
@Log4j2
public class SplitterService extends KraftwerkService{

	@Autowired
	public SplitterService(MinioConfig minioConfig) {
		super(minioConfig);
	}

	@Operation(summary = "Split a XML file into smaller ones")
	@PutMapping(path = "/split/lunatic-xml")
	public ResponseEntity<Object> saveResponsesFromXmlFile(@RequestParam("inputFolder") String inputFolder,
														   @RequestParam("outputFolder") String outputFolder,
														   @RequestParam("filename") String filename,
														   @RequestParam("nbResponsesByFile") int nbSU,
														   @RequestParam("fileSystemType") FileSystemType fileSystemType)
			throws Exception {
		log.info("Split XML file : {} into {} SU by file using {}", filename , nbSU ,
				(fileSystemType.equals(FileSystemType.MINIO) ? "Minio" : "OS file system"));

		FileUtilsInterface fileUtilsInterface = fileSystemType.equals(FileSystemType.MINIO) ?
				new MinioImpl(MinioClient.builder().credentials(minioConfig.getAccessKey(),minioConfig.getSecretKey()).endpoint(minioConfig.getEndpoint()).build(), minioConfig.getBucketName()) :
				new FileSystemImpl(defaultDirectory);

		XmlSplitter.split(String.format("%s/in/%s/",defaultDirectory,inputFolder), filename, String.format("%s/in/%s/",defaultDirectory,outputFolder), "SurveyUnit", nbSU, fileUtilsInterface);
		return new ResponseEntity<>("File split", HttpStatus.OK);
	}

}
