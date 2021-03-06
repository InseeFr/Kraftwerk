package fr.insee.kraftwerk.core.dataprocessing;
import fr.insee.kraftwerk.core.vtl.VtlBindings;
import fr.insee.kraftwerk.core.vtl.VtlScript;

/**
 * Implementation of the UnimodalDataProcessing class for Lunatic data.
 */
public class LunaticDataProcessing extends UnimodalDataProcessing {

    public LunaticDataProcessing(VtlBindings vtlBindings) {
        super(vtlBindings);
    }

    @Override
    public String getStepName() {
        return "Lunatic";
    }

    /**
     * There is no VTL automated data processing for data that comes from Lunatic at the moment.
     *
     * @param bindingName The name of the dataset in the bindings.
     *
     * @return ""
     */
    @Override
    public VtlScript generateVtlInstructions(String bindingName, Object... objects) {
        return new VtlScript();
    }

}
