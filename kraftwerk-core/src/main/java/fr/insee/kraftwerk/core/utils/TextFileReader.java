package fr.insee.kraftwerk.core.utils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Path;

import lombok.extern.log4j.Log4j2;

@Log4j2
public class TextFileReader {


    /**
     * Read the content of a text file.
     * Used to read vtl scripts.
     *
     * @param filePath
     * The path to the file (or only the file name if the file is directly in the resources folder).
     *
     * @return
     * The content of the file in a string.
     */
    public static String readFromPath(Path filePath){
        try {
            FileReader fileReader = new FileReader(filePath.toFile());
            return readTextContent(fileReader);
        }
        catch (IOException e) {
            log.warn(String.format("Unable to read the text file %s.", filePath), e);
            return null;
        }
    }

    public static String readTextContent(Reader fileReader) throws IOException {
        BufferedReader br = new BufferedReader(fileReader);
        StringBuilder content = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null) {
            content.append(line);
        }
        fileReader.close();
        br.close();
        return content.toString();
    }
}
