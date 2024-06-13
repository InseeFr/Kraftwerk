package fr.insee.kraftwerk.core.sequence;

import fr.insee.kraftwerk.core.KraftwerkError;
import fr.insee.kraftwerk.core.exceptions.KraftwerkException;
import fr.insee.kraftwerk.core.inputs.ModeInputs;
import fr.insee.kraftwerk.core.metadata.MetadataModel;
import fr.insee.kraftwerk.core.outputs.OutputFiles;
import fr.insee.kraftwerk.core.outputs.csv.CsvOutputFiles;
import fr.insee.kraftwerk.core.outputs.parquet.ParquetOutputFiles;
import fr.insee.kraftwerk.core.utils.FileUtils;
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

	public void writeOutputFiles(Path inDirectory,LocalDateTime executionDateTime, VtlBindings vtlBindings, Map<String, ModeInputs> modeInputsMap, Map<String, MetadataModel> metadataModels, List<KraftwerkError> errors, Statement database) throws KraftwerkException {
		Path outDirectory = FileUtils.transformToOut(inDirectory,executionDateTime);

		/* Step 4.1: Convert VTL datasets into SQL tables for export */
		SqlUtils.convertVtlBindingsIntoSqlDatabase(vtlBindings, database);
		writeCsvFiles(outDirectory, vtlBindings, modeInputsMap, metadataModels, errors, null, database);
		writeParquetFiles(outDirectory, vtlBindings, modeInputsMap, metadataModels, errors, database);
	}

	//Write CSV
	private void writeCsvFiles(Path outDirectory, VtlBindings vtlBindings, Map<String, ModeInputs> modeInputsMap, Map<String, MetadataModel> metadataModels, List<KraftwerkError> errors, KraftwerkExecutionLog kraftwerkExecutionLog, Statement databaseConnection) throws KraftwerkException {
		/* Step 4.2 : write csv output tables */
		OutputFiles csvOutputFiles = new CsvOutputFiles(outDirectory, vtlBindings, kraftwerkExecutionLog, new ArrayList<>(modeInputsMap.keySet()), databaseConnection);
		csvOutputFiles.writeOutputTables(metadataModels);

		/* Step 4.3 : write scripts to import csv tables in several languages */
		csvOutputFiles.writeImportScripts(metadataModels, errors);
	}

	//Write Parquet
	private void writeParquetFiles(Path outDirectory, VtlBindings vtlBindings, Map<String, ModeInputs> modeInputsMap, Map<String, MetadataModel> metadataModels, List<KraftwerkError> errors, Statement databaseConnection) throws KraftwerkException {
		/* Step 4.4 : write parquet output tables */
		OutputFiles parquetOutputFiles = new ParquetOutputFiles(outDirectory, vtlBindings,  new ArrayList<>(modeInputsMap.keySet()), databaseConnection);
		parquetOutputFiles.writeOutputTables(metadataModels);
		parquetOutputFiles.writeImportScripts(metadataModels, errors);
	}
}
