package fr.insee.kraftwerk.core.dataprocessing;

import fr.insee.bpm.metadata.model.MetadataModel;
import fr.insee.kraftwerk.core.Constants;
import fr.insee.kraftwerk.core.utils.KraftwerkExecutionContext;
import fr.insee.kraftwerk.core.utils.files.FileUtilsInterface;
import fr.insee.kraftwerk.core.vtl.VtlBindings;
import fr.insee.kraftwerk.core.vtl.VtlMacros;
import fr.insee.kraftwerk.core.vtl.VtlScript;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * This processing class is designed to create one dataset per group existing in metadata.
 */
public class InformationLevelsProcessing extends DataProcessing {

	private static final Set<String> fixedIdentifiers = Set.of(
			Constants.SURVEY_UNIT_IDENTIFIER_NAME,
			Constants.QUESTIONNAIRE_STATE_NAME,
			Constants.VALIDATION_DATE_NAME,
			MODE_VARIABLE_NAME
	);

    public InformationLevelsProcessing(VtlBindings vtlBindings,
									   FileUtilsInterface fileUtilsInterface,
									   KraftwerkExecutionContext kraftwerkExecutionContext
	) {
        super(vtlBindings, fileUtilsInterface, kraftwerkExecutionContext);
    }

    @Override
    public String getStepName() {
        return "INFORMATION LEVELS";
    }

	/**
	 * The binding name is the multimodal dataset.
	 * The method generates VTL instructions to create one dataset per group of variables
	 * NOTE: for now, only works with at most one level of group under root group.
	 */
    @Override
    protected VtlScript generateVtlInstructions(String bindingName) {

    	VtlScript vtlScript = new VtlScript();
		
		MetadataModel metadataModel = vtlBindings.getDatasetVariablesMap(bindingName);

		// Root dataset
		Set<String> rootVariableNames = metadataModel.getVariables().getGroupVariableNames(Constants.ROOT_GROUP_NAME);
		Set<String> fixedIdentifiers = extractFixedIdentifiersFromDataset(bindingName);
		addRootDatasetVtlScript(bindingName, rootVariableNames, fixedIdentifiers, vtlScript);

		// To delete duplicates, to be eventually reviewed with a better VTL solution
		addDeduplicateVTLScript(Constants.ROOT_GROUP_NAME, vtlScript);

		//Use the last temp root dataset as definitive
		vtlScript.add("%s := %s;".formatted(
				Constants.ROOT_GROUP_NAME,
				getLastDatasetName(Constants.ROOT_GROUP_NAME)
		));

		// Group datasets
		for (String groupName : metadataModel.getSubGroupNames()) {

			// First init the dataset using measure names, that are fully qualified name
			List<String> groupVariableNames = new ArrayList<>(metadataModel.getVariables().getGroupVariableNames(groupName));
			List<String> groupMeasureNames = groupVariableNames.stream()
					.map(metadataModel::getFullyQualifiedName).toList();
			addGroupDatasetVtlScript(bindingName, groupName, groupMeasureNames, vtlScript);

			// Empty lines are created to produce group level tables and need to be removed
			vtlScript.add(String.format("%s := %s [filter %s<>\"\"];",
					getIncrementedTempDatasetName(groupName), getLastDatasetName(groupName), groupName));
			incrementTempDataset(groupName);

			// To delete duplicates
			addDeduplicateVTLScript(groupName, vtlScript);

			// Then rename fully qualified names into simple names
			for (int j = 0; j < groupVariableNames.size(); j++) {
				String variableName = groupVariableNames.get(j);
				String fullyQualifiedName = groupMeasureNames.get(j);
				vtlScript.add(String.format("%s := %s [rename %s to %s];",
						getIncrementedTempDatasetName(groupName),
						getLastDatasetName(groupName),
						fullyQualifiedName,
						variableName));
				incrementTempDataset(groupName);
			}

			//Use the last temp group dataset as definitive
			vtlScript.add("%s := %s;".formatted(
					groupName,
					getLastDatasetName(groupName)
			));
		}

		return vtlScript;
    }

	private Set<String> extractFixedIdentifiersFromDataset(String datasetName) {
		return new HashSet<>(
				fixedIdentifiers.stream().filter(
						identifier -> vtlBindings.getDataset(datasetName).getMeasureNames().contains(identifier)
				).toList()
		);
	}

	private void addRootDatasetVtlScript(String multimodeDatasetName,
										 Set<String> rootVariableNames,
										 Set<String> fixedIdentifiers,
										 VtlScript vtlScript) {
		StringBuilder rootInstructions = new StringBuilder();

		rootInstructions.append("%s := %s [keep ".formatted(
				getIncrementedTempDatasetName(Constants.ROOT_GROUP_NAME),
				getLastDatasetName(multimodeDatasetName)
		));

		boolean isModeIdentifierPresent =
				vtlBindings.getDataset(multimodeDatasetName).getMeasureNames().contains(MODE_VARIABLE_NAME);

		if(!fixedIdentifiers.isEmpty()){
			String fixedIdentifiersVtl = VtlMacros.toVtlSyntax(fixedIdentifiers);
			rootInstructions.append(fixedIdentifiersVtl);
			if(!rootVariableNames.isEmpty() || isModeIdentifierPresent){
				rootInstructions.append(", ");
			}
		}
		if(!rootVariableNames.isEmpty()){
			String rootMeasuresVtl = VtlMacros.toVtlSyntax(rootVariableNames);
			rootInstructions.append(rootMeasuresVtl);
			if(isModeIdentifierPresent){
				rootInstructions.append(", ");
			}
		}
		if(isModeIdentifierPresent){
			rootInstructions.append(MODE_VARIABLE_NAME);
		}
		rootInstructions.append(" ];");
		vtlScript.add(rootInstructions.toString());
		incrementTempDataset(Constants.ROOT_GROUP_NAME);
	}

	private void addDeduplicateVTLScript(String datasetName, VtlScript vtlScript){
		vtlScript.add("%s := union(%s, %s);".formatted(
				getIncrementedTempDatasetName(datasetName),
				getLastDatasetName(datasetName),
				getLastDatasetName(datasetName)
		));
		incrementTempDataset(datasetName);
	}

	private void addGroupDatasetVtlScript(String multimodeDatasetName,
	                                      String groupName,
	                                      List<String> groupMeasureNames,
	                                      VtlScript vtlScript) {
		StringBuilder groupInstructions = new StringBuilder();

		groupInstructions.append("%s := %s [keep ".formatted(
				getIncrementedTempDatasetName(groupName),
				getLastDatasetName(multimodeDatasetName)
		));

		boolean isModeIdentifierPresent =
				vtlBindings.getDataset(multimodeDatasetName).getMeasureNames().contains(MODE_VARIABLE_NAME);

		if(!groupMeasureNames.isEmpty()){
			String groupMeasuresVtl = VtlMacros.toVtlSyntax(groupMeasureNames);
			groupInstructions.append(groupMeasuresVtl);
			if(isModeIdentifierPresent){
				groupInstructions.append(", ");
			}
		}
		if(isModeIdentifierPresent){
			groupInstructions.append(MODE_VARIABLE_NAME);
		}
		groupInstructions.append(" ];");
		vtlScript.add(groupInstructions.toString());
		incrementTempDataset(groupName);
	}
}
