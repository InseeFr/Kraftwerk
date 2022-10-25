package fr.insee.kraftwerk.core.outputs;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import fr.insee.kraftwerk.core.inputs.UserInputs;
import fr.insee.kraftwerk.core.metadata.VariablesMap;
import fr.insee.kraftwerk.core.utils.TextFileWriter;
import fr.insee.kraftwerk.core.vtl.VtlBindings;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * Class to manage the writing of output tables.
 */
@Slf4j
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
	public OutputFiles(Path outDirectory, VtlBindings vtlBindings, UserInputs userInputs) {
		//
		this.vtlBindings = vtlBindings;
		//
		setOutputDatasetNames(userInputs);
		//
		outputFolder = outDirectory;
		//
		createOutputFolder();
	}

	/** Create output folder if doesn't exist. */
	private void createOutputFolder() {
		try {
			Files.createDirectories(outputFolder);
			log.info(String.format("Created output folder: %s", outputFolder.toFile().getAbsolutePath()));
		} catch (IOException e) {
			log.error("Permission refused to create output folder: " + outputFolder, e);
		}
	}

	/** See getOutputDatasetNames doc. */
	private void setOutputDatasetNames(UserInputs userInputs) {
		Set<String> unwantedDatasets = new HashSet<>(userInputs.getModes());
		for (String modeName : userInputs.getModes()) { // NOTE: deprecated code since clean up processing class
			unwantedDatasets.add(modeName);
			unwantedDatasets.add(modeName + "_keep"); // datasets created during Reconciliation step
		}
		unwantedDatasets.add(userInputs.getMultimodeDatasetName());
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
		//
		ImportScripts importScripts = new ImportScripts();
		//
		for (String datasetName : datasetToCreate) {
			TableScriptInfo tableScriptInfo = new TableScriptInfo(datasetName, outputFileName(datasetName),
					vtlBindings.getDataset(datasetName).getDataStructure(), metadataVariables);
			importScripts.registerTable(tableScriptInfo);
		}
		// NOTE: commented unimplemented scripts
		// TextFileWriter.writeFile(outputFolder.resolve("import_base.R"),
		// importScripts.scriptR_base());
		TextFileWriter.writeFile(outputFolder.resolve("import_with_data_table.R"), importScripts.scriptR_dataTable());
		// TextFileWriter.writeFile(outputFolder.resolve("import_with_pandas.py"),
		// importScripts.scriptPython_pandas());
		TextFileWriter.writeFile(outputFolder.resolve("import.sas"), importScripts.scriptSAS());
	}

	/**
	 * Return the name of the file to be written from the dataset name.
	 */
	public String outputFileName(String datasetName) {
		return outputFolder.getFileName() + "_" + datasetName + ".csv";
	}

	



}
