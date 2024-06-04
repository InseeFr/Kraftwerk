package fr.insee.kraftwerk.core.outputs.csv;

import fr.insee.kraftwerk.core.KraftwerkError;
import fr.insee.kraftwerk.core.exceptions.KraftwerkException;
import fr.insee.kraftwerk.core.metadata.MetadataModel;
import fr.insee.kraftwerk.core.outputs.OutputFiles;
import fr.insee.kraftwerk.core.outputs.TableScriptInfo;
import fr.insee.kraftwerk.core.utils.TextFileWriter;
import fr.insee.kraftwerk.core.utils.log.KraftwerkExecutionLog;
import fr.insee.kraftwerk.core.vtl.VtlBindings;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Class to manage the writing of CSV output tables.
 */
public class CsvOutputFiles extends OutputFiles {
	private final KraftwerkExecutionLog kraftwerkExecutionLog;

	/**
	 * When an instance is created, the output folder is created.
	 * 
	 * @param outDirectory Out directory defined in application properties.
	 * @param vtlBindings  Vtl bindings where datasets are stored.
	 */
	public CsvOutputFiles(Path outDirectory, VtlBindings vtlBindings, List<String> modes, Statement database) {
		super(outDirectory, vtlBindings, modes, database);
		this.kraftwerkExecutionLog = null;
	}
	public CsvOutputFiles(Path outDirectory, VtlBindings vtlBindings, KraftwerkExecutionLog kraftwerkExecutionLog, List<String> modes, Statement database) {
		super(outDirectory, vtlBindings, modes, database);
		this.kraftwerkExecutionLog = kraftwerkExecutionLog;
	}


	/**
	 * Method to write CSV output tables from datasets that are in the bindings.
	 */
	@Override
	public void writeOutputTables(Map<String, MetadataModel> metadataModels) throws KraftwerkException {
		for (String datasetName : getDatasetToCreate()) {
			File outputFile = getOutputFolder().resolve(outputFileName(datasetName)).toFile();
			try {
				Files.deleteIfExists(outputFile.toPath());
				//Data export
				this.getDatabase().execute(String.format("COPY %s TO '%s' (FORMAT CSV)", datasetName, outputFile.getAbsolutePath()));

				//Count rows for functionnal log
				if (kraftwerkExecutionLog != null) {
					try(ResultSet countResult = this.getDatabase().executeQuery("SELECT COUNT(*) FROM " + datasetName)){
                        assert kraftwerkExecutionLog != null; // Assert because of IDE warning
                        kraftwerkExecutionLog.getLineCountByTableMap().put(datasetName, countResult.getInt(1));
					}
				}
			} catch (Exception e) {
				throw new KraftwerkException(500, e.toString());
			}
		}
	}

	@Override
	public void writeImportScripts(Map<String, MetadataModel> metadataModels, List<KraftwerkError> errors) {
		// Assemble required info to write scripts
		List<TableScriptInfo> tableScriptInfoList = new ArrayList<>();
		for (String datasetName : getDatasetToCreate()) {
			TableScriptInfo tableScriptInfo = new TableScriptInfo(datasetName, outputFileName(datasetName),
					getVtlBindings().getDataset(datasetName).getDataStructure(), metadataModels);
			tableScriptInfoList.add(tableScriptInfo);
		}
		// Write scripts
		TextFileWriter.writeFile(getOutputFolder().resolve("import_with_data_table.R"),
				new RImportScript(tableScriptInfoList).generateScript());
		TextFileWriter.writeFile(getOutputFolder().resolve("import.sas"),
				new SASImportScript(tableScriptInfoList,errors).generateScript());
	}

	/**
	 * Return the name of the file to be written from the dataset name.
	 */
	@Override
	public String outputFileName(String datasetName) {
		return getOutputFolder().getParent().getFileName() + "_" + datasetName + ".csv";
	}

}
