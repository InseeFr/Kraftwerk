package fr.insee.kraftwerk.core.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.file.Path;

import fr.insee.kraftwerk.core.utils.files.FileUtilsInterface;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class TextFileReader {

    private TextFileReader(){
        throw new IllegalStateException("Utility class");
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
    public static String readFromPath(Path filePath, FileUtilsInterface fileUtilsInterface){
        try (InputStream inputStream = fileUtilsInterface.readFile(filePath.toString())){
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
            return readTextContent(inputStreamReader);
        }
        catch (IOException e) {
            log.warn(String.format("Unable to read the text file %s.", filePath), e);
            return null;
        }
    }

    public static String readTextContent(Reader inputStreamReader) throws IOException {
        BufferedReader br = new BufferedReader(inputStreamReader);
        StringBuilder content = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null) {
            content.append(line);
        }
        inputStreamReader.close();
        br.close();
        return content.toString();
    }
}
