package fr.insee.kraftwerk.core.metadata;

import lombok.Getter;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import static fr.insee.kraftwerk.core.metadata.CalculatedVariables.CalculatedVariable;

/**
 * Map to store specific information concerning the calculated variables.
 * Keys: a variable name
 * Values: CalculatedVariable objects.
 * */
public class CalculatedVariables extends LinkedHashMap<String, CalculatedVariable> {

    /** Register a calculated variable in the map.
     * The object name attribute is the key of the entry in the map. */
    public void putVariable(CalculatedVariable calculatedVariable) {
        this.put(calculatedVariable.getName(), calculatedVariable);
    }

    /** Get the VTL expression of a registered variable. */
    public String getVtlExpression(String calculatedName) {
        return this.get(calculatedName).getVtlExpression();
    }

    /** Get the dependant variables of a calculated variable. */
    public List<String> getDependantVariables(String calculatedName) {
        return this.get(calculatedName).getDependantVariables();
    }

    /** If the variable is in the map, it is a calculated variable. Return true if so. */
    public boolean isCalculated(String variableName) {
        return this.containsKey(variableName);
    }

    /** POJO class to store specific information of a calculated variable. */
    public static class CalculatedVariable {

        /** Variable name (should be the same as in the DDI) */
        @Getter
        String name;
        /** VTL expression that defines the variable (read in the Lunatic questionnaire). */
        @Getter
        String vtlExpression;
        /** Variables needed to perform the calculation. */
        @Getter
        List<String> dependantVariables;

        public CalculatedVariable(String name, String vtlExpression) {
            this.name = name;
            this.vtlExpression = vtlExpression;
            this.dependantVariables = new ArrayList<>();
        }

        public CalculatedVariable(String name, String vtlExpression, List<String> dependantVariables) {
            this.name = name;
            this.vtlExpression = vtlExpression;
            this.dependantVariables = dependantVariables;
        }

        public void addDependantVariable(String name) {
            dependantVariables.add(name);
        }
    }
}
