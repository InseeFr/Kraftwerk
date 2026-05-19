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
import java.util.HashSet;
import java.util.List;
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

	private VtlScript severalModesInstructions(String bindingName)  {

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

		// Get the common measures
		Set<String> commonMeasures = getCommonMeasures();


		// Cast Integer into Number to ensure numerical measure have the same type
		for (String datasetName : vtlBindings.getDatasetNames()) {
			for (String measure : commonMeasures){
				if (vtlBindings.getMeasureType(datasetName,measure).equals("integer")){
					vtlScript.add(
							String.format("%s := %s [calc %s := cast(%s,number)];",
									getIncrementedTempDatasetName(datasetName),
                                    getTempDatasetName(datasetName),
									measure,
									measure)
					);
					incrementTempDataset(datasetName);
				}
			}
		}

		commonMeasures.add(modeVariableIdentifier);

		// List of all common variables (measures) in the vtl syntax
		// EDIT Since VTL 2.1.0 we cannot use identifiers in keep/drop
		String vtlCommonVariables = VtlMacros.toVtlSyntax(commonMeasures);

		// Instantiate the multimodal dataset with the first dataset that comes
		List<String> unimodalDatasetNames = vtlBindings.getDatasetNames();
		String firstDatasetName = unimodalDatasetNames.getFirst();
		vtlScript.add(String.format("%s := %s [keep %s];",
				bindingName, tempDatasetNames.getOrDefault(firstDatasetName, firstDatasetName), vtlCommonVariables));

		// Concatenate the remaining datasets
		for (int k = 1; k < unimodalDatasetNames.size(); k++) {
			String unimodalDatasetName = unimodalDatasetNames.get(k);
			String tempUnimodalDatasetName = tempDatasetNames.getOrDefault(unimodalDatasetName, unimodalDatasetName);
			String incrementedTempDatasetName = getIncrementedTempDatasetName(bindingName);
			vtlScript.add(String.format("%s := union(%s, %s [keep %s]);",
					incrementedTempDatasetName,
					tempDatasetNames.getOrDefault(bindingName, bindingName),
					tempUnimodalDatasetName,
					vtlCommonVariables));
			incrementTempDataset(bindingName);
		}

		// Get back variables that are not common with left joins
		for (String datasetName : unimodalDatasetNames) {

			// Get the other variables
			Set<String> otherVariables = getOtherVariables(datasetName, commonMeasures);

			if (!otherVariables.isEmpty()) {
				String vtlOtherVariables = VtlMacros.toVtlSyntax(otherVariables);

				// Keep on the dataset to be joined
				vtlScript.add(String.format("%s_keep := %s [keep %s];",
						getIncrementedTempDatasetName(datasetName),
						getTempDatasetName(datasetName),
						vtlOtherVariables));
				incrementTempDataset(datasetName);

				// Join
				vtlScript.add(String.format("%s := left_join(%s, %s_keep);",
						getIncrementedTempDatasetName(bindingName),
						getTempDatasetName(bindingName),
						getTempDatasetName(datasetName)));
				incrementTempDataset(bindingName);
			}
		}

		// Set mode identifier role
		vtlScript.add(String.format("%s := %s [calc identifier %s := %s];",
				getIncrementedTempDatasetName(bindingName), getTempDatasetName(bindingName),
				modeVariableIdentifier,
				modeVariableIdentifier));
		incrementTempDataset(bindingName);

		return vtlScript;
	}

	/**
	 * Creates the mode identifier into the mode dataset
	 * @return the VTL script that creates that identifier into a temp dataset
	 */
	private String createModeIdentifier(String modeName) {
		return String.format("%s := %s [calc %s := \"%s\"];",
				getIncrementedTempDatasetName(modeName),
				getTempDatasetName(modeName),
				modeVariableIdentifier,
				modeName);
	}

	/** Return a set containing variable names that are common to each dataset in the bindings. */
	private Set<String> getCommonMeasures() {

		List<Set<String>> variableNamesList = new ArrayList<>();

		for (String datasetName : vtlBindings.keySet()) {
			Structured.DataStructure dataStructure = vtlBindings.getDataset(datasetName).getDataStructure();
			variableNamesList.add( dataStructure.keySet()
					.stream().filter(name -> dataStructure.get(name).getRole() == Role.MEASURE)
					.collect(Collectors.toSet()) );
		}

		return getCommonElements(variableNamesList);
	}

	/**
	 * Return a variables map containing variables of a dataset that aren't in some
	 * (unimodal) datasets that are in the bindings.
	 * We exclude identifiers as for VTL 2.1 we have to exclude them from keep
	 *
	 * @param datasetName The name of the dataset.
	 *
	 * @return A VariablesMap object
	 */
	private Set<String> getOtherVariables(String datasetName, Set<String> variablesSet) {
		Set<String> otherVariables = new TreeSet<>();
		for (String variableName : vtlBindings.getDataset(datasetName).getMeasureNames()) {
			if(!variablesSet.contains(variableName)) {
				otherVariables.add(variableName);
			}
		}
		return otherVariables;
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
}
