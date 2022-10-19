package fr.insee.kraftwerk.core.dataprocessing;

import java.nio.file.Path;

import fr.insee.kraftwerk.core.utils.TextFileReader;
import fr.insee.kraftwerk.core.vtl.VtlBindings;
import fr.insee.kraftwerk.core.vtl.VtlScript;
import lombok.extern.slf4j.Slf4j;

/**
 * Interface to apply VTL instructions.
 *
 * Each processing brick has two steps:
 * First is an automated process: VTL instructions are generated (eventually an empty string)
 * and are then applied.
 * Second is a process describes by the user VTL instructions file.
 * If the user has not given a VTL file for this brick, the second step is omitted.
 */
@Slf4j
public abstract class DataProcessing {

    protected final VtlBindings vtlBindings;

    protected DataProcessing(VtlBindings vtlBindings){
        this.vtlBindings = vtlBindings;
    }

    public abstract String getStepName();

    public void applyVtlTransformations(String bindingName, Path userVtlInstructionsPath){
        // Generate automated instructions and apply
    	VtlScript automatedInstructions = generateVtlInstructions(bindingName);
    	applyVtlInstructions(automatedInstructions.toString());
        
        // Apply user's instructions
        if(userVtlInstructionsPath != null) {
            String vtlScript = TextFileReader.readFromPath(userVtlInstructionsPath);
        	applyVtlInstructions(vtlScript);
        } else {
            log.info(String.format("No user VTL instructions given for dataset named %s (step %s).",
                    bindingName, getStepName()));
        }
    }

    /**
     * Abstract method that has to be implemented in concrete processing class,
     * return the automated VTL instructions for the step.
     *
     * @param bindingName The name of the concerned dataset.
     * @param objects Optional additional information to perform the step.
     *
     * @return a VTL script.
     */
    protected abstract VtlScript generateVtlInstructions(String bindingName);


    private void applyVtlInstructions(String vtl){
    	if (vtl == null) { return;}
        log.info(String.format("VTL instructions applied for step %s:%n%s", getStepName(), vtl));
        if (!(vtl.isEmpty() || vtl.contentEquals(""))) {
            vtlBindings.evalVtlScript(vtl);
        }
    }


}
