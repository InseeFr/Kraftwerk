package fr.insee.kraftwerk.dataprocessing;

import fr.insee.kraftwerk.metadata.VariablesMap;
import fr.insee.kraftwerk.vtl.VtlBindings;
import fr.insee.kraftwerk.vtl.VtlScript;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import java.util.stream.Collectors;

/**
 * Clean up step to be called after the reconciliation step.
 */
@Slf4j
public class CleanUpProcessing extends DataProcessing {

    private Map<String, VariablesMap> metadataVariables;

    public CleanUpProcessing(VtlBindings vtlBindings) {
        super(vtlBindings);
    }

    @Override
    public String getStepName() {
        return "CLEAN UP";
    }

    /**
     * Clean up step consists in:
     * - removing the paper ucq variables,
     * - removing the unimodal datasets.
     *
     * @param bindingName The name of the multimode dataset.
     * @param userVtlInstructionsPath User vtl script (none for this step).
     * @param objects The batch metadata object instance is expected here.
     */
    @Override
    public void applyVtlTransformations(String bindingName, String userVtlInstructionsPath, Object... objects) {
        // Get the metadata object
        metadataVariables = (Map<String, VariablesMap>) objects[0]; // TODO: better management of metadata object
        // Remove paper UCQ variables
        VtlScript cleanUpScript = generateVtlInstructions(bindingName, metadataVariables);
        log.info(String.format("Automated clean up instructions after step %s:\n%s", getStepName(),
                cleanUpScript));
        vtlBindings.evalVtlScript(cleanUpScript);
        // Remove unimodal datasets
        removeUnimodalDatasets();
    }

    /** Generate VTL script to remove the paper indicator variables. */
    @Override
    protected VtlScript generateVtlInstructions(String bindingName, Object... objects) {
        VtlScript vtlScript = new VtlScript();
        List<String> paperUcqVtlNames = new ArrayList<>();
        for (String modeName : metadataVariables.keySet()) {
            VariablesMap variablesMap = metadataVariables.get(modeName);
            paperUcqVtlNames.addAll(
                    variablesMap.getPaperUcq().stream()
                            .map(variable -> variablesMap.getFullyQualifiedName(variable.getName()))
                            .collect(Collectors.toList())
            );
        }
        if (paperUcqVtlNames.size() > 0) {
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

    /** Remove unimodal dataset from the bindings. */
    private void removeUnimodalDatasets() {
        for (String datasetName : metadataVariables.keySet()) {
            // unimodal datasets
            vtlBindings.getBindings().remove(datasetName);
            log.info(String.format("%s unimodal dataset removed from vtl bindings.", datasetName));
            // datasets created during the reconciliation step
            vtlBindings.getBindings().remove(datasetName + "_keep");
            log.info(String.format("%s generated dataset removed from vtl bindings.", datasetName + "_keep"));
        }
    }
}
