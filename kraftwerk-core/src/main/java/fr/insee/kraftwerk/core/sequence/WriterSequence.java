package fr.insee.kraftwerk.core.sequence;

import fr.insee.kraftwerk.core.KraftwerkError;
import fr.insee.kraftwerk.core.exceptions.KraftwerkException;
import fr.insee.kraftwerk.core.inputs.ModeInputs;
import fr.insee.kraftwerk.core.metadata.MetadataModel;
import fr.insee.kraftwerk.core.outputs.OutputFiles;
import fr.insee.kraftwerk.core.outputs.csv.CsvOutputFiles;
import fr.insee.kraftwerk.core.outputs.parquet.ParquetOutputFiles;
import fr.insee.kraftwerk.core.utils.files.FileUtilsInterface;
import fr.insee.kraftwerk.core.utils.SqlUtils;
import fr.insee.kraftwerk.core.utils.log.KraftwerkExecutionLog;
import fr.insee.kraftwerk.core.vtl.VtlBindings;
import lombok.NoArgsConstructor;

import java.nio.file.Path;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@NoArgsConstructor
public class WriterSequence {

	public void writeOutputFiles(Path inDirectory,
								 LocalDateTime executionDateTime,
								 VtlBindings vtlBindings,
								 Map<String, ModeInputs> modeInputsMap,
								 Map<String, MetadataModel> metadataModels,
								 List<KraftwerkError> errors, 
								 Statement database,
								 FileUtilsInterface fileUtilsInterface) throws KraftwerkException {
		Path outDirectory = FileUtilsInterface.transformToOut(inDirectory,executionDateTime);

		writeCsvFiles(inDirectory, executionDateTime,vtlBindings, modeInputsMap, metadataModels, errors, null, database, fileUtilsInterface);
		writeParquetFiles(outDirectory, vtlBindings, modeInputsMap, metadataModels, errors, database, fileUtilsInterface);
	}

	public void writeCsvFiles(Path inDirectory,
								 LocalDateTime executionDateTime,
								 VtlBindings vtlBindings,
								 Map<String, ModeInputs> modeInputsMap,
								 Map<String, MetadataModel> metadataModels,
								 List<KraftwerkError> errors,
								 KraftwerkExecutionLog kraftwerkExecutionLog,
								 Statement database,
								 FileUtilsInterface fileUtilsInterface) throws KraftwerkException {
		//Write CSV
		Path outDirectory = FileUtilsInterface.transformToOut(inDirectory,executionDateTime);
		/* Step 5.1 : write csv output tables */
		OutputFiles csvOutputFiles = new CsvOutputFiles(outDirectory, vtlBindings, kraftwerkExecutionLog, new ArrayList<>(modeInputsMap.keySet()),
				database, fileUtilsInterface);
		csvOutputFiles.writeOutputTables(metadataModels);
		/* Step 5.2 : write scripts to import csv tables in several languages */
		csvOutputFiles.writeImportScripts(metadataModels, errors);
	}

	//Write Parquet
	private void writeParquetFiles(Path outDirectory,
									 VtlBindings vtlBindings, 
									 Map<String, ModeInputs> modeInputsMap,
									 Map<String, MetadataModel> metadataModels,
									 List<KraftwerkError> errors,
									 Statement database,
									 FileUtilsInterface fileUtilsInterface) throws KraftwerkException {
		/* Step 5.3 : write parquet output tables */
		OutputFiles parquetOutputFiles = new ParquetOutputFiles(outDirectory, vtlBindings,  new ArrayList<>(modeInputsMap.keySet()), database, fileUtilsInterface);
		parquetOutputFiles.writeOutputTables(metadataModels);
		parquetOutputFiles.writeImportScripts(metadataModels, errors);
	}
}
