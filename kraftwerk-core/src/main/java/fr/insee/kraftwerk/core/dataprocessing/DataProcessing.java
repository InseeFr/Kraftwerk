package fr.insee.kraftwerk.core.dataprocessing;

import java.nio.file.Path;
import java.util.List;

import fr.insee.kraftwerk.core.KraftwerkError;
import fr.insee.kraftwerk.core.utils.TextFileReader;
import fr.insee.kraftwerk.core.vtl.VtlBindings;
import fr.insee.kraftwerk.core.vtl.VtlExecute;
import fr.insee.kraftwerk.core.vtl.VtlScript;
import lombok.extern.log4j.Log4j2;


/**
 * Interface to apply VTL instructions.
 *
 * Each processing brick has two steps:
 * First is an automated process: VTL instructions are generated (eventually an empty string)
 * and are then applied.
 * Second is a process describes by the user VTL instructions file.
 * If the user has not given a VTL file for this brick, the second step is omitted.
 */
@Log4j2
public abstract class DataProcessing {

    protected final VtlBindings vtlBindings;
	VtlExecute vtlExecute;

    protected DataProcessing(VtlBindings vtlBindings){
        this.vtlBindings = vtlBindings;
        vtlExecute = new VtlExecute();
    }

    public abstract String getStepName();

    public String applyVtlTransformations(String bindingName, Path userVtlInstructionsPath, List<KraftwerkError> errors){
        // First step
        String automatedVtlInstructions = applyAutomatedVtlInstructions(bindingName, errors);
        // Second step
        if(userVtlInstructionsPath != null) {
            applyUserVtlInstructions(userVtlInstructionsPath, errors);
        } else {
            log.info(String.format("No user VTL instructions given for dataset named %s (step %s).",
                    bindingName, getStepName()));
        }
        return automatedVtlInstructions;
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

    protected String applyAutomatedVtlInstructions(String bindingName, List<KraftwerkError> errors){
        VtlScript automatedInstructions = generateVtlInstructions(bindingName);
        log.debug(String.format("Automated VTL instructions generated for step %s: see temp file", getStepName()));
        if (!(automatedInstructions.isEmpty() || automatedInstructions.toString().contentEquals(""))) {
        	vtlExecute.evalVtlScript(automatedInstructions, vtlBindings, errors);
        }
        return automatedInstructions.toString();
    }

    protected void applyUserVtlInstructions(Path userVtlInstructionsPath, List<KraftwerkError> errors){
        String vtlScript = TextFileReader.readFromPath(userVtlInstructionsPath);
        log.info(String.format("User VTL instructions read for step %s:%n%s", getStepName(),
                vtlScript));
        if (! (vtlScript == null || vtlScript.isEmpty() || vtlScript.contentEquals("")) ) {
        	vtlExecute.evalVtlScript(vtlScript, vtlBindings,errors);
        }
    }

}
