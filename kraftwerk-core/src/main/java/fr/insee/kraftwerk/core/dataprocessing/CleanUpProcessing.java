package fr.insee.kraftwerk.core.dataprocessing;

import fr.insee.bpm.metadata.model.MetadataModel;
import fr.insee.bpm.metadata.model.PaperUcq;
import fr.insee.bpm.metadata.model.VariablesMap;
import fr.insee.kraftwerk.core.utils.files.FileUtilsInterface;
import fr.insee.kraftwerk.core.utils.KraftwerkExecutionContext;
import fr.insee.kraftwerk.core.vtl.VtlBindings;
import fr.insee.kraftwerk.core.vtl.VtlScript;
import lombok.extern.log4j.Log4j2;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.StringJoiner;


/**
 * Clean up step to be called after the reconciliation step.
 */
@Log4j2
public class CleanUpProcessing extends DataProcessing {

    private Map<String, MetadataModel> metadataModels;

    public CleanUpProcessing(VtlBindings vtlBindings, Map<String, MetadataModel> metadataModels, FileUtilsInterface fileUtilsInterface) {
        super(vtlBindings, fileUtilsInterface);
        this.metadataModels=metadataModels;
    }

    @Override
    public String getStepName() {
        return "CLEAN UP";
    }

    /**
     * Clean up step consists in:
     * - removing the paper ucq variables in the multimode dataset,
     * - removing the corresponding variable objects from the variables object,
     * - removing the unimodal datasets.
     *
     * @param bindingName The name of the multimode dataset.
     * @param userVtlInstructionsPath User vtl script (none for this step).
     */
    @Override
    public String applyVtlTransformations(String bindingName, Path userVtlInstructionsPath, KraftwerkExecutionContext kraftwerkExecutionContext) {
        // Remove paper UCQ variables in vtl multimode dataset
        VtlScript cleanUpScript = generateVtlInstructions(bindingName);
        log.debug("Automated clean up instructions after step {} : {}", getStepName(), cleanUpScript);
        vtlExecute.evalVtlScript(cleanUpScript, vtlBindings, kraftwerkExecutionContext);
        // Remove corresponding variables in VariablesMap
        removePaperUcqVariables();
        // Remove unimodal datasets
        removeUnimodalDatasets();
        return cleanUpScript.toString();
    }

    /** Generate VTL script to remove the paper indicator variables. */
    @Override
    protected VtlScript generateVtlInstructions(String bindingName) {
        VtlScript vtlScript = new VtlScript();
        List<String> paperUcqVtlNames = new ArrayList<>();
        for (Entry<String,MetadataModel> mode : metadataModels.entrySet()) {
            VariablesMap variablesMap = mode.getValue().getVariables();
            paperUcqVtlNames.addAll(
                    variablesMap.getPaperUcq().stream()
                            .map(variable -> mode.getValue().getFullyQualifiedName(variable.getName()))
                            .toList()
            );
        }
        if (!paperUcqVtlNames.isEmpty()) {
            StringBuilder dropInstruction = new StringBuilder(
                    String.format("%s := %s [ drop ", bindingName, bindingName)
            );
            StringJoiner vtlDropVariables = new StringJoiner(", ");
            paperUcqVtlNames.forEach(vtlDropVariables::add);
            dropInstruction.append(vtlDropVariables);
            dropInstruction.append(" ];");
            vtlScript.add(dropInstruction.toString());
        }
        return vtlScript;
    }

    /** Remove PaperUcq variables from concerned VariablesMap */
    private void removePaperUcqVariables() {
        for (MetadataModel metadata : metadataModels.values()) {
            metadata.getVariables().getVariables().values().removeIf(PaperUcq.class::isInstance);
        }
    }

    /** Remove unimodal dataset from the bindings. */
    private void removeUnimodalDatasets() {
        for (String datasetName : metadataModels.keySet()) {
            // unimodal datasets
            vtlBindings.remove(datasetName);
            log.info(String.format("%s unimodal dataset removed from vtl bindings.", datasetName));
            // datasets created during the reconciliation step
            vtlBindings.remove(datasetName + "_keep");
            log.info(String.format("%s generated dataset removed from vtl bindings.", datasetName + "_keep"));
        }
    }
}
