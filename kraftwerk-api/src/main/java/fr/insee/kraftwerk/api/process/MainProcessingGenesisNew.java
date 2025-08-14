package fr.insee.kraftwerk.api.process;

import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.insee.bpm.metadata.model.Group;
import fr.insee.bpm.metadata.model.MetadataModel;
import fr.insee.kraftwerk.api.client.GenesisClient;
import fr.insee.kraftwerk.api.configuration.ConfigProperties;
import fr.insee.kraftwerk.core.data.model.InterrogationId;
import fr.insee.kraftwerk.core.data.model.Mode;
import fr.insee.kraftwerk.core.data.model.SurveyUnitUpdateLatest;
import fr.insee.kraftwerk.core.exceptions.KraftwerkException;
import fr.insee.kraftwerk.core.utils.KraftwerkExecutionContext;
import fr.insee.kraftwerk.core.utils.SqlUtils;
import fr.insee.kraftwerk.core.utils.files.FileUtilsInterface;
import fr.insee.kraftwerk.core.vtl.VtlBindings;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.ListUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
public class MainProcessingGenesisNew extends AbstractMainProcessingGenesis{

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
        String databasePath = ("%s/kraftwerk_temp/%s/db.duckdb".formatted(System.getProperty("java.io.tmpdir"),
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

    public void runMainJson(String questionnaireModelId, int batchSize, Mode dataMode) throws KraftwerkException, IOException {

        log.info("Export json for questionnaireModelId {}", questionnaireModelId);
        String databasePath = ("%s/kraftwerk_temp/%s/db.duckdb".formatted(System.getProperty("java.io.tmpdir"),
                questionnaireModelId));
        //We delete database at start (in case there is already one)
        SqlUtils.deleteDatabaseFile(databasePath);
        List<Mode> modes = client.getModesByQuestionnaire(questionnaireModelId);
        init(questionnaireModelId, modes);
        JsonFactory jsonFactory = new JsonFactory();
        ObjectMapper objectMapper = new ObjectMapper();

        // Construct filename
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");
        String timestamp = LocalDateTime.now().format(formatter);
        String fileName = String.format("export_%s_%s.json", questionnaireModelId, timestamp);

        Path outDirectory = FileUtilsInterface.transformToOut(specsDirectory,kraftwerkExecutionContext.getExecutionDateTime());
        try {
            if (Files.notExists(outDirectory)) {
                Files.createDirectories(outDirectory);
                log.info("Created folder: {}", outDirectory);
            }
        } catch (IOException e) {
            log.error("Permission refused to create folder: {} : {}", outDirectory.getParent(), e);
        }
        Path outputPath = outDirectory.resolve(fileName);
        //Try with resources to close database when done
        try (Connection tryDatabase = config.isDuckDbInMemory() ?
                SqlUtils.openConnection()
                : SqlUtils.openConnection(Path.of(databasePath));
                JsonGenerator jsonGenerator = jsonFactory.createGenerator(outputPath.toFile(), JsonEncoding.UTF8))
        {
            if(tryDatabase == null){
                throw new KraftwerkException(500,"Error during internal database creation");
            }
            this.database = tryDatabase.createStatement();
            if (questionnaireModelId.isEmpty()) {
                throw new KraftwerkException(204, null);
            }

            List<InterrogationId> ids = client.getInterrogationIds(questionnaireModelId);
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
                writeJsonOutput(listId, suLatest, objectMapper, jsonGenerator);
                indexPartition++;
            }
            jsonGenerator.writeEndArray(); // End of Json Array
            if (!database.isClosed()){database.close();}
        }catch (SQLException e){
            log.error(e.toString());
            throw new KraftwerkException(500,"SQL error");
        }
        SqlUtils.deleteDatabaseFile(databasePath);
    }

    private void writeJsonOutput(List<InterrogationId> listId, List<SurveyUnitUpdateLatest> suLatest, ObjectMapper objectMapper, JsonGenerator jsonGenerator) throws KraftwerkException, IOException {
        for (InterrogationId interrogationId : listId){
            Map<String, Object> result = buildResultMap(suLatest, interrogationId);
            if (result != null) {
                objectMapper.writeValue(jsonGenerator, result);
            }
        }
    }

    private Map<String,Object> buildResultMap(List<SurveyUnitUpdateLatest> suLatest, InterrogationId interrogationId) throws KraftwerkException{
        Map<String,Object> resultById = new HashMap<>();
        SurveyUnitUpdateLatest currentSu = suLatest.stream()
                .filter(su -> su.getInterrogationId().equals(interrogationId.getId()))
                .findFirst()
                .orElse(null);

        if (currentSu == null) return null;

        resultById.put("partitionId",currentSu.getCampaignId());
        resultById.put("interrogationId", interrogationId.getId());
        resultById.put("surveyUnitId",currentSu.getSurveyUnitId());
        resultById.put("contextualId",currentSu.getContextualId());
        resultById.put("questionnaireModelId",currentSu.getQuestionnaireId());
        resultById.put("mode",currentSu.getMode());
        resultById.put("isCapturedIndirectly",currentSu.getIsCapturedIndirectly());
        resultById.put("validationDate",currentSu.getValidationDate());
        resultById.put("data",transformDataToJson(interrogationId));
        return resultById;
    }

    private Map<String,Object> transformDataToJson(InterrogationId interrogationId) throws KraftwerkException {
        Map<String, Object> resultByScope = new LinkedHashMap<>();
        try {
            MetadataModel firstMetadataModel = metadataModelsByMode.entrySet().iterator().next().getValue();
            Map<String, Group> groups = firstMetadataModel.getGroups();
            for (String group : groups.keySet()){
                List<Object> listIteration = new ArrayList<>();
                String request = String.format("SELECT * FROM %s WHERE interrogationId='%s'", group, interrogationId.getId());
                ResultSet rs = database.executeQuery(request);
                ResultSetMetaData meta = rs.getMetaData();
                int cols = meta.getColumnCount();
                while (rs.next()) {
                    Map<String, Object> row = new LinkedHashMap<>();
                    for (int i = 1; i <= cols; i++) {
                        if (meta.getColumnLabel(i).equals("interrogationId")) continue;
                        row.put(meta.getColumnLabel(i), rs.getObject(i));
                    }
                    listIteration.add(row);
                }
                resultByScope.put(group,listIteration);
            }
        } catch (Exception e){
            log.error(e.getMessage());
            throw new KraftwerkException(500,"SQL error : extraction step");
        }
        return resultByScope;
    }

}
