package fr.insee.kraftwerk.core.sequence;

import lombok.extern.log4j.Log4j2;

import java.nio.file.Path;
import java.nio.file.Paths;

@Log4j2
public class ControlInputSequenceGenesis {

	String defaultDirectory;
	private static final String SPEC_FOLDER = "specs";

	public ControlInputSequenceGenesis(String defaultDirectory) {
		super();
		this.defaultDirectory = defaultDirectory;
	}

	public Path getSpecsDirectory(String specsDirectoryParam) {
		return Paths.get(defaultDirectory,SPEC_FOLDER, specsDirectoryParam);
	}

}
