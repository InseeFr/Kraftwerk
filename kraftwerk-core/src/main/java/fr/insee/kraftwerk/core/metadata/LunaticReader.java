package fr.insee.kraftwerk.core.metadata;

import com.fasterxml.jackson.databind.JsonNode;
import fr.insee.kraftwerk.core.utils.JsonFileReader;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

@Slf4j
public class LunaticReader {

    /**
     * Read the lunatic questionnaire given to get VTL expression of calculated variables.
     * @param lunaticFile Path to a lunatic questionnaire file.
     * @return A CalculatedVariables map.
     */
    public static CalculatedVariables getCalculatedFromLunatic(String lunaticFile) {
        try {
            // Read the lunatic questionnaire json file
            JsonNode rootNode = JsonFileReader.read(lunaticFile);

            // Init the result object
            CalculatedVariables calculatedVariables = new CalculatedVariables();

            // Fill the object
            // TODO

            //
            return calculatedVariables;

        } catch (IOException e) {
            log.error("Unable to read Lunatic questionnaire file: " + lunaticFile);
            return null;
        }
    }
}
