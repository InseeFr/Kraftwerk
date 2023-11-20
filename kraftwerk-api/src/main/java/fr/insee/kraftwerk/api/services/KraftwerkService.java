package fr.insee.kraftwerk.api.services;



import java.nio.file.Path;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import fr.insee.kraftwerk.core.Constants;
import fr.insee.kraftwerk.core.exceptions.KraftwerkException;
import fr.insee.kraftwerk.core.sequence.ControlInputSequence;
import fr.insee.kraftwerk.core.utils.FileUtils;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.annotation.PostConstruct;

@RestController
@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "Success"),
		@ApiResponse(responseCode = "400", description = "Bad Request"),
		@ApiResponse(responseCode = "404", description = "Not Found"),
		@ApiResponse(responseCode = "500", description = "Internal server error") })
public class KraftwerkService {


	protected static final String INDIRECTORY_EXAMPLE = "LOG-2021-x12-web";
	protected static final String JSON = ".json";
	
	@Value("${fr.insee.postcollecte.csv.output.quote}")
	private String csvOutputsQuoteChar;
	
	@Value("${fr.insee.postcollecte.files}")
	protected String defaultDirectory;
	
	protected ControlInputSequence controlInputSequence ;
	
	@PostConstruct
	public void initializeWithProperties() {
		if (StringUtils.isNotEmpty(csvOutputsQuoteChar)) {
			Constants.setCsvOutputQuoteChar(csvOutputsQuoteChar.trim().charAt(0));
		}
		controlInputSequence = new ControlInputSequence(defaultDirectory);
	}
	
	protected ResponseEntity<String> archive(String inDirectoryParam) {
		Path inDirectory;
		try {
			inDirectory = controlInputSequence.getInDirectory(inDirectoryParam);
		} catch (KraftwerkException e) {
			return ResponseEntity.status(e.getStatus()).body(e.getMessage());
		}
		
		/* Step 4.3 : move kraftwerk.json to a secondary folder */
		FileUtils.renameInputFile(inDirectory);

		/* Step 4.4 : move differential data to a secondary folder */
		try {
			FileUtils.archiveInputFiles(controlInputSequence.getUserInputs(inDirectory));
		} catch (KraftwerkException e) {
			return ResponseEntity.status(e.getStatus()).body(e.getMessage());
		}
		
		//delete temp directory
		Path tempOutputPath = FileUtils.transformToTemp(inDirectory);
		try {
			FileUtils.deleteDirectory(tempOutputPath);
		} catch (KraftwerkException e) {
			return ResponseEntity.status(e.getStatus()).body(e.getMessage());
		}
		return ResponseEntity.ok(inDirectoryParam);
	}

}
