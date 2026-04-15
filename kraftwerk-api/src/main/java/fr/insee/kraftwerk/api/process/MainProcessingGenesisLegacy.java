package fr.insee.kraftwerk.api.process;

import fr.insee.kraftwerk.api.client.GenesisClient;
import fr.insee.kraftwerk.api.configuration.ConfigProperties;
import fr.insee.kraftwerk.core.data.model.Mode;
import fr.insee.kraftwerk.core.exceptions.KraftwerkException;
import fr.insee.kraftwerk.core.utils.KraftwerkExecutionContext;
import fr.insee.kraftwerk.core.utils.SqlUtils;
import fr.insee.kraftwerk.core.utils.files.FileUtilsInterface;
import lombok.extern.log4j.Log4j2;

import java.io.IOException;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

@Log4j2
public class MainProcessingGenesisLegacy extends AbstractMainProcessingGenesis{

	public MainProcessingGenesisLegacy(
			ConfigProperties config,
			GenesisClient genesisClient,
		 	FileUtilsInterface fileUtilsInterface,
			KraftwerkExecutionContext kraftwerkExecutionContext
	) {
		super(config,genesisClient,fileUtilsInterface,kraftwerkExecutionContext);
	}

	public void runMain(String campaignId, int batchSize) throws KraftwerkException, IOException {
		log.info("Batch size of interrogations retrieved from Genesis: {}", batchSize);
		String databasePath = ("%s/kraftwerk_temp/%s/db.duckdb".formatted(System.getProperty("java.io.tmpdir"),
				campaignId));
		//We delete database at start (in case there is already one)
		SqlUtils.deleteDatabaseFile(databasePath);
		log.info("Kraftwerk main service started for campaign: {} {}", campaignId, kraftwerkExecutionContext.isWithDDI()
				? "with DDI": "without DDI");
		List<Mode> modes = client.getModes(campaignId);
		init(campaignId, modes);
		//Try with resources to close database when done
		try (Connection tryDatabase = config.isDuckDbInMemory() ?
				SqlUtils.openConnection()
				: SqlUtils.openConnection(Path.of(databasePath))) {
			if(tryDatabase == null){
				throw new KraftwerkException(500,"Error during internal database creation");
			}
			this.database = tryDatabase.createStatement();
			List<String> questionnaireModelIds = client.getQuestionnaireModelIds(campaignId);
			if (questionnaireModelIds.isEmpty()) {
				throw new KraftwerkException(204, null);
			}
			for (String questionnaireId : questionnaireModelIds) {
				processDataByBatch(questionnaireId,batchSize,null);
			}
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
