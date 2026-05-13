package fr.insee.kraftwerk.api.process;

import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.insee.kraftwerk.api.client.GenesisClient;
import fr.insee.kraftwerk.api.configuration.ConfigProperties;
import fr.insee.kraftwerk.api.dto.InterrogationBatchResponse;
import fr.insee.kraftwerk.api.dto.DebugErrorDto;
import fr.insee.kraftwerk.api.dto.DebugJsonExportResultDto;
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
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Instant;
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

    public void runMain(String collectionInstrumentId, int batchSize, Mode dataMode) throws KraftwerkException, IOException {
        log.info("Batch size of interrogations retrieved from Genesis: {}", batchSize);
        String databasePath = ("%s/kraftwerk_temp/%s/db.duckdb".formatted(System.getProperty(JAVA_TMPDIR_PROPERTY),
                collectionInstrumentId));
        //We delete database at start (in case there is already one)
        SqlUtils.deleteDatabaseFile(databasePath);
        log.info("Kraftwerk main service started for questionnaire: {} {}", collectionInstrumentId, kraftwerkExecutionContext.isWithDDI()
                ? "with DDI": "without DDI");
        List<Mode> modes = client.getModesByQuestionnaire(collectionInstrumentId);
        init(collectionInstrumentId, modes);
        //Try with resources to close database when done
        try (Connection tryDatabase = config.isDuckDbInMemory() ?
                SqlUtils.openConnection()
                : SqlUtils.openConnection(Path.of(databasePath))) {
            if(tryDatabase == null){
                throw new KraftwerkException(500,"Error during internal database creation");
            }
            this.database = tryDatabase.createStatement();
            processDataByBatch(collectionInstrumentId, batchSize, dataMode);
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
    public boolean runMainJson(String collectionInstrumentId, int batchSize, Mode dataMode, Instant since) throws KraftwerkException, IOException {

        long start = System.currentTimeMillis();

        Instant beginDate = resolveBeginDate(collectionInstrumentId, dataMode, since);

        InterrogationBatchResponse batchResponse =
                fetchInterrogationBatch(collectionInstrumentId, beginDate);

        List<InterrogationId> ids = batchResponse.getInterrogationIds();

        if (ids.isEmpty()) {
            log.info("No interrogation to process collectionInstrumentId={} since={}", collectionInstrumentId, beginDate);
            runMainJsonInternal(collectionInstrumentId, batchSize, dataMode, ids, false);
            return false;
        }

        log.info("Processing {} interrogationIds for collectionInstrumentId={}", ids.size(), collectionInstrumentId);
        if (batchResponse.getNextSince()!=null) kraftwerkExecutionContext.setRecordedBefore(batchResponse.getNextSince());

        runMainJsonInternal(collectionInstrumentId, batchSize, dataMode, ids, true);

        long duration = System.currentTimeMillis() - start;
        log.info("Processed {} interrogationId for collectionInstrumentId={} in {} ms", ids.size(), collectionInstrumentId, duration);
        return true;
    }

    public void runMainJsonReplay(String collectionInstrumentId,
                                  int batchSize,
                                  Mode dataMode,
                                  Instant start,
                                  @Nullable Instant end)
            throws KraftwerkException, IOException {

        List<InterrogationId> ids =
                fetchInterrogationIdsWithRecordDateBetween(collectionInstrumentId, start, end);

        runMainJsonInternal(collectionInstrumentId, batchSize, dataMode, ids, false);
    }

    private void runMainJsonInternal(
            String id,
            int batchSize,
            Mode dataMode,
            List<InterrogationId> ids,
            boolean updateLastExtraction
    ) throws KraftwerkException, IOException {

        String databasePath = ("%s/kraftwerk_temp/%s/db.duckdb"
                .formatted(System.getProperty(JAVA_TMPDIR_PROPERTY), id));

        //We delete database at start (in case there is already one)
        SqlUtils.deleteDatabaseFile(databasePath);

        List<Mode> modes = client.getModesByQuestionnaire(id);
        init(id, modes);

        Path tmpOutputFile = createTempOutputFile(id);

        //Try with resources to close database when done
        try (Connection connection = openDatabaseConnection(databasePath);
             JsonGenerator jsonGenerator = createJsonGenerator(tmpOutputFile)) {

            this.database = connection.createStatement();

            List<List<InterrogationId>> partitions = ListUtils.partition(ids, batchSize);
            int nbPartitions = partitions.size();
            int indexPartition = 1;

            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.configure(JsonGenerator.Feature.WRITE_BIGDECIMAL_AS_PLAIN, true);
            jsonGenerator.writeStartArray();

            for (List<InterrogationId> listId : partitions) {
                List<SurveyUnitUpdateLatest> suLatest = client.getResponses(id, listId, kraftwerkExecutionContext.getRecordedBefore());
                log.info("Number of documents retrieved from database : {}, partition {}/{}", suLatest.size(), indexPartition, nbPartitions);
                vtlBindings = new VtlBindings();
                // if one mode is specified we filter to keep data of that mode only
                if (dataMode != null) {
                    suLatest = suLatest.stream().filter(su -> su.getMode() == dataMode).toList();
                    log.info("Number of documents kept for mode {}", dataMode);
                }
                unimodalProcess(suLatest);
                multimodalProcess();
                insertDatabase();
                tmpJsonFileWriter(listId, suLatest, objectMapper, jsonGenerator, database);
                indexPartition++;
            }

            jsonGenerator.writeEndArray(); // End of Json Array
            if (!database.isClosed()) database.close();
        } catch (SQLException e) {
            log.error(e.toString());
            throw new KraftwerkException(500, "SQL error");
        }

        moveTempFile(outputFileName(id), tmpOutputFile);
        writeErrors();

        if (updateLastExtraction) {
            LastJsonExtractionDate lastJsonExtractionDate = new LastJsonExtractionDate();
            lastJsonExtractionDate.setLastExtractionDate(kraftwerkExecutionContext.getRecordedBefore());
            client.saveDateExtraction(id, dataMode, lastJsonExtractionDate);
        }

        SqlUtils.deleteDatabaseFile(databasePath);
    }

    /**
     * DEBUG ONLY.
     *
     * Helper method to debug problematic InterrogationIds.
     * Processes each InterrogationId individually (with isolated try/catch)
     * to identify failures without stopping the whole execution.
     *
     * This method intentionally duplicates part of the main logic for debugging purposes
     */
    public DebugJsonExportResultDto runMainJsonDebug(
            String id,
            int batchSize,
            Mode dataMode,
            List<InterrogationId> interrogationIds
    ) throws KraftwerkException, IOException {

        String databasePath = ("%s/kraftwerk_temp/%s/db_debug.duckdb"
                .formatted(System.getProperty(JAVA_TMPDIR_PROPERTY), id));

        SqlUtils.deleteDatabaseFile(databasePath);

        List<Mode> modes = client.getModesByQuestionnaire(id);
        init(id, modes);

        Path tmpOutputFile = createTempOutputFile(id);
        List<InterrogationId> successIds = new ArrayList<>();
        List<DebugErrorDto> errors = new ArrayList<>();

        try (Connection connection = openDatabaseConnection(databasePath);
             JsonGenerator jsonGenerator = createJsonGenerator(tmpOutputFile)) {

            this.database = connection.createStatement();

            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.configure(JsonGenerator.Feature.WRITE_BIGDECIMAL_AS_PLAIN, true);

            jsonGenerator.writeStartArray();

            List<List<InterrogationId>> partitions = ListUtils.partition(interrogationIds, batchSize);
            int nbPartitions = partitions.size();
            int indexPartition = 1;

            for (List<InterrogationId> part : partitions) {
                log.info("DEBUG partition {}/{} (size={})", indexPartition, nbPartitions, part.size());

                for (InterrogationId interrogationId : part) {
                    List<SurveyUnitUpdateLatest> suLatest = null;

                    try {
                        log.info("DEBUG processing interrogationId={}", interrogationId.getId());

                        suLatest = client.getResponses(id, List.of(interrogationId), Instant.now());

                        if (suLatest == null || suLatest.isEmpty()) {
                            log.warn("DEBUG EMPTY interrogationId={} -> no SurveyUnit found", interrogationId);
                            errors.add(new DebugErrorDto(interrogationId, null, "No SurveyUnit found"));
                            continue;
                        }

                        vtlBindings = new VtlBindings();

                        if (dataMode != null) {
                            suLatest = suLatest.stream()
                                    .filter(su -> su.getMode() == dataMode)
                                    .toList();
                        }

                        if (suLatest.isEmpty()) {
                            errors.add(new DebugErrorDto(interrogationId, null, "No SurveyUnit found after dataMode filtering"));
                            continue;
                        }

                        unimodalProcess(suLatest);
                        multimodalProcess();
                        insertDatabase();

                        tmpJsonFileWriter(List.of(interrogationId), suLatest, objectMapper, jsonGenerator, database);

                        successIds.add(interrogationId);

                    } catch (Exception e) {
                        String usuId = (suLatest != null && !suLatest.isEmpty())
                                ? suLatest.getFirst().getUsualSurveyUnitId()
                                : null;

                        log.error("DEBUG FAILED interrogationId={}, usualSurveyUnitId={}", interrogationId, usuId, e);

                        errors.add(new DebugErrorDto(
                                interrogationId,
                                usuId,
                                e.getMessage()
                        ));
                    }
                }

                indexPartition++;
            }

            jsonGenerator.writeEndArray();

            if (!database.isClosed()) {
                database.close();
            }

        } catch (SQLException e) {
            log.error(e.toString());
            throw new KraftwerkException(500, "SQL error");
        } finally {
            SqlUtils.deleteDatabaseFile(databasePath);
        }
        moveTempFile(outputFileName(id), tmpOutputFile);
        writeErrors();

        return new DebugJsonExportResultDto(id, successIds, errors);
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
    private Instant resolveBeginDate(String questionnaireModelId,
                                           Mode dataMode,
                                           @Nullable Instant since) {
        // 1. Explicit date provided
        if (since != null) {
            log.info("Using provided extraction start date {} for questionnaire {}", since, questionnaireModelId);
            return since;
        }

        // 2. Try to retrieve from Genesis
        log.info("No date specified, trying differential extraction for questionnaire {} and mode {}",
                questionnaireModelId, dataMode);

        try {
            LastJsonExtractionDate lastExtractDate = client.getLastExtractionDate(questionnaireModelId, dataMode);
            Instant beginDate = lastExtractDate.getLastExtractionDate();

            log.info("Extracting data between {} and now for questionnaire {}", beginDate, questionnaireModelId);
            return beginDate;

        } catch (KraftwerkException e) {
            log.info("No extraction date found in database → extracting all data (reason: {}) for questionnaire  {}", e.getMessage(), questionnaireModelId);
            return null;
        }
    }

    private InterrogationBatchResponse fetchInterrogationBatch(String questionnaireModelId,
                                                             @Nullable Instant beginDate)
            throws KraftwerkException {
        if (beginDate != null) {
            return client.getInterrogationBatchSince(questionnaireModelId, beginDate);
        }
        return client.getInterrogationBatchAll(questionnaireModelId);
    }

    private List<InterrogationId> fetchInterrogationIdsWithRecordDateBetween(
            String collectionInstrumentId,
            Instant start,
            @Nullable Instant end
    ) throws KraftwerkException {

         end = (end != null)
                ? end
                : Instant.now();

        if (end.isBefore(start)) {
            throw new KraftwerkException(400, "endDate must be after startDate");
        }

        return client.getInterrogationBatchBetween(collectionInstrumentId, start, end).getInterrogationIds();
    }

    private void moveTempFile(String filename, Path tmpOutputFile) throws KraftwerkException {
        Path outDirectory = FileUtilsInterface.transformToOut(specsDirectory,kraftwerkExecutionContext.getExecutionDateTime());
        Path outputPath = outDirectory.resolve(filename);
        kraftwerkExecutionContext.setOutDirectory(outDirectory);
        fileUtilsInterface.moveFile(tmpOutputFile,outputPath.toString());
        log.info("File: {} successfully written", outputPath);
    }

    /**
     * Constructs the output file name for the JSON export of a given questionnaire.
     * <p>
     * The filename includes the questionnaire ID, a timestamp of the current date and time,
     * and the appropriate extension depending on whether encryption is enabled.
     *
     * @param questionnaireModelId the identifier of the questionnaire
     * @return the constructed output file name, including the extension
     */
    public String outputFileName(String questionnaireModelId) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");
        String timestamp = LocalDateTime.now().format(formatter);
        String fileName = String.format("export_%s_%s", questionnaireModelId, timestamp);
        return fileName + JSON_EXTENSION;
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
