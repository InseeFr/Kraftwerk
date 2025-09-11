package fr.insee.kraftwerk.api.process;

import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.insee.kraftwerk.api.client.GenesisClient;
import fr.insee.kraftwerk.api.configuration.ConfigProperties;
import fr.insee.kraftwerk.api.dto.LastJsonExtractionDate;
import fr.insee.kraftwerk.core.data.model.InterrogationId;
import fr.insee.kraftwerk.core.data.model.Mode;
import fr.insee.kraftwerk.core.data.model.SurveyUnitUpdateLatest;
import fr.insee.kraftwerk.core.encryption.EncryptionUtils;
import fr.insee.kraftwerk.core.exceptions.KraftwerkException;
import fr.insee.kraftwerk.core.sequence.JsonWriterSequence;
import fr.insee.kraftwerk.core.utils.KraftwerkExecutionContext;
import fr.insee.kraftwerk.core.utils.SqlUtils;
import fr.insee.kraftwerk.core.utils.files.FileUtilsInterface;
import fr.insee.kraftwerk.core.vtl.VtlBindings;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.ListUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class MainProcessingGenesisNew extends AbstractMainProcessingGenesis{

    private static final String JSON_EXTENSION = ".json";

    private static final String JAVA_TMPDIR_PROPERTY = "java.io.tmpdir";

    @Autowired
    EncryptionUtils encryptionUtils;

    public MainProcessingGenesisNew(
            ConfigProperties config,
            GenesisClient genesisClient,
            FileUtilsInterface fileUtilsInterface,
            KraftwerkExecutionContext kraftwerkExecutionContext
    ) {
        super(config,genesisClient,fileUtilsInterface,kraftwerkExecutionContext);
    }

    public void runMain(String questionnaireModelId, int batchSize, Mode dataMode) throws KraftwerkException, IOException {
        log.info("Batch size of interrogations retrieved from Genesis: {}", batchSize);
        String databasePath = ("%s/kraftwerk_temp/%s/db.duckdb".formatted(System.getProperty(JAVA_TMPDIR_PROPERTY),
                questionnaireModelId));
        //We delete database at start (in case there is already one)
        SqlUtils.deleteDatabaseFile(databasePath);
        log.info("Kraftwerk main service started for questionnaire: {} {}", questionnaireModelId, kraftwerkExecutionContext.isWithDDI()
                ? "with DDI": "without DDI");
        List<Mode> modes = client.getModesByQuestionnaire(questionnaireModelId);
        init(questionnaireModelId, modes);
        //Try with resources to close database when done
        try (Connection tryDatabase = config.isDuckDbInMemory() ?
                SqlUtils.openConnection()
                : SqlUtils.openConnection(Path.of(databasePath))) {
            if(tryDatabase == null){
                throw new KraftwerkException(500,"Error during internal database creation");
            }
            this.database = tryDatabase.createStatement();
            processDataByBatch(questionnaireModelId, batchSize, dataMode);
            outputFileWriter();
            writeErrors();
            if (!database.isClosed()){database.close();}
        }catch (SQLException e){
            log.error(e.toString());
            throw new KraftwerkException(500,"SQL error");
        }
        SqlUtils.deleteDatabaseFile(databasePath);
    }

    /*
    In this method we write at the end of every batch, not at the end of all batch
    This method can do differential extraction (all data since last extraction) otherwise all data since january 1st 2025
     */
    public void runMainJson(String questionnaireModelId, int batchSize, Mode dataMode, LocalDateTime since) throws KraftwerkException, IOException {
        LocalDateTime beginDate = getBeginningDate(questionnaireModelId, dataMode, since);
        log.info("Export json for questionnaireModelId {}", questionnaireModelId);
        String databasePath = ("%s/kraftwerk_temp/%s/db.duckdb".formatted(System.getProperty(JAVA_TMPDIR_PROPERTY),
                questionnaireModelId));
        //We delete database at start (in case there is already one)
        SqlUtils.deleteDatabaseFile(databasePath);
        List<Mode> modes = client.getModesByQuestionnaire(questionnaireModelId);
        init(questionnaireModelId, modes);
        JsonFactory jsonFactory = new JsonFactory();
        ObjectMapper objectMapper = new ObjectMapper();
        // Construct filename
        Files.createDirectories(Path.of(System.getProperty(JAVA_TMPDIR_PROPERTY)));
        Path tmpOutputFile = Files.createTempFile(Path.of(System.getProperty(JAVA_TMPDIR_PROPERTY)),
                outputFileName(questionnaireModelId), null);
        //Try with resources to close database when done
        try (Connection tryDatabase = config.isDuckDbInMemory() ?
                SqlUtils.openConnection()
                : SqlUtils.openConnection(Path.of(databasePath));
                JsonGenerator jsonGenerator = jsonFactory.createGenerator(tmpOutputFile.toFile(), JsonEncoding.UTF8))
        {
            if(tryDatabase == null){
                throw new KraftwerkException(500,"Error during internal database creation");
            }
            this.database = tryDatabase.createStatement();
            if (questionnaireModelId.isEmpty()) {
                throw new KraftwerkException(204, null);
            }
            List<InterrogationId> ids = new ArrayList<>();
            if (beginDate==null){
                ids = client.getInterrogationIds(questionnaireModelId);
            } else {
                ids = client.getInterrogationIdsFromDate(questionnaireModelId,beginDate);
            }
            List<List<InterrogationId>> listIds = ListUtils.partition(ids, batchSize);
            int nbPartitions = listIds.size();
            int indexPartition = 1;

             jsonGenerator.writeStartArray(); // Beginning of Json Array

            for (List<InterrogationId> listId : listIds) {
                List<SurveyUnitUpdateLatest> suLatest = client.getUEsLatestState(questionnaireModelId, listId);
                log.info("Number of documents retrieved from database : {}, partition {}/{}", suLatest.size(), indexPartition, nbPartitions);
                vtlBindings = new VtlBindings();
                if (dataMode != null){
                    suLatest = suLatest.stream().filter(su-> su.getMode()==dataMode).toList();
                }
                unimodalProcess(suLatest);
                multimodalProcess();
                insertDatabase();
                tmpJsonFileWriter(listId, suLatest, objectMapper, jsonGenerator, database);
                indexPartition++;
            }
            jsonGenerator.writeEndArray(); // End of Json Array
            if (!database.isClosed()){database.close();}
        }catch (SQLException e){
            log.error(e.toString());
            throw new KraftwerkException(500,"SQL error");
        }
        moveTempFile(outputFileName(questionnaireModelId), tmpOutputFile);
        writeErrors();
        client.saveDateExtraction(questionnaireModelId, dataMode);
        SqlUtils.deleteDatabaseFile(databasePath);
    }

    private LocalDateTime getBeginningDate(String questionnaireModelId,Mode dataMode,LocalDateTime since) throws KraftwerkException {
        // If a date is provided we return it
        if (since != null) {
            return since;
        }
        // If no date is provided we try to retrieve last extraction date from genesis
        try {
            LastJsonExtractionDate lastExtractDate = client.getLastExtractionDate(questionnaireModelId, dataMode);
            return LocalDateTime.parse(lastExtractDate.getLastExtractionDate());
        } catch (KraftwerkException e) {
            log.info(e.getMessage());
            // If no date is found we try since the January 1st 2025
            return LocalDateTime.of(2025,1,1,0,0);
        }
    }

    private void moveTempFile(String filename, Path tmpOutputFile) throws KraftwerkException {
        Path outDirectory = FileUtilsInterface.transformToOut(specsDirectory,kraftwerkExecutionContext.getExecutionDateTime());
        try {
            if (Files.notExists(outDirectory)) {
                Files.createDirectories(outDirectory);
                log.info("Created folder: {}", outDirectory);
            }
        } catch (IOException e) {
            log.error("Permission refused to create folder: {} : {}", outDirectory.getParent(), e);
        }
        Path outputPath = outDirectory.resolve(filename);
        //Encrypt file if requested
        if(kraftwerkExecutionContext.isWithEncryption()) {
            InputStream encryptedStream = encryptionUtils.encryptOutputFile(tmpOutputFile, kraftwerkExecutionContext);
            fileUtilsInterface.writeFile(filename, encryptedStream, true);
            log.info("File: {} successfully written and encrypted", filename);
        }
        fileUtilsInterface.moveFile(tmpOutputFile,outputPath.toString());
        log.info("File: {} successfully written", outputPath);
    }

    public String outputFileName(String questionnaireModelId) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");
        String timestamp = LocalDateTime.now().format(formatter);
        String fileName = String.format("export_%s_%s", questionnaireModelId, timestamp);
        return kraftwerkExecutionContext.isWithEncryption() ?
                fileName + JSON_EXTENSION + ".enc"
                : fileName + JSON_EXTENSION;
    }

    protected void tmpJsonFileWriter(List<InterrogationId> listIds,
                                        List<SurveyUnitUpdateLatest> suLatest,
                                        ObjectMapper objectMapper,
                                        JsonGenerator jsonGenerator,
                                        Statement database) throws KraftwerkException, IOException {
        JsonWriterSequence jsonWriterSequence = new JsonWriterSequence();
        jsonWriterSequence.tmpJsonOutput(listIds, suLatest, objectMapper, jsonGenerator, metadataModelsByMode, database);
    }

}
