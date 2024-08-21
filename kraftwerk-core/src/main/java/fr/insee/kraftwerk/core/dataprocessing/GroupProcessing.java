package fr.insee.kraftwerk.core.dataprocessing;

import fr.insee.bpm.metadata.model.MetadataModel;

import fr.insee.bpm.metadata.model.VariablesMap;
import fr.insee.kraftwerk.core.utils.files.FileUtilsInterface;
import fr.insee.kraftwerk.core.vtl.VtlBindings;
import fr.insee.kraftwerk.core.vtl.VtlScript;

public class GroupProcessing extends DataProcessing{

	private MetadataModel metadataModel;
	
    public GroupProcessing(VtlBindings vtlBindings, MetadataModel metadataModel, FileUtilsInterface fileUtilsInterface) {
        super(vtlBindings, fileUtilsInterface);
        this.metadataModel = metadataModel;
    }

    @Override
    public String getStepName() {
        return "GROUP PREFIXES";
    }

    /**
     * Rename variables with their "fully qualified name".
     * For each variable that is not in the root group, add a group prefix to its name.
     * Examples:
     * - FIRST_NAME -> INDIVIDUALS_LOOP.FIRST_NAME
     * - CAR_COLOR -> INDIVIDUALS_LOOP.CARS_LOOP.CAR_COLOR
     * @see VariablesMap (getFullyQualifiedName method)
     * @param bindingName The name of the concerned dataset.
     * @return A VTL script.
     */
    @Override
    protected VtlScript generateVtlInstructions(String bindingName) {
        VtlScript vtlScript = new VtlScript();
        VariablesMap variablesMap = metadataModel.getVariables();

        for (String variableName : variablesMap.getVariableNames()) {
            if (variablesMap.getVariable(variableName).getGroup() != metadataModel.getRootGroup()) {
                vtlScript.add(String.format("%s := %s [rename %s to %s];",
                        bindingName, bindingName, variableName, metadataModel.getFullyQualifiedName(variableName)));
            }
        }

        return vtlScript;
    }
}
