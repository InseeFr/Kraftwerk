package fr.insee.kraftwerk.core.utils;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;

import lombok.extern.log4j.Log4j2;


/**
 * Utils class to write text files.
 */
@Log4j2
public class TextFileWriter {
	
	private TextFileWriter() {
		//Utility class
	}

    /**
     * Write a text file.
     * @param filePath Path to the file.
     * @param fileContent Content of the text file.
     */
    public static void writeFile(Path filePath, String fileContent){
        try (FileWriter myWriter = new FileWriter(filePath.toFile())){
            myWriter.write(fileContent);
            log.info(String.format("Text file: %s successfully written", filePath));
        } catch (IOException e) {
            log.warn(String.format("Error occurred when trying to write text file: %s", filePath), e);
        }
    }
}
