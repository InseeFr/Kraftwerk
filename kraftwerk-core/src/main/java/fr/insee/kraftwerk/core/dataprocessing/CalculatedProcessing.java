package fr.insee.kraftwerk.core.dataprocessing;

import fr.insee.bpm.metadata.model.CalculatedVariables;
import fr.insee.kraftwerk.core.utils.files.FileUtilsInterface;
import fr.insee.kraftwerk.core.utils.log.KraftwerkExecutionContext;
import fr.insee.kraftwerk.core.vtl.VtlBindings;
import fr.insee.kraftwerk.core.vtl.VtlChecker;
import fr.insee.kraftwerk.core.vtl.VtlScript;
import lombok.extern.log4j.Log4j2;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Log4j2
public class CalculatedProcessing extends DataProcessing {

    /** Maximal number of iterations to resolve the order of execution of VTL expressions. */
    public static final int MAXIMAL_RESOLVING_ITERATIONS = 100;
    private final CalculatedVariables calculatedVariables;

    public CalculatedProcessing(VtlBindings vtlBindings,  CalculatedVariables calculatedVariables, FileUtilsInterface fileUtilsInterface) {
        super(vtlBindings, fileUtilsInterface);
        this.calculatedVariables = calculatedVariables;
    }

    @Override
    public String getStepName() {
        return "CALCULATED VARIABLES";
    }
    
    
    public String applyCalculatedVtlTransformations(String bindingName, Path userVtlInstructionsPath, KraftwerkExecutionContext kraftwerkExecutionContext){
        // First step
        String automatedVtlInstructions = applyAutomatedVtlInstructions(bindingName, kraftwerkExecutionContext);
        // Second step
        if(userVtlInstructionsPath != null) {
            applyUserVtlInstructions(userVtlInstructionsPath, kraftwerkExecutionContext);
            applyAutomatedVtlInstructions(bindingName, kraftwerkExecutionContext);
        } else {
            log.info(String.format("No user VTL instructions given for dataset named %s (step %s).",
                    bindingName, getStepName()));
        }
        return automatedVtlInstructions;
    }

    /**
     * Return the VTL instruction for each calculated variable registered in the CalculatedVariables object given.
     * FILTER_RESULT variables are added in the given variables map.
     * @param bindingName The name of the concerned dataset.
     * @return a VtlScript with one instruction for each "calculated" variable.
     */
    @Override
    protected VtlScript generateVtlInstructions(String bindingName) {

        List<String> orderedCalculatedNames = resolveCalculated(calculatedVariables);

        VtlScript vtlScript = new VtlScript();

        for (String calculatedName : orderedCalculatedNames) {

            String vtlExpression = calculatedVariables.getVtlExpression(calculatedName);
            if (vtlExpression != null && !vtlExpression.isEmpty()) {
                vtlExpression = VtlChecker.fixVtlExpression(vtlExpression, calculatedName, bindingName, vtlBindings);
                vtlScript.add(vtlExpression);
            }

        }

        return vtlScript;
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
     * <a href="https://www.javaallin.com/code/java-super-clone-method-and-inheritance.html">Link1</a>
     * <a href="https://www.baeldung.com/java-copy-hashmap">Link2</a>
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
