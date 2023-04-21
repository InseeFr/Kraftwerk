package fr.insee.kraftwerk.core.dataprocessing;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.StringJoiner;
import java.util.stream.Collectors;

import fr.insee.kraftwerk.core.KraftwerkError;
import fr.insee.kraftwerk.core.metadata.PaperUcq;
import fr.insee.kraftwerk.core.metadata.VariablesMap;
import fr.insee.kraftwerk.core.vtl.VtlBindings;
import fr.insee.kraftwerk.core.vtl.VtlScript;
import lombok.extern.log4j.Log4j2;


/**
 * Clean up step to be called after the reconciliation step.
 */
@Log4j2
public class CleanUpProcessing extends DataProcessing {

    private Map<String, VariablesMap> metadataVariables;

    public CleanUpProcessing(VtlBindings vtlBindings, Map<String, VariablesMap> metadataVariables) {
        super(vtlBindings);
        this.metadataVariables=metadataVariables;
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
     * @param objects The metadata object instance is expected here.
     */
    @Override
    public String applyVtlTransformations(String bindingName, Path userVtlInstructionsPath, List<KraftwerkError> errors) {
        // Remove paper UCQ variables in vtl multimode dataset
        VtlScript cleanUpScript = generateVtlInstructions(bindingName);
        log.debug("Automated clean up instructions after step {} : {}", getStepName(), cleanUpScript);
        vtlExecute.evalVtlScript(cleanUpScript, vtlBindings, errors);
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
        for (Entry<String,VariablesMap> mode : metadataVariables.entrySet()) {
            VariablesMap variablesMap = mode.getValue();
            paperUcqVtlNames.addAll(
                    variablesMap.getPaperUcq().stream()
                            .map(variable -> variablesMap.getFullyQualifiedName(variable.getName()))
                            .collect(Collectors.toList())
            );
        }
        if (!paperUcqVtlNames.isEmpty()) {
            StringBuilder dropInstruction = new StringBuilder(
                    String.format("%s := %s [ drop ", bindingName, bindingName)
            );
            StringJoiner vtlDropVariables = new StringJoiner(", ");
            paperUcqVtlNames.forEach(vtlDropVariables::add);
            dropInstruction.append(vtlDropVariables.toString());
            dropInstruction.append(" ];");
            vtlScript.add(dropInstruction.toString());
        }
        return vtlScript;
    }

    /** Remove PaperUcq variables from concerned VariablesMap */
    private void removePaperUcqVariables() {
        for (VariablesMap variablesMap : metadataVariables.values()) {
            variablesMap.getVariables().values().removeIf(PaperUcq.class::isInstance);
        }
    }

    /** Remove unimodal dataset from the bindings. */
    private void removeUnimodalDatasets() {
        for (String datasetName : metadataVariables.keySet()) {
            // unimodal datasets
            vtlBindings.remove(datasetName);
            log.info(String.format("%s unimodal dataset removed from vtl bindings.", datasetName));
            // datasets created during the reconciliation step
            vtlBindings.remove(datasetName + "_keep");
            log.info(String.format("%s generated dataset removed from vtl bindings.", datasetName + "_keep"));
        }
    }
}
