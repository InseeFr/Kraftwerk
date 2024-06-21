package fr.insee.kraftwerk.core.dataprocessing;

import fr.insee.kraftwerk.core.utils.files.FileUtilsInterface;
import fr.insee.kraftwerk.core.vtl.VtlBindings;
import fr.insee.kraftwerk.core.vtl.VtlScript;

public class MultimodeTransformations extends DataProcessing {

    public MultimodeTransformations(VtlBindings vtlBindings, FileUtilsInterface fileUtilsInterface) {
        super(vtlBindings, fileUtilsInterface);
    }

    @Override
    public String getStepName() {
        return "MULTIMODE TRANSFORMATIONS";
    }

    @Override
    protected VtlScript generateVtlInstructions(String bindingName) {
        return new VtlScript();
    }
}
