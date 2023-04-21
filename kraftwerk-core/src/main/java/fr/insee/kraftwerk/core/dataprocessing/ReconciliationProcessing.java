package fr.insee.kraftwerk.core.dataprocessing;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import fr.insee.kraftwerk.core.Constants;
import fr.insee.kraftwerk.core.vtl.VtlBindings;
import fr.insee.kraftwerk.core.vtl.VtlMacros;
import fr.insee.kraftwerk.core.vtl.VtlScript;
import fr.insee.vtl.model.Dataset.Role;
import fr.insee.vtl.model.Structured;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class ReconciliationProcessing extends DataProcessing {

	private final String modeVariableIdentifier;

	/** Return processing instance with default mode variable name. */
	public ReconciliationProcessing(VtlBindings vtlBindings) {
		super(vtlBindings);
		this.modeVariableIdentifier = Constants.MODE_VARIABLE_NAME;
	}

	/** Return processing instance with mode variable name given. */
	public ReconciliationProcessing(VtlBindings vtlBindings, String modeVariableName) {
		super(vtlBindings);
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
	 *
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
			String singleInstruction = String.format("%s := %s;", bindingName, vtlBindings.getDatasetNames().get(0));
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

		// Identifiers
		Set<String> identifiers = getIdentifiers();

		// Add mode identifier (as measure for now)
		for (String datasetName : vtlBindings.getDatasetNames()) {
			vtlScript.add(createModeIdentifier(datasetName));
		}

		// Get the common measures
		Set<String> commonMeasures = getCommonMeasures();


		// Cast Integer into Number to ensure numerical measure have the same type
		for (String datasetName : vtlBindings.getDatasetNames()) {
			for (String measure : commonMeasures){
				if (vtlBindings.getMeasureType(datasetName,measure).equals("integer")){
					vtlScript.add(String.format("%1$s := %1$s [calc %2$s := cast(%2$s,number)];",datasetName, measure));
				}
			}
		}

		commonMeasures.add(modeVariableIdentifier);

		// List of all common variables (identifiers + measures) in the vtl syntax
		String vtlCommonVariables = VtlMacros.toVtlSyntax(mergeSets(identifiers, commonMeasures));

		// Instantiate the multimodal dataset with the first dataset that comes
		List<String> unimodalDatasetNames = vtlBindings.getDatasetNames();
		String firstDatasetName = unimodalDatasetNames.get(0);
		vtlScript.add(String.format("%s := %s [keep %s];",
				bindingName, firstDatasetName, vtlCommonVariables));

		// Concatenate the remaining datasets
		for (int k = 1; k < unimodalDatasetNames.size(); k++) {
			String datasetName = unimodalDatasetNames.get(k);
			vtlScript.add(String.format("%s := union(%s, %s [keep %s]);",
					bindingName, bindingName, datasetName, vtlCommonVariables));
		}

		// Get back variables that are not common with left joins
		for (String datasetName : unimodalDatasetNames) {

			// Get the other variables
			Set<String> otherVariables = getOtherVariables(datasetName, commonMeasures);

			if (!otherVariables.isEmpty()) {
				String vtlOtherVariables = VtlMacros.toVtlSyntax(otherVariables);

				// Keep on the dataset to be joined
				vtlScript.add(String.format("%s_keep := %s [keep %s];",
						datasetName, datasetName, vtlOtherVariables));

				// Join
				vtlScript.add(String.format("%s := left_join(%s, %s_keep);",
						bindingName, bindingName, datasetName));
			}
		}

		// Set mode identifier role
		vtlScript.add(String.format("%s := %s [calc identifier %s := %s];",
				bindingName, bindingName, modeVariableIdentifier, modeVariableIdentifier));

		return vtlScript;
	}

	private String createModeIdentifier(String modeName) {
		return String.format("%s := %s [calc %s := \"%s\"];",
				modeName, modeName, modeVariableIdentifier, modeName);
	}


	/** Return a set containing identifiers variable names that are in the bindings datasets. */
	public Set<String> getIdentifiers() {

		Set<String> identifiers = new TreeSet<>();

		for (String datasetName : vtlBindings.keySet()) {
			Structured.DataStructure dataStructure = vtlBindings.getDataset(datasetName).getDataStructure();
			identifiers.addAll( dataStructure.keySet()
					.stream()
					.filter(name -> dataStructure.get(name).getRole() == Role.IDENTIFIER)
					.collect(Collectors.toSet()) );
		}
		return identifiers;
	}

	/** Return a set containing variable names that are common to each datasets in the bindings. */
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
	 *
	 * @param datasetName The name of the dataset.
	 *
	 * @return A VariablesMap object
	 */
	private Set<String> getOtherVariables(String datasetName, Set<String> variablesSet) {
		Set<String> otherVariables = new TreeSet<>();
		for (String variableName : vtlBindings.getDataset(datasetName).getDataStructure().keySet()) {
			if(! variablesSet.contains(variableName)) {
				otherVariables.add(variableName);
			}
		}
		return otherVariables;
	}

	/**
	 * Return a set containing the common strings among the lists given.
	 *
	 * Code sample found here:
	 * https://stackoverflow.com/questions/36110185/how-to-find-common-elements-in-multiple-lists
	 * https://stackoverflow.com/a/36110216/13425151
	 *
	 * @param lists A list containing lists of string.
	 *
	 * @return A set of strings.
	 */
	private Set<String> getCommonElements(List<Set<String>> lists) {
		Set<String> intersection = new HashSet<>(lists.get(0));
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

	/** Merge all sets given in the result set. Given sets are not modified. */
	@SafeVarargs
	private Set<String> mergeSets(Set<String> set1, Set<String>... sets) {
		Set<String> res = new HashSet<>(Set.copyOf(set1));
		for (Set<String> set : sets) {
			res.addAll(set);
		}
		return res;
	}

}
