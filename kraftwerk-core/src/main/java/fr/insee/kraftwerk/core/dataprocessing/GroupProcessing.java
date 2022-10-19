package fr.insee.kraftwerk.core.dataprocessing;

import fr.insee.kraftwerk.core.metadata.VariablesMap;
import fr.insee.kraftwerk.core.vtl.VtlBindings;
import fr.insee.kraftwerk.core.vtl.VtlScript;

public class GroupProcessing extends DataProcessing{
	
    private VariablesMap variablesMap;

    public GroupProcessing(VtlBindings vtlBindings, VariablesMap variablesMap) {
        super(vtlBindings);
        this.variablesMap = variablesMap;
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
     * @param objects The corresponding VariablesMap instance is expected here.
     * @return A VTL script.
     */
    @Override
    protected VtlScript generateVtlInstructions(String bindingName) {

        VtlScript vtlScript = new VtlScript();

        for (String variableName : variablesMap.getVariableNames()) {
            if (variablesMap.getVariable(variableName).getGroup() != variablesMap.getRootGroup()) {
                vtlScript.add(String.format("%s := %s [rename %s to %s];",
                        bindingName, bindingName, variableName, variablesMap.getFullyQualifiedName(variableName)));
            }
        }

        return vtlScript;
    }
}
