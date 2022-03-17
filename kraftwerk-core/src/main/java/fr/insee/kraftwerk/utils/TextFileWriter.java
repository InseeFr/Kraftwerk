package fr.insee.kraftwerk.utils;

import java.io.FileWriter;
import java.io.IOException;

import lombok.extern.slf4j.Slf4j;

/**
 * Utils class to write text files.
 */
@Slf4j
public class TextFileWriter {

    /**
     * Write a text file.
     * @param filePath
     * Absolute path to the file.
     * @param fileContent
     * Content of the text file.
     */
    public static void writeFile(String filePath, String fileContent){
        try {
            FileWriter myWriter = new FileWriter(filePath);
            myWriter.write(fileContent);
            myWriter.close();
            log.info(String.format("Text file: %s successfully written", filePath));
        } catch (IOException e) {
            log.warn(String.format("Error occurred when trying to write text file: %s", filePath), e);
        }
    }
}
