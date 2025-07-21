package fr.insee.kraftwerk.core.sequence;

import fr.insee.bpm.metadata.model.MetadataModel;
import fr.insee.kraftwerk.core.encryption.ApplicationContextProvider;
import fr.insee.kraftwerk.core.encryption.EncryptionUtilsStub;
import fr.insee.kraftwerk.core.exceptions.KraftwerkException;
import fr.insee.kraftwerk.core.inputs.ModeInputs;
import fr.insee.kraftwerk.core.outputs.OutputFiles;
import fr.insee.kraftwerk.core.outputs.OutputFilesFactory;
import fr.insee.kraftwerk.core.utils.KraftwerkExecutionContext;
import fr.insee.kraftwerk.core.utils.files.FileUtilsInterface;
import fr.insee.kraftwerk.core.vtl.VtlBindings;
import org.springframework.context.ApplicationContext;

import java.nio.file.Path;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
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
	public void writeOutputFilesV2(Path inDirectory,
								   String outDirectorySuffix,
								   VtlBindings vtlBindings,
								   ModeInputs modeInputs,
								   KraftwerkExecutionContext kraftwerkExecutionContext,
								   Statement database,
								   FileUtilsInterface fileUtilsInterface) throws KraftwerkException {
		Path outDirectory = FileUtilsInterface.transformToOut(inDirectory, LocalDateTime.now(), outDirectorySuffix);
		Map<String, ModeInputs> modeInputsMap = new HashMap<>();
		modeInputsMap.put(modeInputs.getDataMode(), modeInputs);

		writeCsvFilesV2(outDirectory, vtlBindings, modeInputsMap, new HashMap<>(), kraftwerkExecutionContext, database,
				fileUtilsInterface);
		writeParquetFilesV2(outDirectory, vtlBindings, modeInputsMap, new HashMap<>(), kraftwerkExecutionContext, database, fileUtilsInterface);
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
		OutputFiles csvOutputFiles = outputFilesFactory.createCsv(outDirectory, vtlBindings,
				new ArrayList<>(modeInputsMap.keySet()), database, fileUtilsInterface, kraftwerkExecutionContext);
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
		OutputFiles parquetOutputFiles = outputFilesFactory.createParquet(outDirectory, vtlBindings,
				new ArrayList<>(modeInputsMap.keySet()), database, fileUtilsInterface, kraftwerkExecutionContext);
		parquetOutputFiles.writeOutputTablesV2();
		parquetOutputFiles.writeImportScripts(metadataModels, kraftwerkExecutionContext);
	}

}
