package fr.insee.kraftwerk.core.sequence;

import fr.insee.kraftwerk.core.Constants;
import fr.insee.kraftwerk.core.exceptions.KraftwerkException;
import fr.insee.kraftwerk.core.inputs.UserInputsGenesis;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Log4j2
public class ControlInputSequenceGenesis {

	String defaultDirectory;

	@Getter
	boolean hasConfigFile = true;

	public ControlInputSequenceGenesis(String defaultDirectory) {
		super();
		this.defaultDirectory = defaultDirectory;
	}

	public Path getInDirectory(String inDirectoryParam) {
		Path inDirectory = Paths.get(defaultDirectory, "in", inDirectoryParam);
		hasConfigFile = verifyInDirectory(inDirectory);
		return inDirectory;
	}

	private boolean verifyInDirectory(Path inDirectory) {
		Path userInputFile = inDirectory.resolve(Constants.USER_INPUT_FILE);
		if (Files.exists(userInputFile)) {
			log.info(String.format("Found configuration file in campaign folder: %s", userInputFile));
		} else {
			log.info("No configuration file found in campaign folder: " + inDirectory);
			return false;
		}
		return true;
	}

}
