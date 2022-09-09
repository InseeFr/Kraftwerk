package fr.insee.kraftwerk.core.exceptions;

import java.io.File;
import java.nio.file.Path;

public class NullException extends KraftwerkException {

    private static final long serialVersionUID = 6677487610288558193L;

    
    public NullException() {
        this( "Denominator cannot be zero" );
    }

    public NullException( String message ) {
        super(500, message );
    }

    
    // Autres méthodes si nécessaire.
    //TODO move this method somewhere else
	/**
	 * Rename the incorrect input file
	 */
	public void renameInputFile(Path inDirectory) {
		File file = inDirectory.resolve("kraftwerk.json").toFile();
		File file2 = inDirectory.resolve("kraftwerk-ERROR.json").toFile();
		if (file2.exists()) {
			file2.delete();
		}
		file.renameTo(file2);
	}
}