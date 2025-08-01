package fr.insee.kraftwerk.api.process;

import fr.insee.kraftwerk.api.client.GenesisClient;
import fr.insee.kraftwerk.api.configuration.ConfigProperties;
import fr.insee.kraftwerk.core.data.model.Mode;
import fr.insee.kraftwerk.core.exceptions.KraftwerkException;
import fr.insee.kraftwerk.core.utils.KraftwerkExecutionContext;
import fr.insee.kraftwerk.core.utils.SqlUtils;
import fr.insee.kraftwerk.core.utils.files.FileUtilsInterface;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.SQLException;

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
        init(questionnaireModelId);
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

}
