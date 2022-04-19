package fr.insee.kraftwerk.core.utils;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;

import lombok.extern.slf4j.Slf4j;

/**
 * Utils class to write text files.
 */
@Slf4j
public class TextFileWriter {

    /**
     * Write a text file.
     * @param filePath Path to the file.
     * @param fileContent Content of the text file.
     */
    public static void writeFile(Path filePath, String fileContent){
        try {
            FileWriter myWriter = new FileWriter(filePath.toFile());
            myWriter.write(fileContent);
            myWriter.close();
            log.info(String.format("Text file: %s successfully written", filePath));
        } catch (IOException e) {
            log.warn(String.format("Error occurred when trying to write text file: %s", filePath), e);
        }
    }
}
