package fr.insee.kraftwerk.core.sequence;

import fr.insee.kraftwerk.core.Constants;
import fr.insee.kraftwerk.core.utils.files.FileUtilsInterface;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;

import java.nio.file.Path;
import java.nio.file.Paths;

@Log4j2
public class ControlInputSequenceGenesis {

	String defaultDirectory;
	private static final String SPEC_FOLDER = "specs";
	private final FileUtilsInterface fileUtilsInterface;

	@Getter
	boolean hasConfigFile = true;

	public ControlInputSequenceGenesis(String defaultDirectory, FileUtilsInterface fileUtilsInterface) {
		super();
		this.defaultDirectory = defaultDirectory;
		this.fileUtilsInterface = fileUtilsInterface;
	}

	public Path getSpecsDirectory(String specsDirectoryParam) {
		Path specsDirectory = Paths.get(defaultDirectory,SPEC_FOLDER, specsDirectoryParam);
		hasConfigFile = verifySpecsDirectory(specsDirectory);
		return specsDirectory;
	}

	private boolean verifySpecsDirectory(Path inDirectory) {
		Path userInputFile = inDirectory.resolve(Constants.USER_INPUT_FILE);
		if (fileUtilsInterface.isFileExists(userInputFile.toString())) {
			log.info("Found configuration file in campaign folder: {}", userInputFile);
		} else {
            log.info("No configuration file found in campaign folder: {}", inDirectory);
			return false;
		}
		return true;
	}

}
