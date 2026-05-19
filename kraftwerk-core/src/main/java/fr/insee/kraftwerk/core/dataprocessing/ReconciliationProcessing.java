package fr.insee.kraftwerk.core.dataprocessing;

import fr.insee.kraftwerk.core.Constants;
import fr.insee.kraftwerk.core.utils.KraftwerkExecutionContext;
import fr.insee.kraftwerk.core.utils.files.FileUtilsInterface;
import fr.insee.kraftwerk.core.vtl.VtlBindings;
import fr.insee.kraftwerk.core.vtl.VtlMacros;
import fr.insee.kraftwerk.core.vtl.VtlScript;
import fr.insee.vtl.model.Dataset.Role;
import fr.insee.vtl.model.Structured;
import lombok.extern.log4j.Log4j2;

import java.util.*;
import java.util.stream.Collectors;

@Log4j2
public class ReconciliationProcessing extends DataProcessing {

	private final String modeVariableIdentifier;

	//To keep track of last temp dataset names
	private final Map<String, String> tempDatasetNames = new HashMap<>();

	/** Return processing instance with default mode variable name. */
	public ReconciliationProcessing(
			VtlBindings vtlBindings,
			FileUtilsInterface fileUtilsInterface,
			KraftwerkExecutionContext kraftwerkExecutionContext
	) {
		super(vtlBindings, fileUtilsInterface, kraftwerkExecutionContext);
		this.modeVariableIdentifier = Constants.MODE_VARIABLE_NAME;
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
			tempDatasetNames.put(datasetName, getIncrementedTempDatasetName(datasetName));
		}

		// Get the common measures
		Set<String> commonMeasures = getCommonMeasures();


		// Cast Integer into Number to ensure numerical measure have the same type
		for (String datasetName : vtlBindings.getDatasetNames()) {
			for (String measure : commonMeasures){
				if (vtlBindings.getMeasureType(datasetName,measure).equals("integer")){
					String incrementedDatasetName = getIncrementedTempDatasetName(datasetName);
					vtlScript.add(
							String.format("%s := %s [calc %s := cast(%s,number)];",
									incrementedDatasetName,
                                    tempDatasetNames.getOrDefault(datasetName, datasetName),
									measure,
									measure)
					);
					tempDatasetNames.put(datasetName, incrementedDatasetName);
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
			tempDatasetNames.put(bindingName, incrementedTempDatasetName);
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
						tempDatasetNames.getOrDefault(datasetName, datasetName),
						vtlOtherVariables));
				tempDatasetNames.put(datasetName, getIncrementedTempDatasetName(datasetName));

				// Join
				vtlScript.add(String.format("%s := left_join(%s, %s_keep);",
						getIncrementedTempDatasetName(bindingName),
						tempDatasetNames.getOrDefault(bindingName, bindingName),
						tempDatasetNames.getOrDefault(datasetName, datasetName)));
				tempDatasetNames.put(bindingName, getIncrementedTempDatasetName(bindingName));
			}
		}

		// Set mode identifier role
		vtlScript.add(String.format("%s := %s [calc identifier %s := %s];",
				getIncrementedTempDatasetName(bindingName), tempDatasetNames.getOrDefault(bindingName, bindingName),
				modeVariableIdentifier,
				modeVariableIdentifier));
		tempDatasetNames.put(bindingName, getIncrementedTempDatasetName(bindingName));

		return vtlScript;
	}

	private String createModeIdentifier(String modeName) {
		return String.format("%s := %s [calc %s := \"%s\"];",
				getIncrementedTempDatasetName(modeName),
				tempDatasetNames.getOrDefault(modeName, modeName),
				modeVariableIdentifier,
				modeName);
	}

	private String getIncrementedTempDatasetName(String datasetName) {
		if(!tempDatasetNames.containsKey(datasetName)){
			return datasetName + TEMP_DATASET_SUFFIX + 0;
		}

		OptionalInt tempDatasetNumberOptional = extractTempDatasetNumber(tempDatasetNames.get(datasetName));
		if(tempDatasetNumberOptional.isPresent()){
			return datasetName + TEMP_DATASET_SUFFIX + (tempDatasetNumberOptional.getAsInt() + 1);
		}

		//If number not found at end
		return datasetName + TEMP_DATASET_SUFFIX + 0;
	}

	/**
	 * Extracts number from a temporary dataset name
	 * @param tempDatasetName name of dataset
	 * @return the number at the end
	 */
	private OptionalInt extractTempDatasetNumber(String tempDatasetName) {
		int i = tempDatasetName.length() - 1;

		//Decrement character index until it finds a non digit character
		while (i >= 0 && Character.isDigit(tempDatasetName.charAt(i))) {
			i--;
		}
		// No number found
		if (i == tempDatasetName.length() - 1) {
			return OptionalInt.empty();
		}
		// Number found, parse it and put it into optional
		return OptionalInt.of(Integer.parseInt(tempDatasetName.substring(i + 1)));
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
