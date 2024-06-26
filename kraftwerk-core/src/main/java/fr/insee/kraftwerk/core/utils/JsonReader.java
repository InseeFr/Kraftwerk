package fr.insee.kraftwerk.core.utils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.insee.kraftwerk.core.utils.files.FileUtilsInterface;

public class JsonReader {

    private JsonReader(){
        throw new IllegalStateException("Utility class");
    }

    /**
     * Read a json local json file.
     *
     * @param filePath Path to a json file.
     *
     *  @return A jackson.databind.JsonNode.
     */
    public static JsonNode read(Path filePath, FileUtilsInterface fileUtilsInterface) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        try (InputStream inputStream = fileUtilsInterface.readFile(filePath.toString())){
            return mapper.readTree(inputStream);
        }
    }

}
