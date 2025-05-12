package fr.insee.kraftwerk.core.sequence;

import fr.insee.bpm.metadata.model.MetadataModel;
import fr.insee.kraftwerk.core.exceptions.KraftwerkException;
import fr.insee.kraftwerk.core.inputs.ModeInputs;
import fr.insee.kraftwerk.core.outputs.OutputFiles;
import fr.insee.kraftwerk.core.outputs.csv.CsvOutputFiles;
import fr.insee.kraftwerk.core.outputs.parquet.ParquetOutputFiles;
import fr.insee.kraftwerk.core.utils.files.FileUtilsInterface;
import fr.insee.kraftwerk.core.utils.log.KraftwerkExecutionContext;
import fr.insee.kraftwerk.core.vtl.VtlBindings;
import lombok.NoArgsConstructor;

import java.nio.file.Path;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Map;

@NoArgsConstructor
public class WriterSequence {

	public void writeOutputFiles(Path inDirectory,
								 VtlBindings vtlBindings,
								 Map<String, ModeInputs> modeInputsMap,
								 Map<String, MetadataModel> metadataModels,
								 KraftwerkExecutionContext kraftwerkExecutionContext,
								 Statement database,
								 FileUtilsInterface fileUtilsInterface) throws KraftwerkException {
		Path outDirectory = FileUtilsInterface.transformToOut(inDirectory,kraftwerkExecutionContext.getExecutionDateTime());

		writeCsvFiles(outDirectory, vtlBindings, modeInputsMap, metadataModels, kraftwerkExecutionContext, database, fileUtilsInterface);
		writeParquetFiles(outDirectory, vtlBindings, modeInputsMap, metadataModels, kraftwerkExecutionContext, database, fileUtilsInterface);
	}

	public void writeCsvFiles(Path outDirectory,
								 VtlBindings vtlBindings,
								 Map<String, ModeInputs> modeInputsMap,
								 Map<String, MetadataModel> metadataModels,
								 KraftwerkExecutionContext kraftwerkExecutionContext,
								 Statement database,
								 FileUtilsInterface fileUtilsInterface) throws KraftwerkException {
		/* Step 5.1 : write csv output tables */
		OutputFiles csvOutputFiles = new CsvOutputFiles(outDirectory, vtlBindings, kraftwerkExecutionContext, new ArrayList<>(modeInputsMap.keySet()),
				database, fileUtilsInterface);
		csvOutputFiles.writeOutputTables();
		/* Step 5.2 : write scripts to import csv tables in several languages */
		csvOutputFiles.writeImportScripts(metadataModels, kraftwerkExecutionContext);
	}


	//Write Parquet
	private void writeParquetFiles(Path outDirectory,
									 VtlBindings vtlBindings, 
									 Map<String, ModeInputs> modeInputsMap,
									 Map<String, MetadataModel> metadataModels,
									 KraftwerkExecutionContext kraftwerkExecutionContext,
									 Statement database,
									 FileUtilsInterface fileUtilsInterface) throws KraftwerkException {
		/* Step 5.3 : write parquet output tables */
		OutputFiles parquetOutputFiles = new ParquetOutputFiles(outDirectory, vtlBindings,  new ArrayList<>(modeInputsMap.keySet()), database, fileUtilsInterface);
		parquetOutputFiles.writeOutputTablesV2();
		parquetOutputFiles.writeImportScripts(metadataModels, kraftwerkExecutionContext);
	}


	//========= OPTIMISATIONS PERFS (START) ==========
	/**
	 * @author Adrien Marchal
	 */
	public void writeOutputFilesV2(Path inDirectory,
								 VtlBindings vtlBindings,
								 Map<String, ModeInputs> modeInputsMap,
								 Map<String, MetadataModel> metadataModels,
								 KraftwerkExecutionContext kraftwerkExecutionContext,
								 Statement database,
								 FileUtilsInterface fileUtilsInterface) throws KraftwerkException {
		Path outDirectory = FileUtilsInterface.transformToOut(inDirectory,kraftwerkExecutionContext.getExecutionDateTime());

		writeCsvFilesV2(outDirectory, vtlBindings, modeInputsMap, metadataModels, kraftwerkExecutionContext, database, fileUtilsInterface);
		writeParquetFilesV2(outDirectory, vtlBindings, modeInputsMap, metadataModels, kraftwerkExecutionContext, database, fileUtilsInterface);
	}

	/**
	 * @author Adrien Marchal
	 */
	public void writeCsvFilesV2(Path outDirectory,
							  VtlBindings vtlBindings,
							  Map<String, ModeInputs> modeInputsMap,
							  Map<String, MetadataModel> metadataModels,
							  KraftwerkExecutionContext kraftwerkExecutionContext,
							  Statement database,
							  FileUtilsInterface fileUtilsInterface) throws KraftwerkException {
		/* Step 5.1 : write csv output tables */
		OutputFiles csvOutputFiles = new CsvOutputFiles(outDirectory, vtlBindings, kraftwerkExecutionContext, new ArrayList<>(modeInputsMap.keySet()),
				database, fileUtilsInterface);
		csvOutputFiles.writeOutputTablesV2();
		/* Step 5.2 : write scripts to import csv tables in several languages */
		csvOutputFiles.writeImportScripts(metadataModels, kraftwerkExecutionContext);
	}


	/**
	 * @author Adrien Marchal
	 * Write Parquet
	 */
	private void writeParquetFilesV2(Path outDirectory,
								   VtlBindings vtlBindings,
								   Map<String, ModeInputs> modeInputsMap,
								   Map<String, MetadataModel> metadataModels,
								   KraftwerkExecutionContext kraftwerkExecutionContext,
								   Statement database,
								   FileUtilsInterface fileUtilsInterface) throws KraftwerkException {
		/* Step 5.3 : write parquet output tables */
		OutputFiles parquetOutputFiles = new ParquetOutputFiles(outDirectory, vtlBindings,  new ArrayList<>(modeInputsMap.keySet()), database, fileUtilsInterface);
		parquetOutputFiles.writeOutputTablesV2();
		parquetOutputFiles.writeImportScripts(metadataModels, kraftwerkExecutionContext);
	}
	//========= OPTIMISATIONS PERFS (END) ==========


}
