package fr.insee.kraftwerk.core.dataprocessing;

import fr.insee.kraftwerk.core.Constants;
import fr.insee.kraftwerk.core.metadata.VariablesMap;
import fr.insee.kraftwerk.core.vtl.VtlBindings;
import fr.insee.kraftwerk.core.vtl.VtlMacros;
import fr.insee.kraftwerk.core.vtl.VtlScript;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;


/**
 * This processing class is designed to create a dataset containing all reporting datas
 */
public class ReportingDataProcessing extends DataProcessing{

    private Map<String, VariablesMap> metadataVariables;

    public ReportingDataProcessing(VtlBindings vtlBindings) {
        super(vtlBindings);
    }

    public ReportingDataProcessing(VtlBindings vtlBindings, Map<String, VariablesMap> metadataVariables) {
        super(vtlBindings);
        this.metadataVariables = metadataVariables;
    }


    @Override
    public String getStepName() {
        return "CONTACT ATTEMPTS";
    }

    /**
     * The method generates VTL instructions to create a dataset containing all reporting data
     * IF there is at least 1 attempt variable
     */

    protected VtlScript generateVtlInstructions(String bindingName) {
        VtlScript vtlScript = new VtlScript();

        // fetch all reporting data variable names from metadataVariables
        Set<String> variableNames = new HashSet<>();
        for(String mode : metadataVariables.keySet()){
            variableNames.addAll(metadataVariables.get(mode).getGroupVariableNames(Constants.REPORTING_DATA_GROUP_NAME));
        }

        // If at least 1 reporting variable present in multimode dataset
        if(!variableNames.isEmpty()) {

            // Build contact attempts dataset
            StringBuilder contactAttemptsInstructions = new StringBuilder();

            String contactAttemptsMeasures = VtlMacros.toVtlSyntax(variableNames);

            contactAttemptsInstructions.append(String.format("%s := %s [keep %s, %s];",
                    Constants.REPORTING_DATA_GROUP_NAME, bindingName, Constants.ROOT_IDENTIFIER_NAME, contactAttemptsMeasures));

            // Remove duplicates
            contactAttemptsInstructions.append(String.format("%1$s := union(%1$s,%1$s);", Constants.REPORTING_DATA_GROUP_NAME));

            vtlScript.add(contactAttemptsInstructions.toString());
        }

        return vtlScript;
    }
}
