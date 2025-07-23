package fr.insee.kraftwerk.api.process;

import fr.insee.bpm.exceptions.MetadataParserException;
import fr.insee.bpm.metadata.model.Group;
import fr.insee.bpm.metadata.model.MetadataModel;
import fr.insee.kraftwerk.api.client.GenesisClient;
import fr.insee.kraftwerk.api.configuration.ConfigProperties;
import fr.insee.kraftwerk.core.data.model.InterrogationId;
import fr.insee.kraftwerk.core.data.model.SurveyUnitUpdateLatest;
import fr.insee.kraftwerk.core.exceptions.KraftwerkException;
import fr.insee.kraftwerk.core.inputs.UserInputsGenesis;
import fr.insee.kraftwerk.core.metadata.MetadataUtilsGenesis;
import fr.insee.kraftwerk.core.sequence.BuildBindingsSequenceGenesis;
import fr.insee.kraftwerk.core.sequence.ControlInputSequenceGenesis;
import fr.insee.kraftwerk.core.sequence.InsertDatabaseSequence;
import fr.insee.kraftwerk.core.sequence.MultimodalSequence;
import fr.insee.kraftwerk.core.sequence.UnimodalSequence;
import fr.insee.kraftwerk.core.sequence.WriterSequence;
import fr.insee.kraftwerk.core.utils.KraftwerkExecutionContext;
import fr.insee.kraftwerk.core.utils.SqlUtils;
import fr.insee.kraftwerk.core.utils.TextFileWriter;
import fr.insee.kraftwerk.core.utils.files.FileUtilsInterface;
import fr.insee.kraftwerk.core.vtl.VtlBindings;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.collections4.ListUtils;

import java.io.IOException;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Log4j2
public class MainProcessingGenesis {

	@Setter
	private ControlInputSequenceGenesis controlInputSequenceGenesis;
	@Getter
	private VtlBindings vtlBindings = new VtlBindings();
	@Getter
	private UserInputsGenesis userInputs;
	private final FileUtilsInterface fileUtilsInterface;
	private Statement database;

	private final KraftwerkExecutionContext kraftwerkExecutionContext;

	/* SPECIFIC VARIABLES */
	@Getter
	private Path specsDirectory;
	/**
	 * Map by mode
	 */
	@Getter
	private Map<String, MetadataModel> metadataModels;

	private final GenesisClient client;
	private final ConfigProperties config;

	public MainProcessingGenesis(
			ConfigProperties config,
			GenesisClient genesisClient,
		 	FileUtilsInterface fileUtilsInterface,
			KraftwerkExecutionContext kraftwerkExecutionContext
	) {
		this.config = config;
		this.client = genesisClient;
		this.fileUtilsInterface = fileUtilsInterface;
		this.kraftwerkExecutionContext = kraftwerkExecutionContext;
	}

	public void init(String campaignId) throws KraftwerkException {
		log.info("Kraftwerk main service started for campaign: {} {}", campaignId, kraftwerkExecutionContext.isWithDDI()
				? "with DDI": "without DDI");
		this.controlInputSequenceGenesis = new ControlInputSequenceGenesis(client.getConfigProperties().getDefaultDirectory());
		specsDirectory = controlInputSequenceGenesis.getSpecsDirectory(campaignId);
		//First we check the modes present in database for the given questionnaire
		//We build userInputs for the given questionnaire
		userInputs = new UserInputsGenesis(specsDirectory,
				client.getModes(campaignId), fileUtilsInterface, kraftwerkExecutionContext.isWithDDI());
		if (!userInputs.getModes().isEmpty()) {
            try {
                metadataModels = kraftwerkExecutionContext.isWithDDI() ? MetadataUtilsGenesis.getMetadata(userInputs.getModeInputsMap(), fileUtilsInterface): MetadataUtilsGenesis.getMetadataFromLunatic(userInputs.getModeInputsMap(), fileUtilsInterface);
			} catch (MetadataParserException e) {
                throw new KraftwerkException(500, e.getMessage());
            }
        } else {
            log.error("No source found for campaign {}", campaignId);
		}
	}

