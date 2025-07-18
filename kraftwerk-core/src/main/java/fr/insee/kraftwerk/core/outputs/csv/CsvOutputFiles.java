package fr.insee.kraftwerk.core.outputs.csv;

import fr.insee.bpm.metadata.model.MetadataModel;
import fr.insee.kraftwerk.core.Constants;
import fr.insee.kraftwerk.core.encryption.EncryptionUtils;
import fr.insee.kraftwerk.core.exceptions.KraftwerkException;
import fr.insee.kraftwerk.core.outputs.OutputFiles;
import fr.insee.kraftwerk.core.outputs.TableScriptInfo;
import fr.insee.kraftwerk.core.utils.KraftwerkExecutionContext;
import fr.insee.kraftwerk.core.utils.SqlUtils;
import fr.insee.kraftwerk.core.utils.TextFileWriter;
import fr.insee.kraftwerk.core.utils.files.FileUtilsInterface;
import fr.insee.kraftwerk.core.vtl.VtlBindings;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Class to manage the writing of CSV output tables.
 */
@Slf4j
public class CsvOutputFiles extends OutputFiles {

	/**
	 * When an instance is created, the output folder is created.
	 * 
	 * @param outDirectory Out directory defined in application properties.
	 * @param vtlBindings  Vtl bindings where datasets are stored.
	 */
	public CsvOutputFiles(Path outDirectory, VtlBindings vtlBindings, List<String> modes, Statement database, FileUtilsInterface fileUtilsInterface, KraftwerkExecutionContext kraftwerkExecutionContext, EncryptionUtils encryptionUtils) {
		super(outDirectory, vtlBindings, modes, database, fileUtilsInterface, kraftwerkExecutionContext, encryptionUtils);
	}

	/**
	 * Method to write CSV output tables from datasets that are in the bindings.
	 */
	@Override
	public void writeOutputTables() throws KraftwerkException {
		for (String datasetName : getDatasetToCreate()) {
			try {
				//Temporary file
				Files.createDirectories(Path.of(System.getProperty("java.io.tmpdir")));
				Path tmpOutputFile = Files.createTempFile(Path.of(System.getProperty("java.io.tmpdir")),
						outputFileName(datasetName, kraftwerkExecutionContext), null);

				//Get columns
				Map<String,String> columns = SqlUtils.getColumnTypes(getDatabase(), datasetName);

				if(columns.isEmpty()){
					log.warn("dataset {} is empty !", datasetName);
					return;
				}

				//Create file with double quotes header
				Files.write(tmpOutputFile, buildHeader(columns.keySet().stream().toList()).getBytes());

				//Data export into temp file
				StringBuilder exportCsvQuery = getExportCsvQuery(datasetName, tmpOutputFile.toFile(), columns);
				this.getDatabase().execute(exportCsvQuery.toString());

				String outputFile = getOutputFolder().resolve(outputFileName(datasetName, kraftwerkExecutionContext)).toString();
				if (kraftwerkExecutionContext != null) {
					//Count rows for functional log
					try(ResultSet countResult =
								this.getDatabase().executeQuery("SELECT COUNT(*) FROM '%s'".formatted(datasetName))){
						countResult.next();
                        kraftwerkExecutionContext.getLineCountByTableMap().put(datasetName, countResult.getInt(1));
					}

					//Encrypt file if requested
					if(kraftwerkExecutionContext.isWithEncryption()) {
						InputStream encryptedStream = encryptionUtils.encryptOutputFile(tmpOutputFile, kraftwerkExecutionContext);
						getFileUtilsInterface().writeFile(outputFile, encryptedStream, true);
						log.info("File: {} successfully written and encrypted", outputFile);
						continue; //Go to next dataset to write
					}
				}
				//Move to output folder
				getFileUtilsInterface().moveFile(tmpOutputFile, outputFile);
				log.info("File: {} successfully written", outputFile);
			} catch (SQLException | IOException e) {
				throw new KraftwerkException(500, e.toString());
			}
        }
	}

