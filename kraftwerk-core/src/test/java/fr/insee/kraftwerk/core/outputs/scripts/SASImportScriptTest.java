package fr.insee.kraftwerk.core.outputs.scripts;

import fr.insee.kraftwerk.core.KraftwerkError;
import fr.insee.kraftwerk.core.metadata.Variable;
import fr.insee.kraftwerk.core.metadata.VariableType;
import fr.insee.kraftwerk.core.metadata.VariablesMap;
import fr.insee.kraftwerk.core.outputs.TableScriptInfo;
import fr.insee.kraftwerk.core.outputs.csv.SASImportScript;
import fr.insee.vtl.model.Dataset;
import fr.insee.vtl.model.Structured;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class SASImportScriptTest {

    @Test
    void generateSASImportScript_simpleCase() {
        // Given
        String idName = "ID";
        String fooName = "FOO";
        //
        String tableName = "FOO_TABLE";
        String csvFileName = "test_sas_script.csv";
        Structured.DataStructure dataStructure = new Structured.DataStructure(List.of(
                new Structured.Component(idName, String.class, Dataset.Role.IDENTIFIER),
                new Structured.Component(fooName, Double.class, Dataset.Role.MEASURE)
        ));
        Map<String, VariablesMap> metadataVariables = new HashMap<>();
        VariablesMap variablesMap = new VariablesMap();
        variablesMap.putVariable(new Variable(idName, variablesMap.getRootGroup(), VariableType.STRING));
        variablesMap.putVariable(new Variable(fooName, variablesMap.getRootGroup(), VariableType.NUMBER));
        metadataVariables.put("FOO", variablesMap);

        // When
        List<KraftwerkError> errors = new ArrayList<>();
        TableScriptInfo tableScriptInfo = new TableScriptInfo(tableName, csvFileName, dataStructure, metadataVariables);
        SASImportScript sasImportScript = new SASImportScript(List.of(tableScriptInfo),errors);

        // Then
        String result = sasImportScript.generateScript();
        assertNotNull(result);
        assertNotEquals("", result);
        assertTrue(result.contains(idName));
        assertTrue(result.contains(fooName));
    }

    @Test
    void generateSASImportScript_withLongVariables() {
        // Given
        String idName = "ID";
        String fooName = "FOO";
        String longName1 = "VERY_LONG_VARIABLE_NAME_THAT_IS_MORE_THAN_32_BYTES_LONG";
        String longName2 = "ANOTHER_VERY_LONG_OR_SHOULD_I_SAY_TOO_LONG_VARIABLE_NAME";
        String normalName = "NORMAL_VARIABLE";
        //
        String tableName = "FOO_TABLE";
        String csvFileName = "test_sas_script.csv";
        Structured.DataStructure dataStructure = new Structured.DataStructure(List.of(
                new Structured.Component(idName, String.class, Dataset.Role.IDENTIFIER),
                new Structured.Component(fooName, Double.class, Dataset.Role.MEASURE),
                new Structured.Component(longName1, String.class, Dataset.Role.MEASURE),
                new Structured.Component(longName2, String.class, Dataset.Role.MEASURE),
                new Structured.Component(normalName, String.class, Dataset.Role.MEASURE)
        ));
        Map<String, VariablesMap> metadataVariables = new HashMap<>();
        VariablesMap variablesMap = new VariablesMap();
        variablesMap.putVariable(new Variable(idName, variablesMap.getRootGroup(), VariableType.STRING));
        variablesMap.putVariable(new Variable(fooName, variablesMap.getRootGroup(), VariableType.NUMBER));
        variablesMap.putVariable(new Variable(longName1, variablesMap.getRootGroup(), VariableType.STRING));
        variablesMap.putVariable(new Variable(longName2, variablesMap.getRootGroup(), VariableType.STRING));
        variablesMap.putVariable(new Variable(normalName, variablesMap.getRootGroup(), VariableType.STRING));
        metadataVariables.put("FOO", variablesMap);

        // When
        List<KraftwerkError> errors = new ArrayList<>();
        TableScriptInfo tableScriptInfo = new TableScriptInfo(tableName, csvFileName, dataStructure, metadataVariables);
        SASImportScript sasImportScript = new SASImportScript(List.of(tableScriptInfo),errors);

        // Then
        String result = sasImportScript.generateScript();
        assertNotNull(result);
        assertNotEquals("", result);
        assertTrue(result.contains("WARNING"));
        assertTrue(result.contains(longName1));
        assertTrue(result.contains(longName2));
    }

    @Test
    void generateSASImportScript_noLongVariables() {
        // Given
        String idName = "ID";
        String fooName = "FOO";
        String normalName = "NORMAL_VARIABLE";
        //
        String tableName = "FOO_TABLE";
        String csvFileName = "test_sas_script.csv";
        Structured.DataStructure dataStructure = new Structured.DataStructure(List.of(
                new Structured.Component(idName, String.class, Dataset.Role.IDENTIFIER),
                new Structured.Component(fooName, Double.class, Dataset.Role.MEASURE),
                new Structured.Component(normalName, String.class, Dataset.Role.MEASURE)
        ));
        Map<String, VariablesMap> metadataVariables = new HashMap<>();
        VariablesMap variablesMap = new VariablesMap();
        variablesMap.putVariable(new Variable(idName, variablesMap.getRootGroup(), VariableType.STRING));
        variablesMap.putVariable(new Variable(fooName, variablesMap.getRootGroup(), VariableType.NUMBER));
        variablesMap.putVariable(new Variable(normalName, variablesMap.getRootGroup(), VariableType.STRING));
        metadataVariables.put("FOO", variablesMap);

        // When
        List<KraftwerkError> errors = new ArrayList<>();
        TableScriptInfo tableScriptInfo = new TableScriptInfo(tableName, csvFileName, dataStructure, metadataVariables);
        SASImportScript sasImportScript = new SASImportScript(List.of(tableScriptInfo),errors);

        // Then
        String result = sasImportScript.generateScript();
        assertNotNull(result);
        assertNotEquals("", result);
        assertFalse(result.contains("WARNING: Following variable names are more than 32 characters long"));
    }

}
