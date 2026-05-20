package fr.insee.kraftwerk.core.dataprocessing;

import fr.insee.kraftwerk.core.utils.KraftwerkExecutionContext;
import fr.insee.kraftwerk.core.utils.files.FileUtilsInterface;
import fr.insee.kraftwerk.core.vtl.VtlBindings;
import fr.insee.kraftwerk.core.vtl.VtlMacros;
import fr.insee.kraftwerk.core.vtl.VtlScript;
import fr.insee.vtl.model.Dataset.Role;
import fr.insee.vtl.model.Structured;
import lombok.extern.log4j.Log4j2;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

@Log4j2
public class ReconciliationProcessing extends DataProcessing {

	private final String modeVariableIdentifier;

	/** Return processing instance with default mode variable name. */
	public ReconciliationProcessing(
			VtlBindings vtlBindings,
			FileUtilsInterface fileUtilsInterface,
			KraftwerkExecutionContext kraftwerkExecutionContext
	) {
		super(vtlBindings, fileUtilsInterface, kraftwerkExecutionContext);
		this.modeVariableIdentifier = MODE_VARIABLE_NAME;
	}

	/** Return processing instance with mode variable name given. */
	public ReconciliationProcessing(VtlBindings vtlBindings,
									String modeVariableName,
									FileUtilsInterface fileUtilsInterface,
									KraftwerkExecutionContext kraftwerkExecutionContext
	) {
		super(vtlBindings, fileUtilsInterface, kraftwerkExecutionContext);
		this.modeVariableIdentifier = modeVariableName;
	}

	@Override
	public String getStepName() {
		return "RECONCILIATION";
	}

	/**
	 * Generate VTL instructions to aggregate all the unimodal datasets. The binding
	 * name here is the name of the dataset that will be created, which is the
	 * concatenation of all datasets that are in the bindings when this method is
	 * called.
	 * A new identifier variable is created to identify the original collection mode of each row.
	 * Variables that does not exist in every mode are in the output dataset, with
	 * empty values for the lines of the unconcerned modes.
	 *
	 * @param bindingName The name of the dataset that will be created.
	 *
	 * @return VTL instructions to aggregate all the unimodal datasets.
	 */
	@Override
	protected VtlScript generateVtlInstructions(String bindingName) {
		int modesCount = vtlBindings.size();
		if (modesCount == 1) {
			String singleInstruction = String.format("%s := %s;", bindingName, vtlBindings.getDatasetNames().getFirst());
			return new VtlScript(singleInstruction);
		} else if (modesCount > 1) {
			return severalModesInstructions(bindingName);
		} else {
			log.debug(String.format(
					"Invalid number of datasets in the bindings (%d) at reconciliation step", modesCount));
			return new VtlScript();
		}

	}

