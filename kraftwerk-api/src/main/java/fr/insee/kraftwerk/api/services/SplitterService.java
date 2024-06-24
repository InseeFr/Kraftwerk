package fr.insee.kraftwerk.api.services;

import fr.insee.kraftwerk.api.configuration.MinioConfig;
import fr.insee.kraftwerk.core.utils.xml.XmlSplitter;
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
//TODO add MinIO support

	@Autowired
	public SplitterService(MinioConfig minioConfig) {
		super(minioConfig);
	}

	@Operation(summary = "Split a XML file into smaller ones")
	@PutMapping(path = "/split/lunatic-xml")
	public ResponseEntity<Object> saveResponsesFromXmlFile(@RequestParam("inputFolder") String inputFolder,
														   @RequestParam("outputFolder") String outputFolder,
														   @RequestParam("filename") String filename,
														   @RequestParam("nbResponsesByFile") int nbSU)
			throws Exception {
		log.info("Split XML file : " + filename + " into " + nbSU + " SU by file");
		XmlSplitter.split(String.format("%s/in/%s/",defaultDirectory,inputFolder), filename, String.format("%s/in/%s/",defaultDirectory,outputFolder), "SurveyUnit", nbSU);
		return new ResponseEntity<>("File split", HttpStatus.OK);
	}

}
