package fr.insee.kraftwerk.batch;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

import fr.insee.kraftwerk.core.Constants;
import fr.insee.kraftwerk.core.dataprocessing.CalculatedProcessing;
import fr.insee.kraftwerk.core.dataprocessing.CleanUpProcessing;
import fr.insee.kraftwerk.core.dataprocessing.DataProcessing;
import fr.insee.kraftwerk.core.dataprocessing.DataProcessingManager;
import fr.insee.kraftwerk.core.dataprocessing.GroupProcessing;
import fr.insee.kraftwerk.core.dataprocessing.InformationLevelsProcessing;
import fr.insee.kraftwerk.core.dataprocessing.MultimodeTransformations;
import fr.insee.kraftwerk.core.dataprocessing.ReconciliationProcessing;
import fr.insee.kraftwerk.core.dataprocessing.UnimodalDataProcessing;
import fr.insee.kraftwerk.core.exceptions.KraftwerkException;
import fr.insee.kraftwerk.core.exceptions.NullException;
import fr.insee.kraftwerk.core.extradata.paradata.Paradata;
import fr.insee.kraftwerk.core.extradata.paradata.ParadataParser;
import fr.insee.kraftwerk.core.extradata.reportingdata.CSVReportingDataParser;
import fr.insee.kraftwerk.core.extradata.reportingdata.ReportingData;
import fr.insee.kraftwerk.core.extradata.reportingdata.XMLReportingDataParser;
import fr.insee.kraftwerk.core.inputs.ModeInputs;
import fr.insee.kraftwerk.core.inputs.UserInputs;
import fr.insee.kraftwerk.core.metadata.CalculatedVariables;
import fr.insee.kraftwerk.core.metadata.DDIReader;
import fr.insee.kraftwerk.core.metadata.LunaticReader;
import fr.insee.kraftwerk.core.metadata.VariablesMap;
import fr.insee.kraftwerk.core.outputs.OutputFiles;
import fr.insee.kraftwerk.core.parsers.DataFormat;
import fr.insee.kraftwerk.core.parsers.DataParser;
import fr.insee.kraftwerk.core.parsers.DataParserManager;
import fr.insee.kraftwerk.core.rawdata.SurveyRawData;
import fr.insee.kraftwerk.core.vtl.VtlBindings;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

/**
 * App main class.
 *
 */
@Slf4j
public class Launcher {

	private final Map<String, VariablesMap> metadataVariables = new LinkedHashMap<>();

