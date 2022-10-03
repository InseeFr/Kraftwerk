package fr.insee.kraftwerk.core.outputs;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import fr.insee.kraftwerk.core.exceptions.KraftwerkException;
import fr.insee.kraftwerk.core.inputs.ModeInputs;
import fr.insee.kraftwerk.core.inputs.UserInputs;
import fr.insee.kraftwerk.core.metadata.VariablesMap;
import fr.insee.kraftwerk.core.utils.DateUtils;
import fr.insee.kraftwerk.core.utils.TextFileWriter;
import fr.insee.kraftwerk.core.vtl.VtlBindings;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * Class to manage the writing of output tables.
 */
@Slf4j
public class OutputFiles {

	private static final String ARCHIVE = "Archive";

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

	/**
	 * Move the input file to another directory to archive it
	 */
	public void renameInputFile(Path inDirectory) {

		File file = inDirectory.resolve("kraftwerk.json").toFile();
		String fileWithTime = "kraftwerk-" + DateUtils.getCurrentTimeStamp() + ".json";
		File file2 = inDirectory.resolve(fileWithTime).toFile();
		if (file2.exists()) {
			log.warn(String.format("Trying to rename '%s' to '%s', but second file already exists.", file, file2));
			log.warn("Timestamped input file will be over-written.");
			try {
				Files.delete(file2.toPath());
			} catch (IOException e) {
				log.error("Can't delete file {}, IOException : {}", fileWithTime, e.getMessage());
			}
		}
		file.renameTo(file2);
	}

	public void moveInputFiles(UserInputs userInputs) throws KraftwerkException {
		//
		Path inputFolder = userInputs.getInputDirectory();
		String[] directories = inputFolder.toString().split(Pattern.quote(File.separator));
		String campaignName = directories[directories.length - 1];

		// First we create an archive directory in case it doesn't exist
		if (!Files.exists(inputFolder.resolve(ARCHIVE))) {
			inputFolder.resolve(ARCHIVE).toFile().mkdir();
		}

		//
		for (String mode : userInputs.getModes()) {
			ModeInputs modeInputs = userInputs.getModeInputs(mode);

			// Move data file or folder in the archive
			// MANAGE DATA
			Path dataPath = modeInputs.getDataFile();
			Path newDataPath = inputFolder.resolve(ARCHIVE).resolve(getRoot(dataPath, campaignName));

			if (!Files.exists(newDataPath)) {
				new File(newDataPath.getParent().toString()).mkdirs();
			}
			if (Files.isRegularFile(dataPath)) {
				moveFile(dataPath, newDataPath);
			} else if (Files.isDirectory(dataPath)) {
				moveDirectory(dataPath.toFile(),
						inputFolder.resolve(ARCHIVE).resolve(getRoot(dataPath, campaignName)).toFile());
			} else {
				log.debug(String.format("No file or directory at path: %s", dataPath));
			}

			// MANAGE PARADATA
			// If paradata, we move the paradata folder
			if (modeInputs.getParadataFolder() != null) {
				moveDirectory(modeInputs.getParadataFolder().toFile(), inputFolder.resolve(ARCHIVE)
						.resolve(getRoot(modeInputs.getParadataFolder(), campaignName)).toFile());
			}

			// MANAGE REPORTINGDATA
			// If reporting data, we move reporting data files
			if (modeInputs.getReportingDataFile() != null) {
				if (!Files
						.exists(inputFolder.resolve(ARCHIVE).resolve(modeInputs.getReportingDataFile()).getParent())) {
					new File(getRoot(modeInputs.getReportingDataFile(), campaignName)).mkdirs();
				}
				moveDirectory(modeInputs.getReportingDataFile().getParent().toFile(), inputFolder.resolve(ARCHIVE)
						.resolve(getRoot(modeInputs.getReportingDataFile(), campaignName)).toFile());
				try {
					Files.delete(modeInputs.getReportingDataFile());
				} catch (IOException e) {
					throw new KraftwerkException(500, "Can't delete file " + modeInputs.getReportingDataFile());

				}
			}
		}
	}

	public void moveFile(Path dataPath, Path newDataPath) throws KraftwerkException {
		try {
			Files.move(dataPath, newDataPath);
		} catch (IOException e) {
			throw new KraftwerkException(500, "Can't move file " + dataPath + " to " + newDataPath);
		}
	}

	private static String getRoot(Path path, String campaignName) {
		String[] directories = path.toString().split(Pattern.quote(File.separator));
		int campaignIndex = Arrays.asList(directories).indexOf(campaignName);
		String[] newDirectories = Arrays.copyOfRange(directories, campaignIndex + 1, directories.length);
		StringBuilder result = new StringBuilder();
		String sep = "";
		for (String directory : newDirectories) {
			result.append(sep).append(directory);
			sep = File.separator;
		}
		return result.toString();
	}

	private static void moveDirectory(File sourceFile, File destFile) throws KraftwerkException {
		if (sourceFile.isDirectory()) {
			File[] files = sourceFile.listFiles();
			assert files != null : "List of files in sourceFile is null";
			for (File file : files)
				moveDirectory(file, new File(destFile, file.getName()));
			try {
				Files.delete(sourceFile.toPath());
			} catch (IOException e) {
				throw new KraftwerkException(500, "Can't delete " + sourceFile + " - IOException : " + e.getMessage());
			}
		} else {
			if (!destFile.getParentFile().exists() && !destFile.getParentFile().mkdirs())
				throw new KraftwerkException(500, "Can't create directory to archive");
			if (!sourceFile.renameTo(destFile))
				throw new KraftwerkException(500, "Can't rename file " + sourceFile + " to " + destFile);
		}
	}

}
