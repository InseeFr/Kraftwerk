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
import org.jetbrains.annotations.Nullable;
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
import java.util.Optional;

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
        log.info("Export json for questionnaireModelId {}", questionnaireModelId);

        LocalDateTime beginDate = resolveBeginDate(questionnaireModelId, dataMode, since);

        String databasePath = ("%s/kraftwerk_temp/%s/db.duckdb".formatted(System.getProperty(JAVA_TMPDIR_PROPERTY),
                questionnaireModelId));
        //We delete database at start (in case there is already one)
        SqlUtils.deleteDatabaseFile(databasePath);

        List<Mode> modes = client.getModesByQuestionnaire(questionnaireModelId);
        init(questionnaireModelId, modes);

        Path tmpOutputFile = createTempOutputFile(questionnaireModelId);

        //Try with resources to close database when done
        try (Connection connection = openDatabaseConnection(databasePath);
             JsonGenerator jsonGenerator = createJsonGenerator(tmpOutputFile))
        {
            this.database = connection.createStatement();

            List<InterrogationId> ids = fetchInterrogationIds(questionnaireModelId, beginDate);
            List<List<InterrogationId>> partitions  = ListUtils.partition(ids, batchSize);
            int nbPartitions = partitions .size();
            int indexPartition = 1;

            ObjectMapper objectMapper = new ObjectMapper();
            jsonGenerator.writeStartArray(); // Beginning of Json Array

            for (List<InterrogationId> listId : partitions ) {
                List<SurveyUnitUpdateLatest> suLatest = client.getUEsLatestState(questionnaireModelId, listId);
                log.info("Number of documents retrieved from database : {}, partition {}/{}", suLatest.size(), indexPartition, nbPartitions);
                vtlBindings = new VtlBindings();
                // if one mode is specified we filter to keep data of that mode only
                if (dataMode != null){
                    suLatest = suLatest.stream().filter(su-> su.getMode()==dataMode).toList();
                    log.info("Number of documents kept for mode {}", dataMode);
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

    private Path createTempOutputFile(String questionnaireModelId) throws IOException {
        Files.createDirectories(Path.of(System.getProperty(JAVA_TMPDIR_PROPERTY)));
        return Files.createTempFile(Path.of(System.getProperty(JAVA_TMPDIR_PROPERTY)),
                outputFileName(questionnaireModelId), null);
    }

    private JsonGenerator createJsonGenerator(Path tmpOutputFile) throws IOException {
        JsonFactory jsonFactory = new JsonFactory();
        return jsonFactory.createGenerator(tmpOutputFile.toFile(), JsonEncoding.UTF8);
    }

    @SuppressWarnings("ConstantConditions")
    private Connection openDatabaseConnection(String databasePath) throws SQLException, KraftwerkException {
        Connection connection = config.isDuckDbInMemory()
                ? SqlUtils.openConnection()
                : SqlUtils.openConnection(Path.of(databasePath));
        if (connection == null) {
            throw new KraftwerkException(500, "Error during internal database creation");
        }
        return connection;
    }

    /**
     * Resolve the beginning date for extraction.
     * <p>
     * Priority :
     * 1. The explicit 'since' parameter (highest priority),
     * 2. The last extraction date from Genesis,
     * 3. Or null if nothing is found (meaning: extract all data).
     */
    private LocalDateTime resolveBeginDate(String questionnaireModelId,
                                           Mode dataMode,
                                           @Nullable LocalDateTime since) {
        // 1. Explicit date provided
        if (since != null) {
            log.info("Using provided extraction start date: {}", since);
            return since;
        }

        // 2. Try to retrieve from Genesis
        log.info("No date specified, trying differential extraction for questionnaire {} and mode {}",
                questionnaireModelId, dataMode);

        try {
            LastJsonExtractionDate lastExtractDate = client.getLastExtractionDate(questionnaireModelId, dataMode);
            LocalDateTime beginDate = LocalDateTime.parse(lastExtractDate.getLastExtractionDate());

            log.info("Extracting data between {} and now", beginDate);
            return beginDate;

        } catch (KraftwerkException e) {
            log.info("No extraction date found in database → extracting all data (reason: {})", e.getMessage());
            return null;
        }
    }

    private List<InterrogationId> fetchInterrogationIds(String questionnaireModelId,
                                                        @Nullable LocalDateTime beginDate)
            throws KraftwerkException {
        if (beginDate != null) {
            return client.getInterrogationIdsFromDate(questionnaireModelId, beginDate);
        }
        return client.getInterrogationIds(questionnaireModelId);
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
