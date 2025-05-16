package fr.insee.kraftwerk.api.process;

import fr.insee.bpm.metadata.model.MetadataModel;
import fr.insee.kraftwerk.core.Constants;
import fr.insee.kraftwerk.core.data.model.Mode;
import fr.insee.kraftwerk.core.dataprocessing.DataProcessing;
import fr.insee.kraftwerk.core.dataprocessing.DataProcessingManager;
import fr.insee.kraftwerk.core.exceptions.KraftwerkException;
import fr.insee.kraftwerk.core.extradata.reportingdata.CSVReportingDataParser;
import fr.insee.kraftwerk.core.extradata.reportingdata.ReportingData;
import fr.insee.kraftwerk.core.extradata.reportingdata.XMLReportingDataParser;
import fr.insee.kraftwerk.core.inputs.ModeInputs;
import fr.insee.kraftwerk.core.parsers.DataFormat;
import fr.insee.kraftwerk.core.rawdata.SurveyRawData;
import fr.insee.kraftwerk.core.sequence.InsertDatabaseSequence;
import fr.insee.kraftwerk.core.sequence.WriterSequence;
import fr.insee.kraftwerk.core.utils.KraftwerkExecutionContext;
import fr.insee.kraftwerk.core.utils.SqlUtils;
import fr.insee.kraftwerk.core.utils.files.FileUtilsInterface;
import fr.insee.kraftwerk.core.vtl.VtlBindings;
import fr.insee.kraftwerk.core.vtl.VtlExecute;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

@Getter
@Slf4j
public class ReportingDataProcessing {

    public void runProcessMain(FileUtilsInterface fileUtilsInterface,
                               String defaultDirectory,
                               String inDirectoryParam,
                               String reportingDataFilePathParam
    ) throws KraftwerkException {
        Path inDirectory = Path.of(defaultDirectory, "in", inDirectoryParam);
        runProcess(fileUtilsInterface, inDirectory, inDirectory, reportingDataFilePathParam);
    }

    public void runProcessGenesis(FileUtilsInterface fileUtilsInterface,
                                  Mode mode,
                                  String defaultDirectory,
                                  String inDirectoryParam,
                                  String reportingDataFilePathParam
    ) throws KraftwerkException {
        Path inDirectory = Path.of(defaultDirectory, "in", mode.getFolder(), inDirectoryParam);
        Path specDirectory = Path.of(defaultDirectory, "specs", inDirectoryParam);
        runProcess(fileUtilsInterface, inDirectory, specDirectory, reportingDataFilePathParam);
    }


    private void runProcess(FileUtilsInterface fileUtilsInterface,
                           Path inDirectory,
                           Path inOrSpecDirectory,
                           String reportingDataFilePathParam
    ) throws KraftwerkException {
        Path reportingDataFilePath = inDirectory.resolve(reportingDataFilePathParam);
        SurveyRawData surveyRawData = new SurveyRawData();
        surveyRawData.setMetadataModel(new MetadataModel());
        ModeInputs modeInputs = new ModeInputs();
        modeInputs.setReportingDataFile(reportingDataFilePath);

        parseReportingData(modeInputs, surveyRawData, fileUtilsInterface);

        VtlExecute vtlExecute = new VtlExecute(fileUtilsInterface);
        VtlBindings vtlBindings = new VtlBindings();
        vtlExecute.convertToVtlDataset(surveyRawData, Constants.REPORTING_DATA_GROUP_NAME, vtlBindings);

        /* Apply reporting data VTL transformations */
        DataProcessing dataProcessing = DataProcessingManager.getProcessingClass(
                DataFormat.LUNATIC_XML,
                vtlBindings,
                new MetadataModel(),
                fileUtilsInterface);
        KraftwerkExecutionContext context = new KraftwerkExecutionContext("",false,true,false,0L);
        dataProcessing.applyVtlTransformations(
                Constants.REPORTING_DATA_GROUP_NAME,
                Path.of(Constants.VTL_FOLDER_PATH)
                        .resolve("reporting_datas.vtl"),
                context);

        try (Connection writeDatabaseConnection = SqlUtils.openConnection()) {
            try(Statement writeDatabase = writeDatabaseConnection.createStatement()){
                InsertDatabaseSequence insertDatabaseSequence = new InsertDatabaseSequence();
                insertDatabaseSequence.insertDatabaseProcessing(vtlBindings, writeDatabase);
                WriterSequence writerSequence = new WriterSequence();
                /*
                writerSequence.writeOutputFiles(inOrSpecDirectory,
                        "_REPORTING_DATA_ONLY",
                        vtlBindings,
                        modeInputs,
                        context,
                        writeDatabase,
                        fileUtilsInterface
                );
                */
                writerSequence.writeOutputFilesV2(inOrSpecDirectory,
                        "_REPORTING_DATA_ONLY",
                        vtlBindings,
                        modeInputs,
                        context,
                        writeDatabase,
                        fileUtilsInterface
                );
            }
        }catch (SQLException e){
            log.error(e.toString());
            throw new KraftwerkException(500, "SQL Error");
        }
    }

    public void parseReportingData(ModeInputs modeInputs, SurveyRawData data, FileUtilsInterface fileUtilsInterface) throws KraftwerkException {
        Path reportingDataFile = modeInputs.getReportingDataFile();
        if (reportingDataFile != null) {
            ReportingData reportingData = new ReportingData(reportingDataFile, new ArrayList<>());
            if (reportingDataFile.toString().contains(".xml")) {
                XMLReportingDataParser xMLReportingDataParser = new XMLReportingDataParser(fileUtilsInterface);
                xMLReportingDataParser.parseReportingData(reportingData, data, true);

            } else if (reportingDataFile.toString().contains(".csv")) {
                CSVReportingDataParser cSVReportingDataParser = new CSVReportingDataParser(fileUtilsInterface);
                cSVReportingDataParser.parseReportingData(reportingData, data, true);
            }
        }
    }
}
