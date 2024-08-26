package fr.insee.kraftwerk.core.dataprocessing;

import fr.insee.bpm.metadata.model.MetadataModel;
import fr.insee.kraftwerk.core.utils.files.FileUtilsInterface;
import fr.insee.kraftwerk.core.vtl.VtlBindings;
import fr.insee.kraftwerk.core.vtl.VtlScript;

/**
 * Interface to apply automated unimodal VTL instructions.
 */
public abstract class UnimodalDataProcessing extends DataProcessing {
	
	protected MetadataModel metadataModel;

    protected UnimodalDataProcessing(VtlBindings vtlBindings, MetadataModel metadataModel, FileUtilsInterface fileUtilsInterface){
        super(vtlBindings, fileUtilsInterface);
        this.metadataModel = metadataModel;
    }

    /**
     * Return the automated VTL instructions for the given dataset name and its variables,
     * depending on which data collection tool the data is from.
     *
     * @see DataProcessingManager to get the adapter data processing class.
     *
     * @param bindingName The name of the dataset in the bindings.
     *
     * @return a String of VTL instructions
     */
    public abstract VtlScript generateVtlInstructions(String bindingName);
}
