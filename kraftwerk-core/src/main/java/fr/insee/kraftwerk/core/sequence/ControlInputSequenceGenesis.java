package fr.insee.kraftwerk.core.sequence;

import java.nio.file.Path;
import java.nio.file.Paths;

import fr.insee.kraftwerk.core.Constants;
import fr.insee.kraftwerk.core.utils.files.FileUtilsInterface;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class ControlInputSequenceGenesis {

	String defaultDirectory;
	private static final String INPUT_FOLDER = "in";
	private final FileUtilsInterface fileUtilsInterface;

	@Getter
	boolean hasConfigFile = true;

	public ControlInputSequenceGenesis(String defaultDirectory, FileUtilsInterface fileUtilsInterface) {
		super();
		this.defaultDirectory = defaultDirectory;
		this.fileUtilsInterface = fileUtilsInterface;
	}

	public Path getInDirectory(String inDirectoryParam) {
		Path inDirectory = Paths.get(defaultDirectory, INPUT_FOLDER, inDirectoryParam);
		hasConfigFile = verifyInDirectory(inDirectory);
		return inDirectory;
	}

	private boolean verifyInDirectory(Path inDirectory) {
		Path userInputFile = inDirectory.resolve(Constants.USER_INPUT_FILE);
		if (fileUtilsInterface.isFileExists(userInputFile.toString())) {
			log.info(String.format("Found configuration file in campaign folder: %s", userInputFile));
		} else {
			log.info("No configuration file found in campaign folder: " + inDirectory);
			return false;
		}
		return true;
	}

}
