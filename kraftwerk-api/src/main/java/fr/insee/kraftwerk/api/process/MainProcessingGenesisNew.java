package fr.insee.kraftwerk.api.process;

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

import java.io.IOException;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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

    public Map<String, Object> runMainJson(String questionnaireModelId, String interrogationId) throws KraftwerkException, IOException {
        Map<String, Object> results = new LinkedHashMap<>();
        results.put("interrogationId", interrogationId);
        log.info("Export json for questionnaireModelId {} and interrogationId {}", questionnaireModelId, interrogationId);
        String databasePath = ("%s/kraftwerk_temp/%s/db.duckdb".formatted(System.getProperty("java.io.tmpdir"),
                questionnaireModelId));
        //We delete database at start (in case there is already one)
        SqlUtils.deleteDatabaseFile(databasePath);
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
            if (questionnaireModelId.isEmpty()) {
                throw new KraftwerkException(204, null);
            }
            results.put("questionnaireModelId", questionnaireModelId);

            InterrogationId id = new InterrogationId();
            id.setId(interrogationId);
            List<SurveyUnitUpdateLatest> suLatest = client.getUEsLatestState(questionnaireModelId, List.of(id));
            results.put("partitionId",suLatest.getFirst().getCampaignId());
            results.put("surveyUnitId",suLatest.getFirst().getSurveyUnitId());
            results.put("contextualId",suLatest.getFirst().getContextualId());
            results.put("mode",suLatest.getFirst().getMode());
            results.put("isCapturedIndirectly",suLatest.getFirst().getIsCapturedIndirectly());
            results.put("validationDate",suLatest.getFirst().getValidationDate());
            log.info("InterrogationId {} retrieved from Genesis",id.getId());
            vtlBindings = new VtlBindings();
            unimodalProcess(suLatest);
            multimodalProcess();
            insertDatabase();
            results.put("data",transformDataToJson());
            if (!database.isClosed()){database.close();}
        }catch (SQLException e){
            log.error(e.toString());
            throw new KraftwerkException(500,"SQL error");
        }
        SqlUtils.deleteDatabaseFile(databasePath);
        return results;
    }

    private Map<String,Object> transformDataToJson() throws KraftwerkException {
        Map<String, Object> resultByScope = new LinkedHashMap<>();
        try {
            MetadataModel firstMetadataModel = metadataModelsByMode.entrySet().iterator().next().getValue();
            Map<String, Group> groups = firstMetadataModel.getGroups();
            for (String group : groups.keySet()){
                List<Object> listIteration = new ArrayList<>();
                ResultSet rs = database.executeQuery("SELECT * FROM "+group);
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
