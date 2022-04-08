package fr.insee.kraftwerk.core.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.file.Path;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TextFileReader {

    /**
     * Read the content of a text file from the src/main/resources folder.
     * Used to read vtl scripts.
     *
     * @param filePath
     * The path to the file (or only the file name if the file is directly in the resources folder).
     *
     * @return
     * The content of the file in a string.
     */
    public static String readFromResources(String filePath) {
        ClassLoader classloader = Thread.currentThread().getContextClassLoader();
        InputStream is = classloader.getResourceAsStream(filePath);
        if (is != null) {
            try{
                InputStreamReader isr = new InputStreamReader(is);
                is.close();
                return readTextContent(isr);
            }
            catch(IOException e){
                log.warn(String.format("Unable to read the resource text file %s.", filePath), e);
                return null;
            }
        } else {
            log.warn("null input stream trying to read resource: " + filePath);
            return null;
        }

    }

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
