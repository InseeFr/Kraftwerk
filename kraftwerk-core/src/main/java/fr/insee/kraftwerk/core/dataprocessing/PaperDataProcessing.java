package fr.insee.kraftwerk.core.dataprocessing;

import java.util.List;

import fr.insee.kraftwerk.core.metadata.UcqVariable;
import fr.insee.kraftwerk.core.metadata.VariablesMap;
import fr.insee.kraftwerk.core.vtl.VtlBindings;
import fr.insee.kraftwerk.core.vtl.VtlScript;

/**
 * Implementation of the UnimodalDataProcessing class for data from digitized paper.
 */
public class PaperDataProcessing extends UnimodalDataProcessing {

    private VariablesMap variablesMap;
	
    public PaperDataProcessing(VtlBindings vtlBindings, VariablesMap variablesMap) {
        super(vtlBindings);
        this.variablesMap = variablesMap;
    }

    @Override
    public String getStepName() {
        return "Paper";
    }

    /**
     * In data that comes from digitized paper, UCQ (unique choice questions) are split into indicator variables.
     * In data which come from collection tools like Coleman or Sabiane a UCQ corresponds to a single variable.
     * For instance, a variable GENDER becomes GENDER_1 and GENDER_2 in paper data file.
     * This method return VTL instructions to transform split UCQ into a single variable.
     *
     * If several indicators of a same variable are ticked, the value is the last modality ticked.
     * If none is ticked, the value of the result variable will be empty ("").
     *
     * @param bindingName The name of the dataset in the bindings.
     * @param objects      a VariablesMap object is expected here.
     * @return VTL instructions
     */
    @Override
    public VtlScript generateVtlInstructions(String bindingName) {
        // Get UCQ variables
        List<UcqVariable> ucqVariables = variablesMap.getUcqVariables();

        // Write the VTL instructions
        VtlScript vtlScript = new VtlScript();

        for (UcqVariable ucqVariable : ucqVariables) {

            // Init the vtl instruction for the UCQ variable
            StringBuilder vtlInstruction = new StringBuilder(
                    String.format("%s := %s [calc ", bindingName, bindingName)
            );

            // Get UCQ variable name
            String variableVtlName = variablesMap.getFullyQualifiedName(ucqVariable.getName());

            // Get UCQ modalities
            List<UcqVariable.UcqModality> ucqModalities = ucqVariable.getModalities();
            int modalitiesCount = ucqModalities.size();

            // First modality and first line of the VTL instruction
            UcqVariable.UcqModality firstModality = ucqModalities.get(0);
            String firstModalityVtlName = variablesMap.getFullyQualifiedName(firstModality.getVariableName());
            vtlInstruction.append(String.format("%s := if %s = \"1\" then \"%s\" else (%n",
                    variableVtlName, firstModalityVtlName, firstModality.getValue()));

            // Middle lines of the VTL instruction
            for (int k = 1; k < modalitiesCount - 1; k++) {
                UcqVariable.UcqModality modality = ucqModalities.get(k);
                String modalityVtlName = variablesMap.getFullyQualifiedName(modality.getVariableName());
                vtlInstruction.append(String.format("if %s = \"1\" then \"%s\" else (%n",
                        modalityVtlName, modality.getValue()));
            }

            // Last line of the VTL instruction
            UcqVariable.UcqModality lastModality = ucqModalities.get(modalitiesCount - 1);
            String latsModalityVtlName = variablesMap.getFullyQualifiedName(lastModality.getVariableName());
            vtlInstruction.append(String.format("if %s = \"1\" then \"%s\" else \"\" ",
                    latsModalityVtlName, lastModality.getValue()));

            // (add closing parenthesis and ';')
            vtlInstruction.append(")".repeat(modalitiesCount - 1));
            vtlInstruction.append("];");

            vtlScript.add(vtlInstruction.toString());
        }

        return vtlScript;
    }

}
