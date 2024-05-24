package fr.insee.kraftwerk.core.sequence;

import fr.insee.kraftwerk.core.KraftwerkError;
import fr.insee.kraftwerk.core.exceptions.KraftwerkException;
import fr.insee.kraftwerk.core.inputs.ModeInputs;
import fr.insee.kraftwerk.core.metadata.MetadataModel;
import fr.insee.kraftwerk.core.outputs.OutputFiles;
import fr.insee.kraftwerk.core.outputs.csv.CsvOutputFiles;
import fr.insee.kraftwerk.core.outputs.parquet.ParquetOutputFiles;
import fr.insee.kraftwerk.core.utils.FileUtils;
import fr.insee.kraftwerk.core.utils.log.KraftwerkExecutionLog;
import fr.insee.kraftwerk.core.vtl.VtlBindings;
import lombok.NoArgsConstructor;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@NoArgsConstructor
public class WriterSequence {

	public void writeOutputFiles(Path inDirectory,LocalDateTime executionDateTime, VtlBindings vtlBindings, Map<String, ModeInputs> modeInputsMap, Map<String, MetadataModel> metadataModels, List<KraftwerkError> errors) throws KraftwerkException {
		writeOutputFiles(inDirectory,executionDateTime,vtlBindings,modeInputsMap,metadataModels,errors,null);
	}

	public void writeOutputFiles(Path inDirectory, LocalDateTime localDateTime, VtlBindings vtlBindings, Map<String, ModeInputs> modeInputsMap, Map<String, MetadataModel> metadataModels, List<KraftwerkError> errors, KraftwerkExecutionLog kraftwerkExecutionLog) throws KraftwerkException {
		Path outDirectory = FileUtils.transformToOut(inDirectory,localDateTime);
		/* Step 4.1 : write csv output tables */
		OutputFiles csvOutputFiles = new CsvOutputFiles(outDirectory, vtlBindings,  new ArrayList<>(modeInputsMap.keySet()),kraftwerkExecutionLog);
		csvOutputFiles.writeOutputTables(metadataModels);

		/* Step 4.2 : write scripts to import csv tables in several languages */
		csvOutputFiles.writeImportScripts(metadataModels, errors);
		
		OutputFiles parquetOutputFiles = new ParquetOutputFiles(outDirectory, vtlBindings,  new ArrayList<>(modeInputsMap.keySet()));
		parquetOutputFiles.writeOutputTables(metadataModels);
		parquetOutputFiles.writeImportScripts(metadataModels, errors);

	}
}
