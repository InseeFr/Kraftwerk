package fr.insee.kraftwerk.core.outputs;

import fr.insee.kraftwerk.core.Constants;
import fr.insee.kraftwerk.core.KraftwerkError;
import fr.insee.kraftwerk.core.exceptions.KraftwerkException;
import fr.insee.kraftwerk.core.metadata.MetadataModel;
import fr.insee.kraftwerk.core.utils.FileUtils;
import fr.insee.kraftwerk.core.vtl.VtlBindings;
import lombok.Getter;

import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Class to manage the writing of output tables.
 */
@Getter
public abstract class OutputFiles {

	/** Final absolute path of the output folder */
	private final Path outputFolder;
	private final VtlBindings vtlBindings;
	private final Set<String> datasetToCreate = new HashSet<>();

	/**
	 * When an instance is created, the output folder is created.
	 * 
	 * @param outDirectory Out directory defined in application properties.
	 * @param vtlBindings  Vtl bindings where datasets are stored.
	 */
	protected OutputFiles(Path outDirectory, VtlBindings vtlBindings, List<String> modes) {
		this.vtlBindings = vtlBindings;
		setOutputDatasetNames(modes);
		outputFolder = outDirectory;
		createOutputFolder();
	}

	/** Create output folder if doesn't exist. */
	private void createOutputFolder() {
		FileUtils.createDirectoryIfNotExist(outputFolder);
	}

	/** See getOutputDatasetNames doc. */
	private void setOutputDatasetNames(List<String> modes) {
		Set<String> unwantedDatasets = new HashSet<>(modes);
		for (String modeName : modes) { // NOTE: deprecated code since clean up processing class
			unwantedDatasets.add(modeName);
			unwantedDatasets.add(modeName + "_keep"); // datasets created during Reconciliation step
		}
		unwantedDatasets.add(Constants.MULTIMODE_DATASET_NAME);
		for (String datasetName : vtlBindings.getDatasetNames()) {
			if (!unwantedDatasets.contains(datasetName)) {
				datasetToCreate.add(datasetName);
			}
		}
	}

	public void writeImportScripts(Map<String, MetadataModel> metadataModels, List<KraftwerkError> errors) {
		//Should be override
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
	 * @throws KraftwerkException 
	 */
	public void writeOutputTables(Map<String, MetadataModel> metadataModels) throws KraftwerkException {
		// implemented in subclasses
	}

}
