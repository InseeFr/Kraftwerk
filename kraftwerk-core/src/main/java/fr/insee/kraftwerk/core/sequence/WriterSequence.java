package fr.insee.kraftwerk.core.sequence;

import fr.insee.bpm.metadata.model.MetadataModel;
import fr.insee.kraftwerk.core.encryption.ApplicationContextProvider;
import fr.insee.kraftwerk.core.encryption.EncryptionUtilsStub;
import fr.insee.kraftwerk.core.exceptions.KraftwerkException;
import fr.insee.kraftwerk.core.inputs.ModeInputs;
import fr.insee.kraftwerk.core.outputs.OutputFiles;
import fr.insee.kraftwerk.core.outputs.OutputFilesFactory;
import fr.insee.kraftwerk.core.outputs.csv.CsvOutputFiles;
import fr.insee.kraftwerk.core.utils.KraftwerkExecutionContext;
import fr.insee.kraftwerk.core.utils.files.FileUtilsInterface;
import fr.insee.kraftwerk.core.vtl.VtlBindings;
import org.springframework.context.ApplicationContext;

import java.nio.file.Path;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Map;

public class WriterSequence {

	private final OutputFilesFactory outputFilesFactory;

	public WriterSequence() {
		ApplicationContext context = ApplicationContextProvider.getApplicationContext();
		if (context == null){
			outputFilesFactory = new OutputFilesFactory(new EncryptionUtilsStub());
		}else {
			outputFilesFactory = context.getBean(OutputFilesFactory.class);
		}
	}

	public void writeOutputFiles(Path inDirectory,
								 VtlBindings vtlBindings,
								 Map<String, ModeInputs> modeInputsMap,
								 Map<String, MetadataModel> metadataModels,
								 KraftwerkExecutionContext kraftwerkExecutionContext,
								 Statement database,
								 FileUtilsInterface fileUtilsInterface) throws KraftwerkException {
		Path outDirectory = FileUtilsInterface.transformToOut(inDirectory,kraftwerkExecutionContext.getExecutionDateTime());

		writeCsvFiles(inDirectory, vtlBindings, modeInputsMap, metadataModels, kraftwerkExecutionContext, database, fileUtilsInterface);
		writeParquetFiles(outDirectory, vtlBindings, modeInputsMap, metadataModels, kraftwerkExecutionContext, database, fileUtilsInterface);
	}

	private void writeCsvFiles(Path inDirectory,
								 VtlBindings vtlBindings,
								 Map<String, ModeInputs> modeInputsMap,
								 Map<String, MetadataModel> metadataModels,
								 KraftwerkExecutionContext kraftwerkExecutionContext,
								 Statement database,
								 FileUtilsInterface fileUtilsInterface) throws KraftwerkException {
		//Write CSV
		Path outDirectory = FileUtilsInterface.transformToOut(inDirectory,kraftwerkExecutionContext.getExecutionDateTime());
		/* Step 5.1 : write csv output tables */
		OutputFiles csvOutputFiles = outputFilesFactory.createCsv(outDirectory, vtlBindings,
				new ArrayList<>(modeInputsMap.keySet()), database, fileUtilsInterface, kraftwerkExecutionContext);
		csvOutputFiles.writeOutputTables();
		/* Step 5.2 : write scripts to import csv tables in several languages */
		csvOutputFiles.writeImportScripts(metadataModels, kraftwerkExecutionContext);
	}

	public void writeCsvFiles(Path inDirectory,
							  String outDirectorySuffix,
							  VtlBindings vtlBindings,
							  KraftwerkExecutionContext kraftwerkExecutionContext,
							  Statement database,
							  FileUtilsInterface fileUtilsInterface) throws KraftwerkException {
		//Write CSV
		Path outDirectory = FileUtilsInterface.transformToOut(inDirectory, LocalDateTime.now(), outDirectorySuffix);
		/* Step 5.1 : write csv output tables */
		OutputFiles csvOutputFiles = outputFilesFactory.createCsv(outDirectory, vtlBindings, new ArrayList<>(), database, fileUtilsInterface,kraftwerkExecutionContext);
		csvOutputFiles.writeOutputTables();
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
		OutputFiles parquetOutputFiles = outputFilesFactory.createParquet(outDirectory, vtlBindings,
				new ArrayList<>(modeInputsMap.keySet()), database, fileUtilsInterface, kraftwerkExecutionContext);
		parquetOutputFiles.writeOutputTables();
		parquetOutputFiles.writeImportScripts(metadataModels, kraftwerkExecutionContext);
	}

	public void writeParquetFiles(Path inDirectory,
								  String outDirectorySuffix,
								  VtlBindings vtlBindings,
								  Statement database,
								  FileUtilsInterface fileUtilsInterface) throws KraftwerkException {
		/* Step 5.3 : write parquet output tables */
		Path outDirectory = FileUtilsInterface.transformToOut(inDirectory, LocalDateTime.now(), outDirectorySuffix);
		OutputFiles parquetOutputFiles = new ParquetOutputFiles(outDirectory, vtlBindings,  new ArrayList<>(), database, fileUtilsInterface);
		parquetOutputFiles.writeOutputTables();
	}
}
