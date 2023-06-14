package fr.insee.kraftwerk.core.metadata;

import static fr.insee.kraftwerk.core.Constants.FILTER_RESULT_PREFIX;
import static fr.insee.kraftwerk.core.Constants.MISSING_SUFFIX;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;

import fr.insee.kraftwerk.core.Constants;
import fr.insee.kraftwerk.core.metadata.CalculatedVariables.CalculatedVariable;
import fr.insee.kraftwerk.core.utils.JsonFileReader;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class LunaticReader {

    private static final String VARIABLES = "variables";
    
    private LunaticReader() {
        throw new IllegalStateException("Utility class");
      }



	/**
     * Read the lunatic questionnaire given to get VTL expression of calculated variables.
     * @param lunaticFile Path to a lunatic questionnaire file.
     * @return A CalculatedVariables map.
     */
    public static CalculatedVariables getCalculatedFromLunatic(Path lunaticFile) {
        try {
            JsonNode rootNode = JsonFileReader.read(lunaticFile);

            String lunaticModelVersion = rootNode.get("lunaticModelVersion").toString();
            boolean isLunaticV2 = JsonFileReader.compareVersions(lunaticModelVersion.replace("\"",""),"2.3.0") > 0 ;

            CalculatedVariables calculatedVariables = new CalculatedVariables();

            JsonNode variablesNode = rootNode.get(VARIABLES);
            variablesNode.forEach(variableNode -> {
                if (variableNode.get("variableType").asText().equals("CALCULATED")) {
                    String formula =  isLunaticV2 ? variableNode.get("expression").get("value").asText(): variableNode.get("expression").asText();
                    CalculatedVariable calculatedVariable = new CalculatedVariable(
                            variableNode.get("name").asText(),
                            formula);
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
            log.error("Unable to read Lunatic questionnaire file: {}", lunaticFile);
            return new CalculatedVariables();
        }
    }

    /**
     * Read the lunatic file to get the names of the _MISSING variables and the collected variables added by Eno which are not present in the DDI
     *
     * @param lunaticFile Path to a lunatic questionnaire file.
     * @return A List of String.
     */
    public static List<String> getMissingVariablesFromLunatic(Path lunaticFile){
        try {
            JsonNode rootNode = JsonFileReader.read(lunaticFile);
            List<String> variables = new ArrayList<>();
            List<String> varsEno = Arrays.asList(Constants.getEnoVariables());

            JsonNode variablesNode = rootNode.get(VARIABLES);
            variablesNode.forEach(variableNode -> variables.add(variableNode.get("name").asText()));
            return variables.stream().filter(varToRead-> varToRead.endsWith(MISSING_SUFFIX) || varsEno.contains(varToRead)).toList();

        } catch (IOException e) {
            log.error("Unable to read Lunatic questionnaire file: " + lunaticFile);
            return Collections.emptyList();
        }
    }

    /**
     * Read the lunatic file to get the names of the FILTER_RESULT variables which are not present in the DDI
     *
     * @param lunaticFile Path to a lunatic questionnaire file.
     * @return A List of String.
     */
    public static List<String> getFilterResultFromLunatic(Path lunaticFile){
        try {
            JsonNode rootNode = JsonFileReader.read(lunaticFile);
            List<String> variables = new ArrayList<>();

            JsonNode variablesNode = rootNode.get(VARIABLES);
            variablesNode.forEach(variableNode -> variables.add(variableNode.get("name").asText()));
            return variables.stream().filter(variable-> variable.startsWith(FILTER_RESULT_PREFIX)).toList();

        } catch (IOException e) {
            log.error("Unable to read Lunatic questionnaire file: " + lunaticFile);
            return Collections.emptyList();
        }
    }

}
