package fr.insee.kraftwerk.core.dataprocessing;

import fr.insee.kraftwerk.core.exceptions.KraftwerkException;
import fr.insee.kraftwerk.core.utils.TextFileReader;
import fr.insee.kraftwerk.core.utils.files.FileUtilsInterface;
import fr.insee.kraftwerk.core.utils.log.KraftwerkExecutionContext;
import fr.insee.kraftwerk.core.vtl.VtlBindings;
import fr.insee.kraftwerk.core.vtl.VtlExecute;
import fr.insee.kraftwerk.core.vtl.VtlScript;
import lombok.extern.log4j.Log4j2;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Arrays;


/**
 * Interface to apply VTL instructions.
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

    FileUtilsInterface fileUtilsInterface;

    protected DataProcessing(VtlBindings vtlBindings, FileUtilsInterface fileUtilsInterface){
        this.vtlBindings = vtlBindings;
        vtlExecute = new VtlExecute(fileUtilsInterface);
        this.fileUtilsInterface = fileUtilsInterface;
    }

    public abstract String getStepName();

    public String applyVtlTransformations(String bindingName, Path userVtlInstructionsPath, KraftwerkExecutionContext kraftwerkExecutionContext) throws KraftwerkException {
        // First step
        String automatedVtlInstructions = applyAutomatedVtlInstructions(bindingName, kraftwerkExecutionContext);
        // Second step
        if(userVtlInstructionsPath == null || !fileUtilsInterface.isFileExists(userVtlInstructionsPath.toString())){
            log.info(String.format("No user VTL instructions given for dataset named %s (step %s).",
                    bindingName, getStepName()));
            return automatedVtlInstructions;
        }
        applyUserVtlInstructions(userVtlInstructionsPath, kraftwerkExecutionContext);
        return automatedVtlInstructions;
    }

    /**
     * Abstract method that has to be implemented in concrete processing class,
     * return the automated VTL instructions for the step.
     *
     * @param bindingName The name of the concerned dataset.
     *
     * @return a VTL script.
     */
    protected abstract VtlScript generateVtlInstructions(String bindingName);

    protected String applyAutomatedVtlInstructions(String bindingName, KraftwerkExecutionContext kraftwerkExecutionContext){
        VtlScript automatedInstructions = generateVtlInstructions(bindingName);
        log.debug(String.format("Automated VTL instructions generated for step %s: see temp file", getStepName()));
        if (!(automatedInstructions.isEmpty() || automatedInstructions.toString().contentEquals(""))) {
        	vtlExecute.evalVtlScript(automatedInstructions, vtlBindings, kraftwerkExecutionContext);
        }
        return automatedInstructions.toString();
    }

    protected void applyUserVtlInstructions(Path userVtlInstructionsPath, KraftwerkExecutionContext kraftwerkExecutionContext) throws KraftwerkException {
        String vtlScript;
        try (InputStream inputStream = fileUtilsInterface.readFile(userVtlInstructionsPath.toString())){
            vtlScript = new String(inputStream.readAllBytes());
        } catch ( IOException e){
            throw new KraftwerkException(500, "Reading error on vtl script");
        }
        log.info(String.format("User VTL instructions read for step %s:%n%s", getStepName(),
                vtlScript));
        if (! (vtlScript == null || vtlScript.isEmpty() || vtlScript.contentEquals("")) ) {
        	vtlExecute.evalVtlScript(vtlScript, vtlBindings, kraftwerkExecutionContext);
        }
    }

}
