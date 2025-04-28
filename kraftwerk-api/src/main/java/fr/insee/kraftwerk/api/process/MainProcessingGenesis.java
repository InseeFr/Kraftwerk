package fr.insee.kraftwerk.api.process;

import fr.insee.bpm.exceptions.MetadataParserException;
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
import fr.insee.kraftwerk.core.utils.SqlUtils;
import fr.insee.kraftwerk.core.utils.TextFileWriter;
import fr.insee.kraftwerk.core.utils.files.FileUtilsInterface;
import fr.insee.kraftwerk.core.utils.log.KraftwerkExecutionContext;
import fr.insee.kraftwerk.core.vtl.VtlBindings;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.collections4.ListUtils;
import org.springframework.boot.web.client.RestTemplateBuilder;

import java.io.IOException;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
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

	private final boolean withDDI;

	private KraftwerkExecutionContext kraftwerkExecutionContext;

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

	public MainProcessingGenesis(ConfigProperties config, FileUtilsInterface fileUtilsInterface, boolean withDDI) {
		this.config = config;
		this.client = new GenesisClient(new RestTemplateBuilder(), config);
		this.fileUtilsInterface = fileUtilsInterface;
		this.withDDI = withDDI;
	}

	public MainProcessingGenesis(
			ConfigProperties config,
			GenesisClient genesisClient,
		 	FileUtilsInterface fileUtilsInterface,
		 	boolean withDDI
	) {
		this.config = config;
		this.client = genesisClient;
		this.fileUtilsInterface = fileUtilsInterface;
		this.withDDI = withDDI;
	}

	public void init(String campaignId) throws KraftwerkException {
		long initProcessStartTimeStamp = System.currentTimeMillis();
		kraftwerkExecutionContext = new KraftwerkExecutionContext();
		log.info("Kraftwerk main service started for campaign: {} {}", campaignId, withDDI ? "with DDI": "without DDI");
		this.controlInputSequenceGenesis = new ControlInputSequenceGenesis(client.getConfigProperties().getDefaultDirectory(), fileUtilsInterface);
		specsDirectory = controlInputSequenceGenesis.getSpecsDirectory(campaignId);
		//First we check the modes present in database for the given questionnaire
		//We build userInputs for the given questionnaire
		userInputs = new UserInputsGenesis(controlInputSequenceGenesis.isHasConfigFile(), specsDirectory,
				client.getModes(campaignId), fileUtilsInterface, withDDI);
		if (!userInputs.getModes().isEmpty()) {
            try {
                metadataModels = withDDI ? MetadataUtilsGenesis.getMetadata(userInputs.getModeInputsMap(), fileUtilsInterface): MetadataUtilsGenesis.getMetadataFromLunatic(userInputs.getModeInputsMap(), fileUtilsInterface);
			} catch (MetadataParserException e) {
                throw new KraftwerkException(500, e.getMessage());
            }
        } else {
            log.error("No source found for campaign {}", campaignId);
		}
		long initProcessEndTimeStamp = System.currentTimeMillis();
		long initProcessDeltaTimeStamp = initProcessEndTimeStamp - initProcessStartTimeStamp;
		log.info("====> Initialisation Process duration : {}", initProcessDeltaTimeStamp);
	}

	public void runMain(String campaignId, int batchSize) throws KraftwerkException, IOException {
		log.info("Batch size of interrogations retrieved from Genesis: {}", batchSize);
		long preProcessStartTimeStamp = System.currentTimeMillis();
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
			long preProcessEndTimeStamp = System.currentTimeMillis();
			long preProcessDeltaTimeStamp = preProcessEndTimeStamp - preProcessStartTimeStamp;
			log.info("====> preProcess duration : {}", preProcessDeltaTimeStamp);


			long queryGetQuestionnaireModelIdsStartTimeStamp = System.currentTimeMillis();
			List<String> questionnaireModelIds = client.getQuestionnaireModelIds(campaignId);
			if (questionnaireModelIds.isEmpty()) {
				throw new KraftwerkException(204, null);
			}
			long queryGetQuestionnaireModelIdsEndTimeStamp = System.currentTimeMillis();
			long queryGetQuestionnaireModelIdsDeltaTimeStamp = queryGetQuestionnaireModelIdsEndTimeStamp - queryGetQuestionnaireModelIdsStartTimeStamp;
			log.info("(DEBUG) questionnaireModelIds array size : {}", questionnaireModelIds.size());
			log.info("====> getQuestionnaireModelIds duration : {}", queryGetQuestionnaireModelIdsDeltaTimeStamp);


			long mainLoopStartTimeStamp = System.currentTimeMillis();
			for (String questionnaireId : questionnaireModelIds) {
				long queryGetInterrogationIdsStartTimeStamp = System.currentTimeMillis();
				List<InterrogationId> ids = client.getInterrogationIds(questionnaireId);
				long queryGetInterrogationIdsEndTimeStamp = System.currentTimeMillis();
				long queryGetInterrogationIdsDeltaTimeStamp = queryGetInterrogationIdsEndTimeStamp - queryGetInterrogationIdsStartTimeStamp;
				log.info("(DEBUG) Main ListIds array size : {}", ids.size());
				log.info("====> GetInterrogationIds query duration : {} for questionnaireId {}", queryGetInterrogationIdsDeltaTimeStamp, questionnaireId);
				List<List<InterrogationId>> listIds = ListUtils.partition(ids, batchSize);
				int nbPartitions = listIds.size();
				log.info("(DEBUG) Main ListIds partition number : {}", nbPartitions);
				int indexPartition = 1;
				for (List<InterrogationId> listId : listIds) {
					log.info("=============== PROCESS BLOCK N°{} ==============", indexPartition);
					long partitionProcessStartTimeStamp = System.currentTimeMillis();

					long queryGetUEsLatestStateStartTimeStamp = System.currentTimeMillis();
					List<SurveyUnitUpdateLatest> suLatest = client.getUEsLatestState(questionnaireId, listId);
					long queryGetUEsLatestStateEndTimeStamp = System.currentTimeMillis();
					long queryGetUEsLatestStateDeltaTimeStamp = queryGetUEsLatestStateEndTimeStamp - queryGetUEsLatestStateStartTimeStamp;
					log.info("==> GetUEsLatestState query duration : {}", queryGetUEsLatestStateDeltaTimeStamp);

					log.info("Number of documents retrieved from database : {}, partition {}/{}", suLatest.size(), indexPartition, nbPartitions);
					vtlBindings = new VtlBindings();
					unimodalProcess(suLatest);
					multimodalProcess();
					insertDatabase();
					indexPartition++;

					long partitionProcessEndTimeStamp = System.currentTimeMillis();
					long partitionProcessDeltaTimeStamp = partitionProcessEndTimeStamp - partitionProcessStartTimeStamp;
					log.info("==> partitionProcess duration : {}", partitionProcessDeltaTimeStamp);
				}
			}
			long mainLoopEndTimeStamp = System.currentTimeMillis();
			long mainLoopDeltaTimeStamp = mainLoopEndTimeStamp - mainLoopStartTimeStamp;
			log.info("====> MainLoop duration : {}", mainLoopDeltaTimeStamp);

			outputFileWriter();
			writeErrors();
			if (!database.isClosed()){database.close();}
		}catch (SQLException e){
			log.error(e.toString());
			throw new KraftwerkException(500,"SQL error");
		}
		SqlUtils.deleteDatabaseFile(databasePath);
	}


	//========= OPTIMISATIONS PERFS (START) ==========
	public void runMainV2(String campaignId, int batchSize, int workersNumbers, int workerId) throws KraftwerkException, IOException {
		log.info("(V2) Batch size of interrogations retrieved from Genesis: {}", batchSize);
		long preProcessStartTimeStamp = System.currentTimeMillis();
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
			long preProcessEndTimeStamp = System.currentTimeMillis();
			long preProcessDeltaTimeStamp = preProcessEndTimeStamp - preProcessStartTimeStamp;
			log.info("====> (V2) preProcess duration : {}", preProcessDeltaTimeStamp);

			long queryGetQuestionnaireModelIdsStartTimeStamp = System.currentTimeMillis();
			List<String> questionnaireModelIds = client.getQuestionnaireModelIds(campaignId);
			if (questionnaireModelIds.isEmpty()) {
				throw new KraftwerkException(204, null);
			}
			long queryGetQuestionnaireModelIdsEndTimeStamp = System.currentTimeMillis();
			long queryGetQuestionnaireModelIdsDeltaTimeStamp = queryGetQuestionnaireModelIdsEndTimeStamp - queryGetQuestionnaireModelIdsStartTimeStamp;
			log.info("(DEBUG) (V2) questionnaireModelIds array size : {}", questionnaireModelIds.size());
			log.info("====> (V2) getQuestionnaireModelIds duration : {}", queryGetQuestionnaireModelIdsDeltaTimeStamp);

			long mainLoopStartTimeStamp = System.currentTimeMillis();
			for (String questionnaireId : questionnaireModelIds) {
				//FIRST GET NUMBER OF ELEMENTS OF THE QUESTIONNAIRE
				long totalSize = client.countInterrogationIds(questionnaireId);
				//blockNb must always be at least equal to 1, even if "totalSize" < "batchSize"
				long blockNb = totalSize < batchSize ? 1 : totalSize / batchSize;
				log.info("====> (V2) BLOCKS NUMBER TO PROCESS for questionnaireId {} ============== : {}", questionnaireId, blockNb);

				long queryGetModesByQuestionnaireIdStartTimeStamp = System.currentTimeMillis();
				List<String> modes = client.getDistinctModesByQuestionnaire(questionnaireId);
				long queryGetModesByQuestionnaireIdEndTimeStamp = System.currentTimeMillis();
				long queryGetModesByQuestionnaireIdDeltaTimeStamp = queryGetModesByQuestionnaireIdEndTimeStamp - queryGetModesByQuestionnaireIdStartTimeStamp;
				log.info("====> (V2) getModes duration : {}", queryGetModesByQuestionnaireIdDeltaTimeStamp);


				for (int indexPartition = 0; indexPartition < blockNb; indexPartition++) {
					log.info("=============== (V2) PROCESS BLOCK N°{} ==============", indexPartition);
					long partitionProcessStartTimeStamp = System.currentTimeMillis();

					long queryGetPaginatedInterrogationIdsStartTimeStamp = System.currentTimeMillis();
					//USING PAGINATION INSTEAD
					List<InterrogationId> ids = client.getPaginatedInterrogationIds(questionnaireId, totalSize, workersNumbers, workerId,
																					batchSize, indexPartition);
					long queryGetPaginatedInterrogationIdsEndTimeStamp = System.currentTimeMillis();
					long queryGetPaginatedInterrogationIdsDeltaTimeStamp = queryGetPaginatedInterrogationIdsEndTimeStamp - queryGetPaginatedInterrogationIdsStartTimeStamp;
					log.info("(DEBUG) (V2) Main ListIds array size : {}", ids.size());
					log.info("====> (V2) GetPaginatedInterrogationIds query duration : {} for questionnaireId {}", queryGetPaginatedInterrogationIdsDeltaTimeStamp, questionnaireId);

					long queryGetUEsLatestStateStartTimeStamp = System.currentTimeMillis();
					List<SurveyUnitUpdateLatest> suLatest = client.getUEsLatestStateV2(questionnaireId, ids, modes);
					long queryGetUEsLatestStateEndTimeStamp = System.currentTimeMillis();
					long queryGetUEsLatestStateDeltaTimeStamp = queryGetUEsLatestStateEndTimeStamp - queryGetUEsLatestStateStartTimeStamp;
					log.info("==> (V2) GetUEsLatestStateV2 query duration : {}", queryGetUEsLatestStateDeltaTimeStamp);
					//Free RAM with unused List in the rest of the loop.
					ids = null;

					log.info("(V2) Number of documents retrieved from database : {}, partition {}/{}", suLatest.size(), indexPartition, blockNb);
					vtlBindings = new VtlBindings();

					unimodalProcess(suLatest);
					multimodalProcess();
					insertDatabase();

					long partitionProcessEndTimeStamp = System.currentTimeMillis();
					long partitionProcessDeltaTimeStamp = partitionProcessEndTimeStamp - partitionProcessStartTimeStamp;
					log.info("==> (V2) partitionProcess duration : {}", partitionProcessDeltaTimeStamp);
				}
			}
			long mainLoopEndTimeStamp = System.currentTimeMillis();
			long mainLoopDeltaTimeStamp = mainLoopEndTimeStamp - mainLoopStartTimeStamp;
			log.info("====> (V2) MainLoop duration : {}", mainLoopDeltaTimeStamp);

			outputFileWriter();
			writeErrors();
			if (!database.isClosed()){database.close();}
		}catch (SQLException e){
			log.error(e.toString());
			throw new KraftwerkException(500,"SQL error");
		}
		SqlUtils.deleteDatabaseFile(databasePath);
	}
	//========= OPTIMISATIONS PERFS (END) ==========


	private void unimodalProcess(List<SurveyUnitUpdateLatest> suLatest) throws KraftwerkException {
		BuildBindingsSequenceGenesis buildBindingsSequenceGenesis = new BuildBindingsSequenceGenesis(fileUtilsInterface);
		for (String dataMode : userInputs.getModeInputsMap().keySet()) {
			buildBindingsSequenceGenesis.buildVtlBindings(dataMode, vtlBindings, metadataModels, suLatest, specsDirectory);
			UnimodalSequence unimodal = new UnimodalSequence();
			unimodal.applyUnimodalSequence(userInputs, dataMode, vtlBindings, kraftwerkExecutionContext, metadataModels, fileUtilsInterface);
		}
	}

	/* Step 3 : multimodal VTL data processing */
	private void multimodalProcess() {
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

}
