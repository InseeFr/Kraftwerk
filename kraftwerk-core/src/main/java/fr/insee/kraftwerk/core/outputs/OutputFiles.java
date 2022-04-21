package fr.insee.kraftwerk.core.outputs;

import fr.insee.kraftwerk.core.inputs.ModeInputs;
import fr.insee.kraftwerk.core.inputs.UserInputs;
import fr.insee.kraftwerk.core.metadata.VariablesMap;
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
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Class to manage the writing of output tables.
 */
@Slf4j
public class OutputFiles {

	/** Final absolute path to the batch output folder */
	@Getter
	private final Path outputFolder;

	private final VtlBindings vtlBindings;

	private final Set<String> datasetToCreate = new HashSet<>();

	/**
	 * When an instance is created, the output folder for the concerned batch
	 * execution is created.
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
		//TextFileWriter.writeFile(outputFolder.resolve("import_base.R"), importScripts.scriptR_base());
		TextFileWriter.writeFile(outputFolder.resolve("import_with_data_table.R"), importScripts.scriptR_dataTable());
		//TextFileWriter.writeFile(outputFolder.resolve("import_with_pandas.py"), importScripts.scriptPython_pandas());
		TextFileWriter.writeFile(outputFolder.resolve("import.sas"), importScripts.scriptSAS());
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
		File file = inDirectory.resolve("kraftwerk.json").toFile();
		File file2 = inDirectory.resolve("kraftwerk-" + sdf.format(timestamp) + ".json").toFile();
		if (file2.exists()) {
			log.warn(String.format("Trying to rename '%s' to '%s', but second file already exists.", file, file2));
			log.warn("Timestamped input file will be over-written.");
			file2.delete();
		}
		file.renameTo(file2);
	}

	public void moveInputFiles(UserInputs userInputs) {
		//
		Path inputFolder = userInputs.getInputDirectory();
		String[] directories = inputFolder.toString().split(Pattern.quote(File.separator));
		String campaignName = directories[directories.length-1];

		// First we create an archive directory in case it doesn't exist
		if (!Files.exists(inputFolder.resolve("Archive"))) {
			inputFolder.resolve("Archive").toFile().mkdir();
		}

		//
		for (String mode : userInputs.getModes()) {
			ModeInputs modeInputs = userInputs.getModeInputs(mode);

			// Move data file or folder in the archive
			Path dataPath = modeInputs.getDataFile();
			Path newDataPath = inputFolder.resolve("Archive").resolve(getRoot(modeInputs.getDataFile(), campaignName));
			if (!Files.exists(newDataPath)) {
				new File(newDataPath.getParent().toString()).mkdirs();
			}

			// TODO: remove following code block (unused)
			// ---------------------------------------------------------------
//			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
//			Timestamp timestamp = new Timestamp(System.currentTimeMillis());
			// get the last modified date and format it to the defined format
//			String nameNewFile = modeInputs.getDataFile().toString();
//			nameNewFile = nameNewFile.substring(0, nameNewFile.lastIndexOf(".")) + "-" + sdf.format(timestamp)
//					+ nameNewFile.substring(nameNewFile.lastIndexOf(".")); // <- throws an exception when data path is a folder (not containing "."), also might not work as expected if some folder name contains a "."
			// ---------------------------------------------------------------

			if (Files.isRegularFile(dataPath)) {
				try {
					Files.move(modeInputs.getDataFile(), newDataPath);
				} catch (IOException e) {
					log.error("Error occurred when trying to move data file or folder in the \"Archive\" directory.");
					log.error(e.getMessage());
				}
			} else if (Files.isDirectory(dataPath)) {
				moveDirectory(
						dataPath.toFile(),
						inputFolder.resolve("Archive").resolve(getRoot(dataPath, campaignName)).toFile());
			} else {
				log.debug(String.format("No file or directory at path: %s", dataPath));
			}

			// If paradata, we move the paradata folder
			if (modeInputs.getParadataFolder() != null // TODO: simplify condition (UserInputs class shouldn't allow field content to equal "" or "null" (should be either a value or null)
					&& !modeInputs.getParadataFolder().toString().contentEquals("")
					&& !modeInputs.getParadataFolder().toString().contentEquals("null")) {
				moveDirectory(
						modeInputs.getParadataFolder().toFile(),
						inputFolder.resolve("Archive")
								.resolve(getRoot(modeInputs.getParadataFolder(), campaignName)).toFile());
			}

			// If reporting data, we move reporting data files
			if (modeInputs.getReportingDataFile() != null // TODO: simplify condition (see above)
					&& !modeInputs.getReportingDataFile().toString().equals("")) {
				if (!Files.exists(inputFolder.resolve("Archive").resolve(modeInputs.getReportingDataFile()).getParent())) {
					new File(getRoot(modeInputs.getReportingDataFile(), campaignName)).mkdirs();
				}
				moveDirectory(
						modeInputs.getReportingDataFile().toFile(),
						inputFolder.resolve("Archive")
								.resolve(getRoot(modeInputs.getReportingDataFile(), campaignName)).toFile());
				new File(modeInputs.getReportingDataFile().toString()).delete();
			}
		}
	}

	private static String getRoot(Path path, String campaignName) {
		String[] directories = path.toString().split(Pattern.quote(File.separator));
		int campaignIndex = Arrays.asList(directories).indexOf(campaignName);
		String[] newDirectories = Arrays.copyOfRange(directories, campaignIndex+1, directories.length);
		StringBuilder result = new StringBuilder();
		String sep = "";
		for (String directory : newDirectories) {
			result.append(sep).append(directory);
			sep = File.separator;
		}
		return result.toString();
	}

	private static void moveDirectory(File sourceFile, File destFile) {
		if (sourceFile.isDirectory()) {
			File[] files = sourceFile.listFiles();
			assert files != null;
			for (File file : files)
				moveDirectory(file, new File(destFile, file.getName()));
			if (!sourceFile.delete()) throw new RuntimeException();
		} else {
			if (!destFile.getParentFile().exists())
				if (!destFile.getParentFile().mkdirs())
					throw new RuntimeException();
			if (!sourceFile.renameTo(destFile))
				throw new RuntimeException();
		}
	}

}
