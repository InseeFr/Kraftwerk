package fr.insee.kraftwerk.core.vtl;

import java.util.Collection;
import java.util.StringJoiner;

/**
 * Class to help writing vtl instructions.
 */
public class VtlMacros {

    private VtlMacros(){
        throw new IllegalStateException("Utility class");
    }

    /**
     * Return a string that is an enumeration of variables in the VTL
     * syntax from the given variable names set.
     *
     * @param variablesSet A set of variable names.
     *
     * @return A string like "VariableA, VariableB, VariableC".
     */
    public static String toVtlSyntax(Collection<String> variablesSet) {
        StringJoiner res = new StringJoiner(", "); // space for readability
        for(String variableName : variablesSet) {
            res.add(variableName);
        }
        return res.toString();
    }
}