	public Boolean main(@NonNull Path inDirectory){

		try {
			if (verifyInDirectory(inDirectory)) {

				String campaignName = inDirectory.getFileName().toString();

				log.info(
						"===============================================================================================");
				log.info("Kraftwerk batch started for campaign: " + campaignName);
				log.info(
						"===============================================================================================");

				Path outDirectory = transformToOut(inDirectory);

				/* Step 1 : Init */

				UserInputs userInputs = new UserInputs(inDirectory.resolve(Constants.USER_INPUT_FILE), inDirectory);

				VtlBindings vtlBindings = new VtlBindings();

				/* Step 2 : unimodal data */
				for (String dataMode : userInputs.getModeInputsMap().keySet()) {
					ModeInputs modeInputs = userInputs.getModeInputs(dataMode);

					SurveyRawData data = new SurveyRawData();

					/* Step 2.0 : Read the DDI file to get survey variables */
					data.setVariablesMap(DDIReader.getVariablesFromDDI(modeInputs.getDdiUrl()));
					metadataVariables.put(dataMode, data.getVariablesMap());

					/* Step 2.1 : Fill the data object with the survey answers file */
					data.setDataFilePath(modeInputs.getDataFile());
					DataParser parser = DataParserManager.getParser(modeInputs.getDataFormat(), data);
					parser.parseSurveyData(modeInputs.getDataFile());

					/* Step 2.2 : Get paradata for the survey */
					Path paraDataFolder = modeInputs.getParadataFolder();
					if (paraDataFolder != null) {
						ParadataParser paraDataParser = new ParadataParser();
						Paradata paraData = new Paradata(paraDataFolder);
						paraDataParser.parseParadata(paraData, data);
					}

					/* Step 2.3 : Get reportingData for the survey */
					Path reportingDataFile = modeInputs.getReportingDataFile();
					if (reportingDataFile != null) {
						ReportingData reportingData = new ReportingData(reportingDataFile);
						if (reportingDataFile.toString().contains(".xml")) {
							XMLReportingDataParser xMLReportingDataParser = new XMLReportingDataParser();

							xMLReportingDataParser.parseReportingData(reportingData, data);

						} else {
							if (reportingDataFile.toString().contains(".csv")) {
								CSVReportingDataParser cSVReportingDataParser = new CSVReportingDataParser();
								cSVReportingDataParser.parseReportingData(reportingData, data);
							}
						}
					}

					/* Step 2.4a : Convert data object to a VTL Dataset */
					data.setDataMode(dataMode); // TODO: remove useless dataMode attribute from data object
					vtlBindings.convertToVtlDataset(data, dataMode);

					/* Step 2.4b : Apply VTL expression for calculated variables (if any) */
					if (modeInputs.getLunaticFile() != null) {
						CalculatedVariables calculatedVariables = LunaticReader
								.getCalculatedFromLunatic(modeInputs.getLunaticFile());
						DataProcessing calculatedProcessing = new CalculatedProcessing(vtlBindings);
						calculatedProcessing.applyVtlTransformations(dataMode, null, calculatedVariables,
								data.getVariablesMap());
					} else {
						log.info(String.format("No Lunatic questionnaire file for mode \"%s\"", dataMode));
						if (modeInputs.getDataFormat() == DataFormat.LUNATIC_XML
								|| modeInputs.getDataFormat() == DataFormat.LUNATIC_JSON) {
							log.warn(String.format(
									"Calculated variables for lunatic data of mode \"%s\" will not be evaluated.",
									dataMode));
						}
					}

					/* Step 2.4c : Prefix variable names with their belonging group names */
					new GroupProcessing(vtlBindings).applyVtlTransformations(dataMode, null, data.getVariablesMap());

					/* Step 2.5 : Apply mode-specific VTL transformations */
					UnimodalDataProcessing dataProcessing = DataProcessingManager
							.getProcessingClass(modeInputs.getDataFormat(), vtlBindings);
					dataProcessing.applyVtlTransformations(dataMode, modeInputs.getModeVtlFile(),
							data.getVariablesMap());

				}

				/* Step 3 : multimodal VTL data processing */

				String multimodeDatasetName = userInputs.getMultimodeDatasetName();

				/* Step 3.1 : aggregate unimodal datasets into a multimodal unique dataset */
				DataProcessing reconciliationProcessing = new ReconciliationProcessing(vtlBindings);
				reconciliationProcessing.applyVtlTransformations(multimodeDatasetName,
						userInputs.getVtlReconciliationFile());

				/* Step 3.1.b : clean up processing */
				CleanUpProcessing cleanUpProcessing = new CleanUpProcessing(vtlBindings);
				cleanUpProcessing.applyVtlTransformations(multimodeDatasetName, null, metadataVariables);

				/* Step 3.2 : treatments on the multimodal dataset */
				DataProcessing multimodeTransformations = new MultimodeTransformations(vtlBindings);
				multimodeTransformations.applyVtlTransformations(multimodeDatasetName,
						userInputs.getVtlTransformationsFile());

				/* Step 3.3 : Create datasets on each information level (i.e. each group) */
				DataProcessing informationLevelsProcessing = new InformationLevelsProcessing(vtlBindings);
				informationLevelsProcessing.applyVtlTransformations(multimodeDatasetName,
						userInputs.getVtlInformationLevelsFile());

				/* Step 4 : Write output files */

				/* Step 4.1 : write csv output tables */
				OutputFiles outputFiles = new OutputFiles(outDirectory, vtlBindings, userInputs);
				outputFiles.writeOutputCsvTables();

				/* Step 4.2 : write scripts to import csv tables in several languages */
				outputFiles.writeImportScripts(metadataVariables);

				/* Step 4.3 : move kraftwerk.json to a secondary folder */
				outputFiles.renameInputFile(inDirectory);

				/* Step 4.4 : move differential data to a secondary folder */
				try {
					outputFiles.moveInputFiles(userInputs);
				} catch (KraftwerkException e) {
					log.error(e.getMessage());
				}

				log.info(
						"===============================================================================================");
				log.info("Kraftwerk batch terminated for campaign: " + campaignName);
				log.info(
						"===============================================================================================\n");

			}

		} catch (NullException e) {
			// TODO Auto-generated catch block
			NullException nullException = new NullException();
			nullException.renameInputFile(inDirectory);
		}

		return true;
	}

	private boolean verifyInDirectory(Path inDirectory) {

		Path userInputFile = inDirectory.resolve(Constants.USER_INPUT_FILE);

		if (Files.exists(userInputFile)) {
			log.info(String.format("Found configuration file in campaign folder: %s", userInputFile));
		} else {
			log.info("No batch configuration file found in campaign folder: " + inDirectory);
			return false;
		}
		return true;
	}

	/**
	 * Change /some/path/in/campaign-name to /some/path/out/campaign-name
	 */
	public Path transformToOut(Path inDirectory) {
		return "in".equals(inDirectory.getFileName().toString()) ? inDirectory.getParent().resolve("out")
				: transformToOut(inDirectory.getParent()).resolve(inDirectory.getFileName());
	}

}