package fr.insee.kraftwerk.core.dataprocessing;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import fr.insee.kraftwerk.core.Constants;
import fr.insee.kraftwerk.core.metadata.CalculatedVariables;
import fr.insee.kraftwerk.core.metadata.Group;
import fr.insee.kraftwerk.core.metadata.Variable;
import fr.insee.kraftwerk.core.metadata.VariableType;
import fr.insee.kraftwerk.core.metadata.VariablesMap;
import fr.insee.kraftwerk.core.vtl.VtlBindings;
import fr.insee.kraftwerk.core.vtl.VtlScript;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CalculatedProcessing extends DataProcessing {

    /** Maximal number of iterations to resolve the order of execution of VTL expressions. */
    public static final int MAXIMAL_RESOLVING_ITERATIONS = 100;

    public CalculatedProcessing(VtlBindings vtlBindings) {
        super(vtlBindings);
    }

    @Override
    public String getStepName() {
        return "CALCULATED VARIABLES";
    }

    /**
     * Return the VTL instruction for each calculated variable registered in the CalculatedVariables object given.
     * FILTER_RESULT variables are added in the given variables map.
     * @param bindingName The name of the concerned dataset.
     * @param objects  Objects expected here are:
     *                - a CalculatedVariables instance,
     *                - the corresponding VariablesMap object (used to get fully qualified name).
     * @return a VtlScript with one instruction for each "calculated" variable.
     */
    @Override
    protected VtlScript generateVtlInstructions(String bindingName, Object... objects) {

        CalculatedVariables calculatedVariables = (CalculatedVariables) objects[0];

        VariablesMap variablesMap = (VariablesMap) objects[1];

        List<String> orderedCalculatedNames = resolveCalculated(calculatedVariables);

        VtlScript vtlScript = new VtlScript();

        for (String calculatedName : orderedCalculatedNames) {

            /*
            If the variable is not registered in the variables map (DDI variables),
            it can be a FILTER_RESULT variable created by Lunatic.
            In this case,
            Otherwise, the variable is not created and a warning pops in the log.
            */

            if (!variablesMap.hasVariable(calculatedName)) {
                if (calculatedName.startsWith(Constants.FILTER_RESULT_PREFIX)) {
                    addFilterResult(calculatedName, variablesMap);
                } else {
                    log.warn(String.format("Unknown CALCULATED variable \"%s\".", calculatedName));
                }
            }

            String vtlExpression = calculatedVariables.getVtlExpression(calculatedName);
            if (vtlExpression != null && !vtlExpression.equals("")) {
                vtlScript.add(String.format("%s := %s [calc %s := %s];",
                        bindingName, bindingName, calculatedName, vtlExpression));
            }

        }

        return vtlScript;
    }

    /** If the variable name after "FILTER_RESULT_" is recognized in the variables map,
     * a variable corresponding to the given filter result is added in the map with the right group. */
    private void addFilterResult(String filterResultName, VariablesMap variablesMap) {
        String correspondingVariableName = filterResultName.replace(Constants.FILTER_RESULT_PREFIX, "");
        Group group;
        if (variablesMap.hasVariable(correspondingVariableName)) { // the variable is directly found
            group = variablesMap.getVariable(correspondingVariableName).getGroup();
        } else if (variablesMap.hasMcq(correspondingVariableName)) { // otherwise, it should be from a MCQ
            group = variablesMap.getMcqGroup(correspondingVariableName);
        } else { //TODO : FIXME ?????
            group = variablesMap.getGroup(variablesMap.getGroupNames().get(0));
            // No information from the DDI about question or variable
            // It has been arbitrarily associated with the group above
        }
        variablesMap.putVariable(new Variable(filterResultName, group, VariableType.BOOLEAN));
    }

    /** Return a list of calculated variable names, in such an order that the evaluation of VTL expressions
     * can be performed. */
    private List<String> resolveCalculated(CalculatedVariables calculatedVariables) {
        // Init result
        List<String> resolved = new ArrayList<>();

        // Create a shallow copy of the calculated variables map
        CalculatedVariables unresolved = shallowCopy(calculatedVariables);

        // Resolve
        int counter = 0;
        while (!unresolved.isEmpty() && counter < MAXIMAL_RESOLVING_ITERATIONS) {
            for (Iterator<String> iterator = unresolved.keySet().iterator(); iterator.hasNext();) {
                String calculatedName = iterator.next();
                if (isResolved(unresolved, calculatedName)) {
                    iterator.remove(); // ( => unresolved.remove(calculatedName); )
                    resolved.add(calculatedName);
                }
            }
            counter++;
        }

        // In case sequence break due to counter (should not happen if the lunatic questionnaire given is consistent)
        if (!unresolved.isEmpty()) {
            log.warn("Following calculated variables could not be resolved and will not be calculated: ");
            log.warn(unresolved.keySet().toString());
        }

        return resolved;
    }

    /** Return a shallow copy of the given CalculatedVariables object.
     * https://www.javaallin.com/code/java-super-clone-method-and-inheritance.html
     * https://www.baeldung.com/java-copy-hashmap
     * */
    private CalculatedVariables shallowCopy(CalculatedVariables calculatedVariables) {
        CalculatedVariables res = new CalculatedVariables();
        res.putAll(calculatedVariables);
        return res;
    }

    private boolean isResolved(CalculatedVariables calculatedVariables, String calculatedName) {
        for (String variableName : calculatedVariables.getDependantVariables(calculatedName)) {
            if (calculatedVariables.containsKey(variableName)) {
                return false;
            }
        }
        return true;
    }
}
