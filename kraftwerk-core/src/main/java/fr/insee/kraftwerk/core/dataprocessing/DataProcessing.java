package fr.insee.kraftwerk.core.dataprocessing;

import fr.insee.kraftwerk.core.exceptions.KraftwerkException;
import fr.insee.kraftwerk.core.utils.KraftwerkExecutionContext;
import fr.insee.kraftwerk.core.utils.files.FileUtilsInterface;
import fr.insee.kraftwerk.core.vtl.VtlBindings;
import fr.insee.kraftwerk.core.vtl.VtlExecute;
import fr.insee.kraftwerk.core.vtl.VtlScript;
import lombok.extern.log4j.Log4j2;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.Optional;


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

    protected static final String TEMP_DATASET_SUFFIX = "_tmp";

    protected final VtlBindings vtlBindings;
	VtlExecute vtlExecute;

    FileUtilsInterface fileUtilsInterface;

    protected DataProcessing(VtlBindings vtlBindings,
                             FileUtilsInterface fileUtilsInterface,
                             KraftwerkExecutionContext kraftwerkExecutionContext
    ){
        this.vtlBindings = vtlBindings;
        vtlExecute = new VtlExecute(fileUtilsInterface, kraftwerkExecutionContext);
        this.fileUtilsInterface = fileUtilsInterface;
    }

    public abstract String getStepName();

    public String applyVtlTransformations(String bindingName, Path userVtlInstructionsPath, KraftwerkExecutionContext kraftwerkExecutionContext) throws KraftwerkException {
        // First step
        String automatedVtlInstructions = applyAutomatedVtlInstructions(bindingName, kraftwerkExecutionContext);
        // Second step
        if(userVtlInstructionsPath == null || !fileUtilsInterface.isFileExists(userVtlInstructionsPath.toString())){
            log.info("No user VTL instructions given for dataset named {} (step {}).", bindingName, getStepName());
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
        log.debug("Automated VTL instructions generated for step {}: see temp file", getStepName());
        if (!(automatedInstructions.isEmpty() || automatedInstructions.toString().contentEquals(""))) {
        	vtlExecute.evalVtlScript(automatedInstructions, vtlBindings, kraftwerkExecutionContext);
        }

        String lastTempDatasetName = getLastTempDatasetName(bindingName);
        if(lastTempDatasetName != null){
            vtlBindings.replace(bindingName, vtlBindings.getDataset(lastTempDatasetName));
            cleanTempDatasets(bindingName);
        }

        return automatedInstructions.toString();
    }

    private String getLastTempDatasetName(String bindingName){
        String tempDatasetName = bindingName + TEMP_DATASET_SUFFIX;
        Optional<String> lastTempDataSetNameOptional = vtlBindings.getDatasetNames().stream()
                .filter(datasetName -> datasetName.startsWith(tempDatasetName))
                .filter(datasetName -> datasetName.substring(tempDatasetName.length()).matches("\\d+")) // Must end with a number
                //Get max based on number at the end
                .max(Comparator.comparingInt(s -> Integer.parseInt(s.substring(tempDatasetName.length()))));
        if(lastTempDataSetNameOptional.isPresent()){
            return lastTempDataSetNameOptional.get();
        }
        return vtlBindings.getDatasetNames().contains(tempDatasetName) ? tempDatasetName : null;
    }

    private void cleanTempDatasets(String bindingName){
        String tempDatasetNamePrefix = bindingName + TEMP_DATASET_SUFFIX;
        vtlBindings.remove(tempDatasetNamePrefix); // Removes numberless temp dataset if exists
        vtlBindings.getDatasetNames().stream()
                .filter(datasetName -> datasetName.startsWith(tempDatasetNamePrefix))
                .filter(datasetName -> datasetName.substring(tempDatasetNamePrefix.length()).matches("\\d+"))
                .toList()
                .forEach(vtlBindings::remove);
    }

    protected void applyUserVtlInstructions(
            Path userVtlInstructionsPath,
            KraftwerkExecutionContext kraftwerkExecutionContext
    ) throws KraftwerkException {
        String vtlScript = getUserVtlInstructions(userVtlInstructionsPath, kraftwerkExecutionContext);
        log.info("User VTL instructions read for step {}:\n{}", getStepName(), vtlScript);
        if (vtlScript != null
                && !vtlScript.isEmpty()
                && !vtlScript.contentEquals("")
        ) {
            vtlExecute.evalVtlScript(vtlScript, vtlBindings, kraftwerkExecutionContext);
        }
    }

    private String getUserVtlInstructions(
            Path userVtlInstructionsPath,
            KraftwerkExecutionContext kraftwerkExecutionContext
    ) throws KraftwerkException {
        if(kraftwerkExecutionContext.getUserVtlInstructionsCache().containsKey(userVtlInstructionsPath)){
            return kraftwerkExecutionContext.getUserVtlInstructionsCache().get(userVtlInstructionsPath);
        }
        log.info("Reading vtl user instructions file {}", userVtlInstructionsPath.toString());
        try (InputStream inputStream = fileUtilsInterface.readFile(userVtlInstructionsPath.toString())){
            String vtlScript = new String(inputStream.readAllBytes());
            kraftwerkExecutionContext.getUserVtlInstructionsCache().put(userVtlInstructionsPath, vtlScript);
            return vtlScript;
        } catch (IOException _){
            throw new KraftwerkException(500, "Reading error on vtl script");
        }
    }
}
