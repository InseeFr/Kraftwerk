package fr.insee.kraftwerk.core.sequence;

import java.nio.file.Path;
import java.nio.file.Paths;

import fr.insee.kraftwerk.core.Constants;
import fr.insee.kraftwerk.core.exceptions.KraftwerkException;
import fr.insee.kraftwerk.core.inputs.UserInputsFile;
import fr.insee.kraftwerk.core.utils.files.FileUtilsInterface;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class ControlInputSequence {
	
	String defaultDirectory;
	FileUtilsInterface fileUtilsInterface;

	public ControlInputSequence(String defaultDirectory, FileUtilsInterface fileUtilsInterface) {
		super();
		this.defaultDirectory = defaultDirectory;
		this.fileUtilsInterface = fileUtilsInterface;
	}

	public UserInputsFile getUserInputs(Path inDirectory, FileUtilsInterface fileUtilsInterface) throws KraftwerkException {
		return new UserInputsFile(inDirectory.resolve(Constants.USER_INPUT_FILE), inDirectory, fileUtilsInterface);
	}

	public Path getInDirectory(String inDirectoryParam) throws KraftwerkException {
		Path inDirectory = Path.of(inDirectoryParam);
		if (!verifyInDirectory(inDirectory)) inDirectory = Paths.get(defaultDirectory, "in", inDirectoryParam);
		if (!verifyInDirectory(inDirectory)) throw new KraftwerkException(400, String.format("Configuration file not found at paths %s and %s",inDirectoryParam, inDirectory));
		return inDirectory;
	}

	private boolean verifyInDirectory(Path inDirectory) {
		Path userInputFile = inDirectory.resolve(Constants.USER_INPUT_FILE);
        return fileUtilsInterface.isFileExists(userInputFile.toString());
    }

	
}
