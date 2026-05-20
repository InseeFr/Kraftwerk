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
import java.util.HashMap;
import java.util.Map;
import java.util.OptionalInt;


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
    protected static final String MODE_VARIABLE_NAME = "MODE_KRAFTWERK";

    protected final VtlBindings vtlBindings;
	VtlExecute vtlExecute;

    FileUtilsInterface fileUtilsInterface;

    //To keep track of last temp dataset names
    protected final Map<String, String> tempDatasetNames = new HashMap<>();

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

        String lastTempDatasetName = getLastDatasetName(bindingName);
        if(lastTempDatasetName != null){
            vtlBindings.replace(bindingName, vtlBindings.getDataset(lastTempDatasetName));
            cleanTempDatasets(bindingName);
        }

        return automatedInstructions.toString();
    }

    private void cleanTempDatasets(String bindingName){
        String tempDatasetNamePrefix = bindingName + TEMP_DATASET_SUFFIX;
        vtlBindings.remove(tempDatasetNamePrefix); // Removes numberless temp dataset if exists
        vtlBindings.getDatasetNames().stream()
                .filter(datasetName -> datasetName.startsWith(tempDatasetNamePrefix))
                .filter(datasetName -> datasetName.substring(tempDatasetNamePrefix.length()).matches("\\d+"))
                .toList()
                .forEach(vtlBindings::remove);
        tempDatasetNames.remove(bindingName);
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

    /**
     * @param datasetName Name of the dataset to get a temporary dataset name from
     * @return the name of the last created temp dataset, returns the source dataset name if no temp dataset exists
     */
    protected String getLastDatasetName(String datasetName){
        return tempDatasetNames.getOrDefault(datasetName, datasetName);
    }

    /**
     * @param datasetName Name of the dataset to get a temporary dataset name from
     * @return A temp dataset index 0 if first temp dataset, +1 if
     */
    protected String getIncrementedTempDatasetName(String datasetName) {
        if(!tempDatasetNames.containsKey(datasetName)){
            return datasetName + TEMP_DATASET_SUFFIX + 0;
        }

        OptionalInt tempDatasetNumberOptional = extractTempDatasetNumber(tempDatasetNames.get(datasetName));
        if(tempDatasetNumberOptional.isPresent()){
            return datasetName + TEMP_DATASET_SUFFIX + (tempDatasetNumberOptional.getAsInt() + 1);
        }

        //If number not found at end
        return datasetName + TEMP_DATASET_SUFFIX + 0;
    }

    /**
     * Extracts number from a temporary dataset name
     * @param tempDatasetName name of dataset
     * @return the number at the end
     */
    private OptionalInt extractTempDatasetNumber(String tempDatasetName) {
        int i = tempDatasetName.length() - 1;

        //Decrement character index until it finds a non digit character
        while (i >= 0 && Character.isDigit(tempDatasetName.charAt(i))) {
            i--;
        }
        // No number found
        if (i == tempDatasetName.length() - 1) {
            return OptionalInt.empty();
        }
        // Number found, parse it and put it into optional
        return OptionalInt.of(Integer.parseInt(tempDatasetName.substring(i + 1)));
    }

    /**
     * Adds the incremented temp dataset to the map
     */
    protected void incrementTempDataset(String datasetName){
        tempDatasetNames.put(datasetName, getIncrementedTempDatasetName(datasetName));
    }
}