	public void runMain(String campaignId, int batchSize) throws KraftwerkException, IOException {
		log.info("Batch size of interrogations retrieved from Genesis: {}", batchSize);
		String databasePath = ("%s/kraftwerk_temp/%s/db.duckdb".formatted(System.getProperty("java.io.tmpdir"),
				campaignId));
		//We delete database at start (in case there is already one)
		SqlUtils.deleteDatabaseFile(databasePath);
		init(campaignId);
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
				List<InterrogationId> ids = client.getInterrogationIds(questionnaireId);
				List<List<InterrogationId>> listIds = ListUtils.partition(ids, batchSize);
				int nbPartitions = listIds.size();
				int indexPartition = 1;
				for (List<InterrogationId> listId : listIds) {
					List<SurveyUnitUpdateLatest> suLatest = client.getUEsLatestState(questionnaireId, listId);
					log.info("Number of documents retrieved from database : {}, partition {}/{}", suLatest.size(), indexPartition, nbPartitions);
					vtlBindings = new VtlBindings();
					unimodalProcess(suLatest);
					multimodalProcess();
					insertDatabase();
					indexPartition++;
				}
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

	public Map<String, Object> runMainJson(String campaignId, String interrogationId) throws KraftwerkException, IOException {
		Map<String, Object> results = new LinkedHashMap<>();
		results.put("interrogationId", interrogationId);
		log.info("Export json for campaign {} and interrogationId {}", campaignId, interrogationId);
		String databasePath = ("%s/kraftwerk_temp/%s/db.duckdb".formatted(System.getProperty("java.io.tmpdir"),
				campaignId));
		//We delete database at start (in case there is already one)
		SqlUtils.deleteDatabaseFile(databasePath);
		init(campaignId);
		//Try with resources to close database when done
		try (Connection tryDatabase = config.isDuckDbInMemory() ?
				SqlUtils.openConnection()
				: SqlUtils.openConnection(Path.of(databasePath))) {
			if(tryDatabase == null){
				throw new KraftwerkException(500,"Error during internal database creation");
			}
			this.database = tryDatabase.createStatement();
			String questionnaireModelId = client.getQuestionnaireModelIdByInterrogationId(interrogationId);
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

	private void unimodalProcess(List<SurveyUnitUpdateLatest> suLatest) throws KraftwerkException {
		BuildBindingsSequenceGenesis buildBindingsSequenceGenesis = new BuildBindingsSequenceGenesis(fileUtilsInterface);
		for (String dataMode : userInputs.getModeInputsMap().keySet()) {
			buildBindingsSequenceGenesis.buildVtlBindings(dataMode, vtlBindings, metadataModels, suLatest, specsDirectory);
			UnimodalSequence unimodal = new UnimodalSequence();
			unimodal.applyUnimodalSequence(userInputs, dataMode, vtlBindings, kraftwerkExecutionContext, metadataModels, fileUtilsInterface);
		}
	}

	/* Step 3 : multimodal VTL data processing */
	private void multimodalProcess() throws KraftwerkException {
		MultimodalSequence multimodalSequence = new MultimodalSequence();
		multimodalSequence.multimodalProcessing(userInputs, vtlBindings, kraftwerkExecutionContext, metadataModels,
				fileUtilsInterface);
	}

	/* Step 4 : Insert into SQL database */
	private void insertDatabase(){
		InsertDatabaseSequence insertDatabaseSequence = new InsertDatabaseSequence();
		insertDatabaseSequence.insertDatabaseProcessing(vtlBindings, database);
	}

	/* Step 5 : Write output files */
	private void outputFileWriter() throws KraftwerkException {
		WriterSequence writerSequence = new WriterSequence();
		writerSequence.writeOutputFiles(specsDirectory, vtlBindings, userInputs.getModeInputsMap(), metadataModels, kraftwerkExecutionContext, database, fileUtilsInterface);
	}

	/* Step 6 : Write errors */
	private void writeErrors() {
		TextFileWriter.writeErrorsFile(specsDirectory, kraftwerkExecutionContext, fileUtilsInterface);
	}

	private Map<String,Object> transformDataToJson() throws KraftwerkException {
		Map<String, Object> resultByScope = new LinkedHashMap<>();
		try {
			MetadataModel firstMetadataModel = metadataModels.entrySet().iterator().next().getValue();
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
