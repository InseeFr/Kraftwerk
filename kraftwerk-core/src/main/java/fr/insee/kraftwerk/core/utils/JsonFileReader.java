package fr.insee.kraftwerk.core.utils;

import java.io.IOException;
import java.nio.file.Path;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonFileReader {

    private JsonFileReader(){
        throw new IllegalStateException("Utility class");
    }

    /**
     * Read a json local json file.
     *
     * @param filePath Path to a json file.
     *
     *  @return A jackson.databind.JsonNode.
     */
    public static JsonNode read(Path filePath) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readTree(filePath.toFile());
    }

}