	private VtlScript severalModesInstructions(String multimodeBindingName)  {

		/*
		 * The idea of the generated vtl instructions is as follows :
		 *
		 * 1- Create the multimodal dataset as the union (vtl concatenation function) of
		 * each unimodal dataset, on which we make a keep to select the variables that
		 * are common in each mode.
		 *
		 * 2- Make a left_join of each unimodal dataset (on which we drop the common
		 * variable this time) on the multimode one to get back mode-specific variables.
		 */

		VtlScript vtlScript = new VtlScript();

		// Add mode identifier (as measure for now)
		for (String datasetName : vtlBindings.getDatasetNames()) {
			vtlScript.add(createModeIdentifier(datasetName));
			incrementTempDataset(datasetName);
		}

		// Get the common measures between all datasets
		Set<String> commonMeasuresAllDatasets = getCommonMeasures();
		
		// Cast Integer into Number to ensure numerical measures have the same type
		addCastToNumberScripts(commonMeasuresAllDatasets, vtlScript);

		// List of all common variables (measures) in the vtl syntax
		// Since VTL 2.1.0 we cannot use identifiers in keep/drop
		String vtlCommonVariablesAllDatasets = VtlMacros.toVtlSyntax(commonMeasuresAllDatasets);

		// Instantiate the multimodal dataset with the first dataset that comes
		// and only the common variables between all unimode datasets
		List<String> unimodalDatasetNames = vtlBindings.getDatasetNames();
		String firstDatasetName = unimodalDatasetNames.getFirst();
		vtlScript.add(String.format("%s := %s [keep %s];",
				multimodeBindingName,
				getLastDatasetName(firstDatasetName),
				vtlCommonVariablesAllDatasets));

		// Concatenate the remaining datasets using union
		for (String modeDatasetName : unimodalDatasetNames) {
			String vtlCommonVariables = VtlMacros.toVtlSyntax(commonMeasuresAllDatasets);
			vtlScript.add(String.format("%s := union(%s, %s [keep %s]);",
					getIncrementedTempDatasetName(multimodeBindingName),
					getLastDatasetName(multimodeBindingName),
					getLastDatasetName(modeDatasetName),
					vtlCommonVariables));
			incrementTempDataset(multimodeBindingName);
		}

		Map<String, String> addedVariablesNamesAndType = new HashMap<>();
		// Get back variables that are not common between all datasets
		for (String modeDatasetName : unimodalDatasetNames) {
			Set<String> modeMeasures = new HashSet<>(vtlBindings.getDataset(modeDatasetName).getMeasureNames());

			//Get variables already present both in mode and multimodal DS
			Set<String> commonMeasures = getCommonElements(List.of(modeMeasures, addedVariablesNamesAndType.keySet()));
			commonMeasures.addAll(commonMeasuresAllDatasets);

			if(!commonMeasures.isEmpty()) {
				//Calc variables present in multimode but not in mode
				Set<String> multimodeMeasuresToCalc = getAbsentElements(
					addedVariablesNamesAndType.keySet(),
					modeMeasures
				);
				if(!multimodeMeasuresToCalc.isEmpty()) {
					vtlScript.add(getCalcEmptyVariablesScript(
									modeDatasetName,
									multimodeMeasuresToCalc,
									addedVariablesNamesAndType));
					incrementTempDataset(modeDatasetName);
				}
				commonMeasures.addAll(multimodeMeasuresToCalc);

				//Union on common variables
				String vtlCommonVariables = VtlMacros.toVtlSyntax(commonMeasures);
				vtlScript.add(String.format("%s := union(%s, %s [keep %s]);",
						getIncrementedTempDatasetName(multimodeBindingName),
						getLastDatasetName(multimodeBindingName),
						getLastDatasetName(modeDatasetName),
						vtlCommonVariables));
				incrementTempDataset(multimodeBindingName);
			}

			// Get the other variables
			Set<String> allCommonVariables = new HashSet<>(commonMeasuresAllDatasets);
			allCommonVariables.addAll(addedVariablesNamesAndType.keySet());
			Set<String> variablesToAdd = getAbsentElements(modeMeasures, allCommonVariables);

			if (!variablesToAdd.isEmpty()) {
				String vtlOtherVariables = VtlMacros.toVtlSyntax(variablesToAdd);

				// Keep on the dataset to be joined
				vtlScript.add(String.format("%s_keep := %s [keep %s];",
						getIncrementedTempDatasetName(modeDatasetName),
						getLastDatasetName(modeDatasetName),
						vtlOtherVariables));
				incrementTempDataset(modeDatasetName);

				// Join
				vtlScript.add(String.format("%s := left_join(%s, %s_keep);",
						getIncrementedTempDatasetName(multimodeBindingName),
						getLastDatasetName(multimodeBindingName),
						getLastDatasetName(modeDatasetName)));
				incrementTempDataset(multimodeBindingName);

				for(String variableToAdd : variablesToAdd){
					addedVariablesNamesAndType.put(variableToAdd,
							vtlBindings.getMeasureType(modeDatasetName, variableToAdd));
				}
			}
		}

		// Set mode identifier role
		vtlScript.add(String.format("%s := %s [calc identifier %s := %s];",
				getIncrementedTempDatasetName(multimodeBindingName), getLastDatasetName(multimodeBindingName),
				modeVariableIdentifier,
				modeVariableIdentifier));
		incrementTempDataset(multimodeBindingName);

		return vtlScript;
	}

	/**
	 * Creates the mode identifier into the mode dataset
	 * @return the VTL script that creates that identifier into a temp dataset
	 */
	private String createModeIdentifier(String modeName) {
		return String.format("%s := %s [calc %s := \"%s\"];",
				getIncrementedTempDatasetName(modeName),
				getLastDatasetName(modeName),
				modeVariableIdentifier,
				modeName);
	}

