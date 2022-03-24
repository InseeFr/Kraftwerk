package fr.insee.kraftwerk.core.dataprocessing;

import fr.insee.kraftwerk.core.metadata.CalculatedVariables;
import fr.insee.kraftwerk.core.vtl.VtlBindings;
import fr.insee.kraftwerk.core.vtl.VtlScript;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class CalculatedProcessing extends DataProcessing {

    /** Maximal number of iterations to resolve the order of execution of VTL expressions. */
    public static int MAXIMAL_RESOLVING_ITERATIONS = 100;

    public CalculatedProcessing(VtlBindings vtlBindings) {
        super(vtlBindings);
    }

    @Override
    public String getStepName() {
        return "CALCULATED VARIABLES";
    }

    /**
     * Return the VTL instruction for each calculated variable registered in the CalculatedVariables object given.
     * @param bindingName The name of the concerned dataset.
     * @param objects A CalculatedVariables instance is expected here.
     * @return a VtlScript with one instruction for each "calculated" variable.
     */
    @Override
    protected VtlScript generateVtlInstructions(String bindingName, Object... objects) {

        CalculatedVariables calculatedVariables = (CalculatedVariables) objects[0];

        List<String> orderedCalculatedNames = resolveCalculated(calculatedVariables);

        VtlScript vtlScript = new VtlScript();

        for (String calculatedName : orderedCalculatedNames) {
            String vtlExpression = calculatedVariables.getVtlExpression(calculatedName);
            vtlScript.add(String.format("%s := %s [calc %s := %s];",
                    bindingName, bindingName, calculatedName, vtlExpression));
        }

        return vtlScript;
    }

    /** Return a list of calculated variable names, in such an order that the evaluation of VTL expressions
     * can be performed. */
    private List<String> resolveCalculated(CalculatedVariables calculatedVariables) {
        // Init result
        List<String> res = new ArrayList<>();

        // Create a shallow copy of the calculated variables map
        CalculatedVariables unresolved = shallowCopy(calculatedVariables);

        // Resolve
        int counter = 0;
        while (!unresolved.isEmpty() && counter < MAXIMAL_RESOLVING_ITERATIONS) {
            for (String calculatedName : unresolved.keySet()) {
                if (isResolved(unresolved, calculatedName)) {
                    unresolved.remove(calculatedName);
                    res.add(calculatedName);
                }
            }
            counter++;
        }

        // In case of counter sequence break (should not happen if the lunatic questionnaire given is consistent)
        if (!unresolved.isEmpty()) {
            log.warn("Following calculated variables could not be resolved and will not be calculated: ");
            log.warn(unresolved.keySet().toString());
        }

        return res;
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
