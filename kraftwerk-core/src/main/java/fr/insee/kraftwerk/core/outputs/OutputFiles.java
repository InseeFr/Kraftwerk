package fr.insee.kraftwerk.core.outputs;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import fr.insee.kraftwerk.core.KraftwerkError;
import fr.insee.kraftwerk.core.metadata.VariablesMap;
import fr.insee.kraftwerk.core.outputs.csv.RImportScript;
import fr.insee.kraftwerk.core.outputs.csv.SASImportScript;
import fr.insee.kraftwerk.core.utils.FileUtils;
import fr.insee.kraftwerk.core.utils.TextFileWriter;
import fr.insee.kraftwerk.core.vtl.VtlBindings;
import lombok.Getter;

/**
 * Class to manage the writing of output tables.
 */
public abstract class OutputFiles {

	/** Final absolute path of the output folder */
	@Getter
	private final Path outputFolder;
	@Getter
	private final VtlBindings vtlBindings;

	@Getter
	private final Set<String> datasetToCreate = new HashSet<>();

	/**
	 * When an instance is created, the output folder is created.
	 * 
	 * @param outDirectory Out directory defined in application properties.
	 * @param vtlBindings  Vtl bindings where datasets are stored.
	 * @param userInputs   Used to get the campaign name and to filter intermediate
	 *                     datasets that we don't want to output.
	 */
	protected OutputFiles(Path outDirectory, VtlBindings vtlBindings, List<String> modes, String multimodeDatasetNames) {
		this.vtlBindings = vtlBindings;
		setOutputDatasetNames(modes, multimodeDatasetNames);
		outputFolder = outDirectory;
		createOutputFolder();
	}

	/** Create output folder if doesn't exist. */
	private void createOutputFolder() {
		FileUtils.createDirectoryIfNotExist(outputFolder);
	}

	/** See getOutputDatasetNames doc. */
	private void setOutputDatasetNames(List<String> modes, String multimodeDatasetNames) {
		Set<String> unwantedDatasets = new HashSet<>(modes);
		for (String modeName : modes) { // NOTE: deprecated code since clean up processing class
			unwantedDatasets.add(modeName);
			unwantedDatasets.add(modeName + "_keep"); // datasets created during Reconciliation step
		}
		unwantedDatasets.add(multimodeDatasetNames);
		for (String datasetName : vtlBindings.getDatasetNames()) {
			if (!unwantedDatasets.contains(datasetName)) {
				datasetToCreate.add(datasetName);
			}
		}
	}

	public void writeImportScripts(Map<String, VariablesMap> metadataVariables, List<KraftwerkError> errors) {
		// Assemble required info to write scripts
		List<TableScriptInfo> tableScriptInfoList = new ArrayList<>();
		for (String datasetName : datasetToCreate) {
			TableScriptInfo tableScriptInfo = new TableScriptInfo(datasetName, outputFileName(datasetName),
					vtlBindings.getDataset(datasetName).getDataStructure(), metadataVariables);
			tableScriptInfoList.add(tableScriptInfo);
		}
		// Write scripts
		TextFileWriter.writeFile(outputFolder.resolve("import_with_data_table.R"),
				new RImportScript(tableScriptInfoList).generateScript());
		TextFileWriter.writeFile(outputFolder.resolve("import.sas"),
				new SASImportScript(tableScriptInfoList,errors).generateScript());
	}

	/**
	 * Return the name of the file to be written from the dataset name.
	 */
	public String outputFileName(String datasetName) {
		// implemented in subclasses
		return datasetName;
	}

	/**
	 * Method to write output tables from datasets that are in the bindings.
	 */
	public void writeOutputTables(Map<String, VariablesMap> metadataVariables) {
		// implemented in subclasses
	}

}
