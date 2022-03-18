package fr.insee.kraftwerk.core.outputs;

import fr.insee.kraftwerk.core.inputs.UserInputs;
import fr.insee.kraftwerk.core.utils.TextFileWriter;
import fr.insee.kraftwerk.core.vtl.VtlBindings;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.HashSet;
import java.util.Set;

/**
 * Class to manage the writing of output tables.
 */
@Slf4j
public class OutputTables {

	/** Final absolute path to the batch output folder */
	@Getter
	private final Path outputFolder;

	private final VtlBindings vtlBindings;

	private final Set<String> datasetToCreate = new HashSet<String>();

	/**
	 * When an instance is created, the output folder for the concerned batch
	 * execution is created.
	 * 
	 * @param outDirectory Out directory defined in application properties.
	 * @param vtlBindings  Vtl bindings where datasets are stored.
	 * @param userInputs   Used to get the campaign name and to filter intermediate
	 *                     datasets that we don't want to output.
	 */
	public OutputTables(Path outDirectory, VtlBindings vtlBindings, UserInputs userInputs) {
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
	 * We don't want to output unimodal datasets and the multimode dataset. This
	 * method return the list of dataset names to be output, that are the dataset
	 * created during the information levels processing step (one per group), and
	 * those that might have been created by user VTL instructions.
	 */
	public Set<String> getOutputDatasetNames() {
		return datasetToCreate;
	}

	/**
	 * Method to write CSV output tables from datasets that are in the bindings.
	 */
	public void writeOutputCsvTables() {
		for (String datasetName : datasetToCreate) {
			File outputFile = outputFolder.resolve(outputFileName(datasetName)).toFile();
			if (outputFile.exists()) {
				CsvTableWriter.updateCsvTable(vtlBindings.getDataset(datasetName),
						outputFolder + "/" + outputFileName(datasetName), outputFolder.getFileName() + "_" + datasetName, outputFolder);
			} else {
				CsvTableWriter.writeCsvTable(vtlBindings.getDataset(datasetName),
						outputFolder + "/" + outputFileName(datasetName));
			}
		}
	}

	public void writeImportScripts() {
		//
		ImportScripts importScripts = new ImportScripts();
		//
		for (String datasetName : datasetToCreate) {
			TableScriptInfo tableScriptInfo = new TableScriptInfo(datasetName, outputFileName(datasetName),
					vtlBindings.getDataset(datasetName).getDataStructure());
			importScripts.registerTable(tableScriptInfo);
		}
		//
		TextFileWriter.writeFile(outputFolder + "/import_base.R", importScripts.scriptR_base());
		TextFileWriter.writeFile(outputFolder + "/import_with_data_table.R", importScripts.scriptR_dataTable());
		TextFileWriter.writeFile(outputFolder + "/import_with_pandas.py", importScripts.scriptPython_pandas());
		TextFileWriter.writeFile(outputFolder + "/import.sas", importScripts.scriptSAS());
	}

	/**
	 * Return the name of the file to be written from the dataset name.
	 */
	public String outputFileName(String datasetName) {
		return outputFolder.getFileName() + "_" + datasetName + ".csv";
	}

	/**
	 * Move the input file to another directory to archive it
	 */
	public void renameInputFile(Path inDirectory) {

		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
		Timestamp timestamp = new Timestamp(System.currentTimeMillis());
		Path inPath = inDirectory;
		File file = inPath.resolve("kraftwerk.json").toFile();
		// File (or directory) with new name
		File file2 = inPath.resolve("kraftwerk-" + sdf.format(timestamp) + ".json").toFile();

		if (file2.exists())
			try {
				throw new java.io.IOException("file exists");
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		// Rename file (or directory)
		file.renameTo(file2);
	}

}
