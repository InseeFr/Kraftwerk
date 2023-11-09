package fr.insee.kraftwerk.core.dataprocessing;

import fr.insee.kraftwerk.core.Constants;
import fr.insee.kraftwerk.core.metadata.VariablesMap;
import fr.insee.kraftwerk.core.vtl.VtlBindings;
import fr.insee.kraftwerk.core.vtl.VtlMacros;
import fr.insee.kraftwerk.core.vtl.VtlScript;

import java.util.Set;
import java.util.stream.Collectors;

import static fr.insee.kraftwerk.core.Constants.OUTCOME_ATTEMPT_SUFFIX_NAME;

/**
 * This processing class is designed to create a dataset containing all contact attempts
 */
public class ContactAttemptsProcessing extends DataProcessing{
    public ContactAttemptsProcessing(VtlBindings vtlBindings) {
        super(vtlBindings);
    }

    @Override
    public String getStepName() {
        return "CONTACT ATTEMPTS";
    }

    /**
     * The method generates VTL instructions to create a dataset containing all contact attempts
     * IF there is at least 1 attempt variable
     */
    @Override
    protected VtlScript generateVtlInstructions(String bindingName) {
        VtlScript vtlScript = new VtlScript();

        VariablesMap multimodeVariablesMap = vtlBindings.getDatasetVariablesMap(bindingName);

        // If at least 1 contact attempt variable present in multimode dataset
        if(multimodeVariablesMap.hasVariable(OUTCOME_ATTEMPT_SUFFIX_NAME + "_1")) {

            // Build contact attempts dataset
            StringBuilder contactAttemptsInstructions = new StringBuilder();

            // Remove all variables other than identifier and contact attempts
            Set<String> variableNames = multimodeVariablesMap.getGroupVariableNames(Constants.ROOT_GROUP_NAME);
            Set<String> filteredVariableNames = variableNames.stream().filter(s -> s.startsWith(OUTCOME_ATTEMPT_SUFFIX_NAME)
                    || s.equals(Constants.ROOT_IDENTIFIER_NAME)).collect(Collectors.toSet());

            String contactAttemptsMeasures = VtlMacros.toVtlSyntax(filteredVariableNames);

            contactAttemptsInstructions.append(String.format("%s := %s [keep %s, %s];",
                    Constants.CONTACT_ATTEMPTS_GROUP_NAME, bindingName, Constants.ROOT_IDENTIFIER_NAME, contactAttemptsMeasures));

            // Remove duplicates
            contactAttemptsInstructions.append(String.format("%s := union (%s, %s);",
                    Constants.CONTACT_ATTEMPTS_GROUP_NAME, Constants.CONTACT_ATTEMPTS_GROUP_NAME,Constants.CONTACT_ATTEMPTS_GROUP_NAME));

            vtlScript.add(contactAttemptsInstructions.toString());
        }

        return vtlScript;
    }
}
