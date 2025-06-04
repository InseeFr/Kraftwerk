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
					log.info("=============== PROCESS BLOCK N°{} ==============", indexPartition);
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


	//========= OPTIMISATIONS PERFS (START) ==========
	/**
	 * @author Adrien Marchal
	 */
	public void runMainV2(String campaignId, int batchSize, int workersNumbers, int workerId) throws KraftwerkException, IOException {
		log.info("(V2) Batch size of interrogations retrieved from Genesis: {}", batchSize);
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
				runMainV2OnQuestionnaire(batchSize, workersNumbers, workerId, questionnaireId);
			}
			outputFileWriterV2();
			writeErrors();
			if (!database.isClosed()){database.close();}
		}catch (SQLException e){
			log.error(e.toString());
			throw new KraftwerkException(500,"SQL error");
		}
		SqlUtils.deleteDatabaseFile(databasePath);
	}

	private void runMainV2OnQuestionnaire(
			int batchSize, int workersNumbers, int workerId, String questionnaireId
	) throws KraftwerkException {
		//FIRST GET NUMBER OF ELEMENTS OF THE QUESTIONNAIRE
		long totalSize = client.countInterrogationIds(questionnaireId);
		//blockNb must always be at least equal to 1, even if "totalSize" < "batchSize"
		long blockNb = totalSize % batchSize == 0 ? totalSize / batchSize : totalSize / batchSize + 1;
		log.info("====> (V2) Number of questionnaireIds to process : {} ({} blocks)", totalSize, blockNb);
		long workerBlocksNb = workerId == workersNumbers ? blockNb / workersNumbers : blockNb / workersNumbers + 1;
		log.info("====> (V2) NUMBER OF BLOCKS TO PROCESS for questionnaireId {} on workerId {} : {}", questionnaireId, workerId, workerBlocksNb);

		List<String> modes = client.getDistinctModesByQuestionnaire(questionnaireId);

		for (int indexPartition = 0; indexPartition < workerBlocksNb; indexPartition++) {
			runMainV2OnPartition(batchSize, workerId, questionnaireId, workerBlocksNb, indexPartition, totalSize, modes);
		}
	}

	private void runMainV2OnPartition(
			int batchSize, int workerId, String questionnaireId, long workerBlocksNb, int indexPartition,
			long totalSize, List<String> modes
	) throws KraftwerkException {
		long absoluteBlockIndex = (workerId - 1) * workerBlocksNb + indexPartition;
		log.info("=============== (V2) PROCESS BLOCK N°{} (on workerId {}) ==============", absoluteBlockIndex + 1, workerId);

		//USING PAGINATION INSTEAD
		List<InterrogationId> ids = client.getPaginatedInterrogationIds(questionnaireId, totalSize, batchSize, absoluteBlockIndex);

		List<SurveyUnitUpdateLatest> suLatest = client.getUEsLatestStateV2(questionnaireId, ids, modes);
		//Free RAM with unused List in the rest of the loop.
		ids = null;

		log.info("(V2) Number of documents retrieved from database : {}, partition {}/{}", suLatest.size(), indexPartition + 1, workerBlocksNb);
		vtlBindings = new VtlBindings();

		unimodalProcess(suLatest);
		multimodalProcess();
		insertDatabase();
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

	//========= OPTIMISATIONS PERFS (START) ==========
	/**
	 * @author Adrien Marchal
	 */
	/* Step 5 : Write output files */
	private void outputFileWriterV2() throws KraftwerkException {
		WriterSequence writerSequence = new WriterSequence();
		writerSequence.writeOutputFilesV2(specsDirectory, vtlBindings, userInputs.getModeInputsMap(), metadataModels, kraftwerkExecutionContext, database, fileUtilsInterface);
	}
	//========= OPTIMISATIONS PERFS (END) ==========

	/* Step 6 : Write errors */
	private void writeErrors() {
		TextFileWriter.writeErrorsFile(specsDirectory, kraftwerkExecutionContext, fileUtilsInterface);
	}

}