	private static @NotNull StringBuilder getExportCsvQuery(String datasetName, File outputFile, Map<String, String> columnTypes) {
		StringBuilder query = new StringBuilder("COPY (SELECT ");
			int nbColOk = 0;
			for (Entry<String,String> column : columnTypes.entrySet()) {
				String col = column.getKey();
				String type = column.getValue().toUpperCase(); // e.g. 'INTEGER', 'VARCHAR', etc.

				String expression;
				// replaces false/true by 0/1
				if (type.equals("BOOLEAN")) {
					expression = String.format(
							"CASE WHEN \"%1$s\" IS NULL THEN NULL ELSE CASE WHEN \"%1$s\" THEN 1 ELSE 0 END END AS \"%1$s\"",
							col
					);
				} else if (type.contains("CHAR") || type.equals("TEXT")) {
					expression = String.format("COALESCE(\"%1$s\", '') AS \"%1$s\"", col);
				} else {
					expression = String.format("\"%1$s\"", col); // keep null
				}

				query.append(expression);
				if (nbColOk < columnTypes.keySet().size() - 1) {
					query.append(", ");
				}
				nbColOk++;
			}

			query.append(String.format(" FROM \"%s\") TO '%s' (FORMAT CSV, HEADER false, DELIMITER '%s'",
					datasetName,
					outputFile.getAbsolutePath() + "data",
					Constants.CSV_OUTPUTS_SEPARATOR));

			if (!columnTypes.isEmpty()) {
				//Double quote values parameter
				query.append(", FORCE_QUOTE(");
				for (String col : columnTypes.keySet()) {
					query.append("\"").append(col).append("\",");
				}
				query.deleteCharAt(query.length() - 1); // remove trailing comma
				query.append(")");
			}

			query.append(")");
			log.debug("csv query : \n {}",query);
			return query;
	}

	private static String buildHeader(List<String> columnNames) {
		StringBuilder headerBuilder = new StringBuilder();
		for (String columnName : columnNames) {
			headerBuilder.append(String.format("\"%s\"", columnName)).append(Constants.CSV_OUTPUTS_SEPARATOR);
		}
		headerBuilder.deleteCharAt(headerBuilder.length()-1);
		headerBuilder.append("\n");
		return headerBuilder.toString();
	}


	@Override
	public void writeImportScripts(Map<String, MetadataModel> metadataModels, KraftwerkExecutionContext kraftwerkExecutionContext) {
		// Assemble required info to write scripts
		List<TableScriptInfo> tableScriptInfoList = new ArrayList<>();
		for (String datasetName : getDatasetToCreate()) {
			TableScriptInfo tableScriptInfo = new TableScriptInfo(datasetName, outputFileName(datasetName, kraftwerkExecutionContext),
					getVtlBindings().getDataset(datasetName).getDataStructure(), metadataModels);
			tableScriptInfoList.add(tableScriptInfo);
		}
		// Write scripts
		TextFileWriter.writeFile(getOutputFolder().resolve("import_with_data_table.R"),
				new RImportScript(tableScriptInfoList).generateScript(), getFileUtilsInterface());
		TextFileWriter.writeFile(getOutputFolder().resolve("import.sas"),
				new SASImportScript(tableScriptInfoList,
						kraftwerkExecutionContext == null ? new ArrayList<>() :
								kraftwerkExecutionContext.getErrors()
				).generateScript(), getFileUtilsInterface());
	}

	/**
	 * Return the name of the file to be written from the dataset name.
	 */
	@Override
	public String outputFileName(String datasetName, KraftwerkExecutionContext kraftwerkExecutionContext) {
		String output = getOutputFolder().getParent().getFileName() + "_" + datasetName + ".csv";
		return kraftwerkExecutionContext.isWithEncryption() ?
				output + encryptionUtils.getEncryptedFileExtension()
				: output;
	}

}
