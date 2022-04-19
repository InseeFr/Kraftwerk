package fr.insee.kraftwerk.core.outputs;

import fr.insee.kraftwerk.core.Constants;
import fr.insee.kraftwerk.core.inputs.ModeInputs;
import fr.insee.kraftwerk.core.inputs.UserInputs;
import fr.insee.kraftwerk.core.metadata.VariablesMap;
import fr.insee.kraftwerk.core.utils.TextFileWriter;
import fr.insee.kraftwerk.core.vtl.VtlBindings;
import fr.insee.vtl.model.Dataset;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
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
						outputFolder + "/" + outputFileName(datasetName),
						outputFolder.getFileName() + "_" + datasetName, outputFolder);
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

	public void moveInputFile(UserInputs userInputs) {
		Path inputFolder = userInputs.getInputDirectory();
		String[] directories = inputFolder.toString().split(Pattern.quote("\\"));
		String campaignName = directories[directories.length-1];
		Map<String, ModeInputs> modeInputsMap = userInputs.getModeInputsMap();
		// First we create an archive directory in case it doesn't exist
		if (!Files.exists(inputFolder.resolve("Archive"))) {
			new File(inputFolder.resolve("Archive").toString()).mkdir();
		}
		for (String mode : modeInputsMap.keySet()) {
			ModeInputs modeInputs = userInputs.getModeInputs(mode);
			Path pathDataFile = Paths.get(Constants.getResourceAbsolutePath(inputFolder.toString() + "/Archive/" + getRoot(modeInputs.getDataFile(), campaignName)));
			if (!Files.exists(pathDataFile)) {
				new File(pathDataFile.getParent().toString())
				.mkdirs();

			}
			// We then put the old mode file in the archive file
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			Timestamp timestamp = new Timestamp(System.currentTimeMillis());
			// get the last modified date and format it to the defined format
			String nameNewFile = modeInputs.getDataFile().toString();

			nameNewFile = nameNewFile.substring(0, nameNewFile.lastIndexOf(".")) + "-" + sdf.format(timestamp)
					+ nameNewFile.substring(nameNewFile.lastIndexOf("."));
			try {
				Files.move(Paths.get(Constants.getResourceAbsolutePath(modeInputs.getDataFile().toString())),	
						pathDataFile);
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

			// If paradata, we move the paradata
			if (modeInputs.getParadataFolder() != null && !modeInputs.getParadataFolder().toString().contentEquals("") && !modeInputs.getParadataFolder().toString().contentEquals("null")) {
				try {
					moveDirectory(
							new File(modeInputs.getParadataFolder().toString()),
							new File(Constants.getInputPath(inputFolder.toString(), "/Archive", getRoot(modeInputs.getParadataFolder(), campaignName))));
				} catch (Exception e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}

			}
			// If reportingdata, we move the paradata
			if (modeInputs.getReportingDataFile() != null && !modeInputs.getReportingDataFile().toString().equals("")) {
				if (!Files.exists(inputFolder.resolve("Archive").resolve(modeInputs.getReportingDataFile()).getParent())) {
					new File(getRoot(modeInputs.getReportingDataFile(), campaignName).toString())
							.mkdirs();
				}
				try {
					moveDirectory(
							new File(modeInputs.getReportingDataFile().toString()),
							new File(Constants.getInputPath(inputFolder.toString(), "/Archive", getRoot(modeInputs.getReportingDataFile(), campaignName))));
					new File(modeInputs.getReportingDataFile().toString()).delete();
				} catch (Exception e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();

			}

		}
		}

	}
		
	


	private String getRoot(Path path, String campaignName) {
		String[] directories = path.toString().split(Pattern.quote("\\"));
		int indexEnquete = Arrays.asList(directories).indexOf(campaignName);
		String[] newDirectories = Arrays.copyOfRange(directories, indexEnquete+1, directories.length);
		String result = "";
		for (String directory : newDirectories) {
			result = result + "/" + directory;
		}
		return result;
	}
	
	
		private static void moveDirectory(File sourceFile, File destFile) {
			if (sourceFile.isDirectory()) {
				File[] files = sourceFile.listFiles();
				assert files != null;
				for (File file : files)
					moveDirectory(file, new File(destFile, file.getName()));
				if (!sourceFile.delete())throw new RuntimeException();
			} else {
				if (!destFile.getParentFile().exists())
					if (!destFile.getParentFile().mkdirs())
						throw new RuntimeException();
				if (!sourceFile.renameTo(destFile)) 
					throw new RuntimeException();
				
			}
		}
}
