package fr.insee.kraftwerk.core.outputs;

import java.io.File;
import java.nio.file.Path;
import java.util.*;

import fr.insee.kraftwerk.core.metadata.VariablesMap;
import fr.insee.kraftwerk.core.outputs.scripts.ImportScript;
import fr.insee.kraftwerk.core.outputs.scripts.RDataTableImportScript;
import fr.insee.kraftwerk.core.outputs.scripts.SASImportScript;
import fr.insee.kraftwerk.core.outputs.scripts.TableScriptInfo;
import fr.insee.kraftwerk.core.utils.FileUtils;
import fr.insee.kraftwerk.core.utils.TextFileWriter;
import fr.insee.kraftwerk.core.vtl.VtlBindings;
import lombok.Getter;

/**
 * Class to manage the writing of output tables.
 */
public class OutputFiles {


	/** Final absolute path of the output folder */
	@Getter
	private final Path outputFolder;

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
	public OutputFiles(Path outDirectory, VtlBindings vtlBindings, List<String> modes, String multimodeDatasetNames) {
		//
		this.vtlBindings = vtlBindings;
		//
		setOutputDatasetNames(modes, multimodeDatasetNames);
		//
		outputFolder = outDirectory;
		//
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

	/**
	 * Method to write CSV output tables from datasets that are in the bindings.
	 */
	public void writeOutputCsvTables() {
		for (String datasetName : datasetToCreate) {
			File outputFile = outputFolder.resolve(outputFileName(datasetName)).toFile();
			if (outputFile.exists()) {
				CsvTableWriter.updateCsvTable(vtlBindings.getDataset(datasetName),
						outputFolder.resolve(outputFileName(datasetName)));
			} else {
				CsvTableWriter.writeCsvTable(vtlBindings.getDataset(datasetName),
						outputFolder.resolve(outputFileName(datasetName)));
			}
		}
	}

	public void writeImportScripts(Map<String, VariablesMap> metadataVariables) {
		// Assemble required info to write scripts
		List<TableScriptInfo> tableScriptInfoList = new ArrayList<>();
		for (String datasetName : datasetToCreate) {
			TableScriptInfo tableScriptInfo = new TableScriptInfo(datasetName, outputFileName(datasetName),
					vtlBindings.getDataset(datasetName).getDataStructure(), metadataVariables);
			tableScriptInfoList.add(tableScriptInfo);
		}
		// Write scripts
		TextFileWriter.writeFile(outputFolder.resolve("import_with_data_table.R"),
				new RDataTableImportScript(tableScriptInfoList).generateScript());
		TextFileWriter.writeFile(outputFolder.resolve("import.sas"),
				new SASImportScript(tableScriptInfoList).generateScript());
	}

	/**
	 * Return the name of the file to be written from the dataset name.
	 */
	public String outputFileName(String datasetName) {
		return outputFolder.getFileName() + "_" + datasetName + ".csv";
	}

}
