package fr.insee.kraftwerk.core.utils;

import java.nio.file.Path;

public class FileUtils {

	/**
	 * Change /some/path/in/campaign-name to /some/path/out/campaign-name
	 */
	public static Path transformToOut(Path inDirectory) {
		return "in".equals(inDirectory.getFileName().toString()) ? inDirectory.getParent().resolve("out")
				: transformToOut(inDirectory.getParent()).resolve(inDirectory.getFileName());
	}

	
}
