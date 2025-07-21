package fr.insee.kraftwerk.core.outputs.parquet;

import fr.insee.bpm.metadata.model.MetadataModel;
import fr.insee.kraftwerk.core.encryption.EncryptionUtils;
import fr.insee.kraftwerk.core.exceptions.KraftwerkException;
import fr.insee.kraftwerk.core.outputs.OutputFiles;
import fr.insee.kraftwerk.core.outputs.TableScriptInfo;
import fr.insee.kraftwerk.core.utils.KraftwerkExecutionContext;
import fr.insee.kraftwerk.core.utils.TextFileWriter;
import fr.insee.kraftwerk.core.utils.files.FileUtilsInterface;
import fr.insee.kraftwerk.core.vtl.VtlBindings;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Class to manage the writing of Parquet output tables.
 */
@Slf4j
public class ParquetOutputFiles extends OutputFiles {

	public static final String PARQUET_EXTENSION = ".parquet";


    /**
	 * When an instance is created, the output folder is created.
	 *
	 * @param outDirectory Out directory defined in application properties.
	 * @param vtlBindings  Vtl bindings where datasets are stored.
	 * @param modes list of modes names
	 * @param database connection to duckDb database
	 * @param fileUtilsInterface file interface to use (file system or minio)
	 */

	public ParquetOutputFiles(Path outDirectory, VtlBindings vtlBindings, List<String> modes, Statement database,
							  FileUtilsInterface fileUtilsInterface, KraftwerkExecutionContext kraftwerkExecutionContext, EncryptionUtils encryptionUtils) {
		super(outDirectory, vtlBindings, modes, database, fileUtilsInterface, kraftwerkExecutionContext, encryptionUtils);
	}


	/**
	 * @author Adrien Marchal
	 * Method to write output tables from datasets that are in the bindings.
	 */
	@Override
	public void writeOutputTablesV2() throws KraftwerkException {
		for (String datasetName : getDatasetToCreate()) {
			try {
				Files.createDirectories(Path.of(System.getProperty("java.io.tmpdir")));
				Path tmpOutputFile = Files.createTempFile(Path.of(System.getProperty("java.io.tmpdir")),
						outputFileName(datasetName, kraftwerkExecutionContext), null);

				Files.deleteIfExists(tmpOutputFile);
				//Data export
				getDatabase().execute(String.format("COPY %s TO '%s' (FORMAT PARQUET)", datasetName, tmpOutputFile));


				String outputFile = getOutputFolder().resolve(outputFileName(datasetName, kraftwerkExecutionContext)).toString();

				//Encrypt file if requested
				if(kraftwerkExecutionContext.isWithEncryption()) {
					InputStream encryptedStream = encryptionUtils.encryptOutputFile(tmpOutputFile, kraftwerkExecutionContext);
					getFileUtilsInterface().writeFile(outputFile, encryptedStream, true);
					log.info("File: {} successfully written and encrypted", outputFile);
					continue; //Go to next dataset to write
				}

				//Move to output folder
				getFileUtilsInterface().moveFile(tmpOutputFile, outputFile);
				log.info("File: {} successfully written", outputFile);
			} catch (Exception e) {
				throw new KraftwerkException(500, e.toString());
			}
		}
	}


	@Override
	public void writeImportScripts(Map<String, MetadataModel> metadataModels, KraftwerkExecutionContext kraftwerkExecutionContext) {
		// Assemble required info to write scripts
		List<TableScriptInfo> tableScriptInfoList = new ArrayList<>();
		for (String datasetName : getDatasetToCreate()) {
			getAllOutputFileNames(datasetName).forEach(filename -> {
				TableScriptInfo tableScriptInfo = new TableScriptInfo(datasetName, filename,
						getVtlBindings().getDataset(datasetName).getDataStructure(), metadataModels);
				tableScriptInfoList.add(tableScriptInfo);
			});

		}
		// Write scripts
		TextFileWriter.writeFile(getOutputFolder().resolve("import_parquet.R"),
				new RImportScript(tableScriptInfoList).generateScript(), this.getFileUtilsInterface());
	}

	/**
	 * Return the name of the file to be written from the dataset name.
	 */
	@Override
	public String outputFileName(String datasetName, KraftwerkExecutionContext kraftwerkExecutionContext) {
		String path =  getOutputFolder().getParent().getFileName() + "_" + datasetName ;
		return kraftwerkExecutionContext.isWithEncryption() ?
			path + PARQUET_EXTENSION + encryptionUtils.getEncryptedFileExtension()
			: path + PARQUET_EXTENSION;
	}

	public List<String> getAllOutputFileNames(String datasetName) {
		List<String> filenames = new ArrayList<>();
		String path =  getOutputFolder().getParent().getFileName() + "_" + datasetName ;
		filenames.add(path); // 0
		return filenames;
	}

	public Map<String, Long> countExistingFilesByDataset(Path dir) throws KraftwerkException {
		try (Stream<Path> stream = Files.walk(dir)) {
			return stream
					.filter(Files::isRegularFile)
					.map(Path::getFileName)
					.map(Path::toString)
					.filter(name -> name.contains(PARQUET_EXTENSION))
					.filter(name -> getDatasetToCreate().stream().anyMatch(name::contains) )
					.map(name -> getDatasetToCreate().stream().filter(name::contains).findFirst().orElse(""))
					.collect(Collectors.groupingBy(name -> name, Collectors.counting()));
		} catch (IOException e) {
			throw new KraftwerkException(500,"Cannot read outputfolder" + e.getMessage());
		}

	}

}
