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
 * This processing class is designed to create a dataset containing all reporting dat
 */
public class ReportingDataProcessing extends DataProcessing{
    public ReportingDataProcessing(VtlBindings vtlBindings) {
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
                    Constants.REPORTING_DATA_DATASET_NAME, bindingName, Constants.ROOT_IDENTIFIER_NAME, contactAttemptsMeasures));

            // Remove duplicates
            contactAttemptsInstructions.append(String.format("%1$s := union(%1$s,%1$s);",Constants.REPORTING_DATA_DATASET_NAME));

            vtlScript.add(contactAttemptsInstructions.toString());
        }

        return vtlScript;
    }
}