	/** Return a set containing variable names that are common to each dataset in the bindings. */
	private Set<String> getCommonMeasures() {

		List<Set<String>> variableNamesList = new ArrayList<>();

		for (String datasetName : vtlBindings.keySet()) {
			Structured.DataStructure dataStructure = vtlBindings.getDataset(datasetName).getDataStructure();
			Set<String> variableNames = dataStructure.keySet()
					.stream()
					.filter(name -> dataStructure.get(name).getRole() == Role.MEASURE)
					.collect(Collectors.toCollection(HashSet::new));
			variableNames.add(modeVariableIdentifier);
			variableNamesList.add(variableNames);
		}
		return getCommonElements(variableNamesList);
	}

	/**
	 * Adds scripts to cast integers to numbers for given measures on ALL datasets
	 * @param measureNames measures to affect
	 * @param vtlScript list of VTL scripts to add to
	 */
	private void addCastToNumberScripts(Set<String> measureNames, VtlScript vtlScript) {
		for (String datasetName : vtlBindings.getDatasetNames()) {
			for (String measure : measureNames){
				if (
						vtlBindings.getDataset(datasetName).getMeasureNames().contains(measure)
								&& vtlBindings.getMeasureType(datasetName, measure).equals("integer")
				) {
					vtlScript.add(
							String.format("%s := %s [calc %s := cast(%s,number)];",
									getIncrementedTempDatasetName(datasetName),
									getLastDatasetName(datasetName),
									measure,
									measure)
					);
					incrementTempDataset(datasetName);
				}
			}
		}
	}

	/**
	 * Return a set containing Strings that are NOT in the second one
	 *
	 * @param sourceSet The name of the source set.
	 * @param comparedSet Second set to compare from
	 *
	 * @return A set of string that are in the first one but not in the second set
	 */
	private Set<String> getAbsentElements(
			Set<String> sourceSet,
			Set<String> comparedSet
	) {
		Set<String> absentStrings = new TreeSet<>();
		for(String element : sourceSet){
			if(!comparedSet.contains(element)){
				absentStrings.add(element);
			}
		}
		return absentStrings;
	}

	/**
	 * Return a set containing the common strings among the lists given.
	 * Code sample found here:
	 * <a href="https://stackoverflow.com/questions/36110185/how-to-find-common-elements-in-multiple-lists">...</a>
	 * <a href="https://stackoverflow.com/a/36110216/13425151">...</a>
	 *
	 * @param lists A list containing lists of string.
	 *
	 * @return A set of strings.
	 */
	private Set<String> getCommonElements(List<Set<String>> lists) {
		Set<String> intersection = new HashSet<>(lists.getFirst());
		for (Set<String> list : lists) {
			Set<String> newIntersection = new HashSet<>();
			for (String name : list) {
				if (intersection.contains(name)) {
					newIntersection.add(name);
				}
			}
			intersection = newIntersection;
		}
		return intersection;
	}

	/**
	 * @param modeDatasetName Dataset to add to
	 * @param multimodeMeasuresToCalc Measures to add empty value
	 * @param variablesNamesAndType A map containing the types of the multimode measures to calc (used in cast)
	 * @return a VTL script that adds empty measures to a map
	 */
	private String getCalcEmptyVariablesScript(String modeDatasetName,
	                                           Set<String> multimodeMeasuresToCalc,
	                                           Map<String, String> variablesNamesAndType) {
		if(multimodeMeasuresToCalc.isEmpty()){
			return null;
		}

		StringBuilder stringBuilder = new StringBuilder("%s := %s [calc ".formatted(
				getIncrementedTempDatasetName(modeDatasetName),
				getLastDatasetName(modeDatasetName)
		));
		for(String multimodeMeasure : multimodeMeasuresToCalc
		){
			stringBuilder.append("%s := cast(null, ".formatted(multimodeMeasure));
			String multimodeMeasureType = variablesNamesAndType.get(multimodeMeasure);
			stringBuilder.append("%s), ".formatted(multimodeMeasureType));
		}
		//Delete last ", " and replace with "];"
		stringBuilder.delete(stringBuilder.length() - 2, stringBuilder.length() - 1);
		stringBuilder.append("];");

		return stringBuilder.toString();
	}
}
