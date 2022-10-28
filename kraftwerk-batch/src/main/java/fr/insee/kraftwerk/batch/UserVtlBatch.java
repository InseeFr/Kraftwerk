package fr.insee.kraftwerk.batch;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;

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
import fr.insee.kraftwerk.core.parsers.DataParser;
import fr.insee.kraftwerk.core.parsers.DataParserManager;
import fr.insee.kraftwerk.core.rawdata.SurveyRawData;
import fr.insee.kraftwerk.core.vtl.VtlBindings;
import fr.insee.kraftwerk.core.vtl.VtlExecute;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class UserVtlBatch implements Tasklet {

    private static final String JSON = ".json";
	@Setter
    Path campaignDirectory;
	
	
	VtlExecute vtlExecute = new VtlExecute();

    @Override
    public RepeatStatus execute(StepContribution stepContribution, ChunkContext chunkContext) throws IOException, NullException  {

        String campaignName = campaignDirectory.getFileName().toString();

        Path userInputFile = campaignDirectory.resolve(Constants.USER_VTL_INPUT_FILE);

        Path vtlOutputDir = campaignDirectory.getParent().getParent().resolve("out").resolve(campaignName);
        Files.createDirectories(vtlOutputDir);

        if (Files.exists(userInputFile)) {

            log.info(String.format("Found '%s' input file at location %s",
                    Constants.USER_VTL_INPUT_FILE, campaignDirectory));
            log.info("Vtl datasets job started.");

            UserInputs userInputs = new UserInputs(userInputFile, campaignDirectory);

            VtlBindings vtlBindings = new VtlBindings();

            Map<String, VariablesMap> metadataVariables = new LinkedHashMap<>();

            for (String dataMode : userInputs.getModes()) {

                ModeInputs modeInputs = userInputs.getModeInputs(dataMode);

                SurveyRawData data = new SurveyRawData();

                data.setVariablesMap(DDIReader.getVariablesFromDDI(modeInputs.getDdiUrl()));
                metadataVariables.put(dataMode, data.getVariablesMap());

                DataParser parser = DataParserManager.getParser(modeInputs.getDataFormat(), data);
                parser.parseSurveyData(modeInputs.getDataFile());

                Path paraDataFolder = modeInputs.getParadataFolder();
                if (paraDataFolder != null) {
                    ParadataParser paraDataParser = new ParadataParser();
                    Paradata paraData = new Paradata(paraDataFolder);
                    paraDataParser.parseParadata(paraData, data);
                }

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

                vtlExecute.convertToVtlDataset(data, dataMode, vtlBindings);

                if (modeInputs.getLunaticFile() != null) {
                    CalculatedVariables calculatedVariables = LunaticReader.getCalculatedFromLunatic(
                            modeInputs.getLunaticFile());
                    DataProcessing calculatedProcessing = new CalculatedProcessing(vtlBindings, calculatedVariables, data.getVariablesMap());
                    calculatedProcessing.applyVtlTransformations(dataMode, null);
                }

                new GroupProcessing(vtlBindings, data.getVariablesMap()).applyVtlTransformations(dataMode, null);

                UnimodalDataProcessing dataProcessing = DataProcessingManager
                        .getProcessingClass(modeInputs.getDataFormat(), vtlBindings, data.getVariablesMap());
                dataProcessing.applyVtlTransformations(dataMode, modeInputs.getModeVtlFile());

                vtlExecute.writeJsonDataset(dataMode, vtlOutputDir.resolve("1_" + dataMode + JSON), vtlBindings);

            }

            String multimodeDatasetName = userInputs.getMultimodeDatasetName();

            DataProcessing reconciliationProcessing = new ReconciliationProcessing(vtlBindings);
            reconciliationProcessing.applyVtlTransformations(multimodeDatasetName,
                    userInputs.getVtlReconciliationFile());

            vtlExecute.writeJsonDataset(multimodeDatasetName,
                    vtlOutputDir.resolve("2_" + multimodeDatasetName + "_reconciliation.json"), vtlBindings);

            CleanUpProcessing cleanUpProcessing = new CleanUpProcessing(vtlBindings, metadataVariables);
            cleanUpProcessing.applyVtlTransformations(multimodeDatasetName, null);

            DataProcessing multimodeTransformations = new MultimodeTransformations(vtlBindings);
            multimodeTransformations.applyVtlTransformations(multimodeDatasetName,
                    userInputs.getVtlTransformationsFile());

            vtlExecute.writeJsonDataset(multimodeDatasetName,
                    vtlOutputDir.resolve("3_" + multimodeDatasetName + JSON), vtlBindings);

            DataProcessing informationLevelsProcessing = new InformationLevelsProcessing(vtlBindings);
            informationLevelsProcessing.applyVtlTransformations(multimodeDatasetName,
                    userInputs.getVtlInformationLevelsFile());

            Set<String> finalNames = vtlBindings.keySet().stream()
                    .filter(bindingName ->
                            !(metadataVariables.containsKey(bindingName) || bindingName.equals(multimodeDatasetName)))
                    .collect(Collectors.toSet());
            for(String datasetName : finalNames) {
            	vtlExecute.writeJsonDataset(datasetName,
                        vtlOutputDir.resolve("4_" + datasetName + JSON), vtlBindings);
            }

            log.info("Vtl datasets job terminated.");

        }

        else {
            log.error(String.format("No '%s' configuration file found at location: %s",
                    Constants.USER_VTL_INPUT_FILE, campaignDirectory));
        }

        return RepeatStatus.FINISHED;
    }
}
