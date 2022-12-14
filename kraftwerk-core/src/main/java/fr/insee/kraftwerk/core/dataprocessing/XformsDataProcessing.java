package fr.insee.kraftwerk.core.dataprocessing;

import fr.insee.kraftwerk.core.metadata.VariablesMap;
import fr.insee.kraftwerk.core.vtl.VtlBindings;
import fr.insee.kraftwerk.core.vtl.VtlScript;

/**
 * Implementation of the UnimodalDataProcessing class for Coleman data.
 */
public class XformsDataProcessing extends UnimodalDataProcessing {

    public XformsDataProcessing(VtlBindings vtlBindings, VariablesMap variablesMap) {
        super(vtlBindings, variablesMap);
    }

    @Override
    public String getStepName() {
        return "Coleman";
    }

    /**
     * There is only the removal of duplicates as VTL automated data processing for data that comes from Coleman at the moment.
     *
     * @param bindingName The name of the dataset in the bindings.
     *
     * @return ""
     */
    public VtlScript generateVtlInstructions(String bindingName) {
        // Write the VTL instructions
        VtlScript vtlScript = new VtlScript();

        // To delete duplicates, to be eventually reviewed with a better VTL solution
        vtlScript.add(String.format("%1$s := union(%1$s,%1$s);",bindingName));
        return vtlScript;
    }
}
