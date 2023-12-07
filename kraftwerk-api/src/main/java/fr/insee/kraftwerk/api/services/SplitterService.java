package fr.insee.kraftwerk.api.services;

import fr.insee.kraftwerk.core.utils.XMLSplitter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "${tag.splitter}")
@Log4j2
public class SplitterService extends KraftwerkService{

	@Operation(summary = "Split a XML file into smaller ones")
	@PutMapping(path = "/split/lunatic-xml")
	public ResponseEntity<Object> saveResponsesFromXmlFile(@RequestParam("inputFolder") String inputFolder,
														   @RequestParam("outputFolder") String outputFolder,
														   @RequestParam("filename") String filename,
														   @RequestParam("nbResponsesByFile") int nbSU)
			throws Exception {
		log.info("Split XML file : " + filename + " into " + nbSU + " SU by file");
		XMLSplitter.split(String.format("%s/in/%s/",defaultDirectory,inputFolder), filename, String.format("%s/in/%s/",defaultDirectory,outputFolder), "SurveyUnit", nbSU);
		return new ResponseEntity<>("Test", HttpStatus.OK);
	}

}
