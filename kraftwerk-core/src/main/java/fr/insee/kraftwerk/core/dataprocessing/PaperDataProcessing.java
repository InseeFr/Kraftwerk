package fr.insee.kraftwerk.core.dataprocessing;

import fr.insee.kraftwerk.core.metadata.MetadataModel;
import fr.insee.kraftwerk.core.metadata.UcqModality;
import fr.insee.kraftwerk.core.metadata.UcqVariable;
import fr.insee.kraftwerk.core.vtl.VtlBindings;
import fr.insee.kraftwerk.core.vtl.VtlScript;

import java.util.List;

/**
 * Implementation of the UnimodalDataProcessing class for data from digitized paper.
 */
public class PaperDataProcessing extends UnimodalDataProcessing {

    public PaperDataProcessing(VtlBindings vtlBindings, MetadataModel metadataModel) {
        super(vtlBindings, metadataModel);
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
     * @return VTL instructions
     */
    @Override
    public VtlScript generateVtlInstructions(String bindingName) {
        // Get UCQ variables
        List<UcqVariable> ucqVariables = metadataModel.getVariables().getUcqVariables();

        // Write the VTL instructions
        VtlScript vtlScript = new VtlScript();

        // To delete duplicates, to be eventually reviewed with a better VTL solution
        vtlScript.add(String.format("%1$s := union(%1$s,%1$s);",bindingName));

        for (UcqVariable ucqVariable : ucqVariables) {

            // Init the vtl instruction for the UCQ variable
            StringBuilder vtlInstruction = new StringBuilder(
                    String.format("%s := %s [calc ", bindingName, bindingName)
            );

            // Get UCQ variable name
            String variableVtlName = metadataModel.getFullyQualifiedName(ucqVariable.getName());

            // Get UCQ modalities
            List<UcqModality> ucqModalities = ucqVariable.getModalities();
            int modalitiesCount = ucqModalities.size();

            // First modality and first line of the VTL instruction
            UcqModality firstModality = ucqModalities.getFirst();
            String firstModalityVtlName = metadataModel.getFullyQualifiedName(firstModality.getVariableName());
            vtlInstruction.append(String.format("%s := if %s = \"1\" then \"%s\" else (%n",
                    variableVtlName, firstModalityVtlName, firstModality.getValue()));

            // Middle lines of the VTL instruction
            for (int k = 1; k < modalitiesCount - 1; k++) {
                UcqModality modality = ucqModalities.get(k);
                String modalityVtlName = metadataModel.getFullyQualifiedName(modality.getVariableName());
                vtlInstruction.append(String.format("if %s = \"1\" then \"%s\" else (%n",
                        modalityVtlName, modality.getValue()));
            }

            // Last line of the VTL instruction
            UcqModality lastModality = ucqModalities.get(modalitiesCount - 1);
            String latsModalityVtlName = metadataModel.getFullyQualifiedName(lastModality.getVariableName());
            vtlInstruction.append(String.format("if %s = \"1\" then \"%s\" else \"\" ",
                    latsModalityVtlName, lastModality.getValue()));

            // add closing parenthesis and ';'
            vtlInstruction.append(")".repeat(modalitiesCount - 1));
            vtlInstruction.append("];");

            vtlScript.add(vtlInstruction.toString());
        }

        return vtlScript;
    }

}
