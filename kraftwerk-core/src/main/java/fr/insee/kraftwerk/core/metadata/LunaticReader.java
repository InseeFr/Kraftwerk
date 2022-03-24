package fr.insee.kraftwerk.core.metadata;

import com.fasterxml.jackson.databind.JsonNode;
import fr.insee.kraftwerk.core.utils.JsonFileReader;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

import static fr.insee.kraftwerk.core.metadata.CalculatedVariables.CalculatedVariable;

@Slf4j
public class LunaticReader {

    /**
     * Read the lunatic questionnaire given to get VTL expression of calculated variables.
     * @param lunaticFile Path to a lunatic questionnaire file.
     * @return A CalculatedVariables map.
     */
    public static CalculatedVariables getCalculatedFromLunatic(String lunaticFile) {
        try {
            JsonNode rootNode = JsonFileReader.read(lunaticFile);

            CalculatedVariables calculatedVariables = new CalculatedVariables();

            JsonNode variablesNode = rootNode.get("variables");
            variablesNode.forEach(variableNode -> {
                if (variableNode.get("variableType").asText().equals("CALCULATED")) {
                    CalculatedVariable calculatedVariable = new CalculatedVariable(
                            variableNode.get("name").asText(),
                            variableNode.get("expression").asText());
                    JsonNode dependantVariablesNode = variableNode.get("bindingDependencies");
                    if (dependantVariablesNode != null) {
                        dependantVariablesNode.forEach(name ->
                                calculatedVariable.addDependantVariable(name.asText()));
                    }
                    calculatedVariables.putVariable(calculatedVariable);
                }
            });

            return calculatedVariables;

        } catch (IOException e) {
            log.error("Unable to read Lunatic questionnaire file: " + lunaticFile);
            return null;
        }
    }

}
