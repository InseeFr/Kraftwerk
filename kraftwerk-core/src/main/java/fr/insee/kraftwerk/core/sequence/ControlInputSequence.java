package fr.insee.kraftwerk.core.sequence;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import fr.insee.kraftwerk.core.Constants;
import fr.insee.kraftwerk.core.exceptions.KraftwerkException;
import fr.insee.kraftwerk.core.inputs.UserInputs;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class ControlInputSequence {
	
	String defaultDirectory;

	public ControlInputSequence(String defaultDirectory) {
		super();
		this.defaultDirectory = defaultDirectory;
	}

	public UserInputs getUserInputs(Path inDirectory) throws KraftwerkException {
		return new UserInputs(inDirectory.resolve(Constants.USER_INPUT_FILE), inDirectory);
	}

	public Path getInDirectory(String inDirectoryParam) throws KraftwerkException {
		Path inDirectory = Paths.get(inDirectoryParam);
		if (!verifyInDirectory(inDirectory)) inDirectory = Paths.get(defaultDirectory, "in", inDirectoryParam);
		if (!verifyInDirectory(inDirectory)) throw new KraftwerkException(400, "Configuration file not found");
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
