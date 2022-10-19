package fr.insee.kraftwerk.core.dataprocessing;

import fr.insee.kraftwerk.core.vtl.VtlBindings;
import fr.insee.kraftwerk.core.vtl.VtlScript;

/**
 * Implementation of the UnimodalDataProcessing class for Coleman data.
 */
public class XformsDataProcessing extends UnimodalDataProcessing {

    public XformsDataProcessing(VtlBindings vtlBindings) {
        super(vtlBindings);
    }

    @Override
    public String getStepName() {
        return "Coleman";
    }

    /**
     * There is no VTL automated data processing for data that comes from Coleman at the moment.
     *
     * @param bindingName The name of the dataset in the bindings.
     *
     * @return ""
     */
    public VtlScript generateVtlInstructions(String bindingName) {
        return new VtlScript();
    }
}
