package fr.insee.kraftwerk.dataprocessing;

import fr.insee.kraftwerk.vtl.VtlBindings;
import fr.insee.kraftwerk.vtl.VtlScript;

public class MultimodeTransformations extends DataProcessing {

    public MultimodeTransformations(VtlBindings vtlBindings) {
        super(vtlBindings);
    }

    @Override
    public String getStepName() {
        return "MULTIMODE TRANSFORMATIONS";
    }

    @Override
    protected VtlScript generateVtlInstructions(String bindingName, Object... objects) {
        return new VtlScript();
    }
}
