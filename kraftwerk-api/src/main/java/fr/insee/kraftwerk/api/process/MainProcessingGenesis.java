package fr.insee.kraftwerk.api.process;

import fr.insee.bpm.exceptions.MetadataParserException;
import fr.insee.bpm.metadata.model.MetadataModel;
import fr.insee.kraftwerk.api.client.GenesisClient;
import fr.insee.kraftwerk.api.configuration.ConfigProperties;
import fr.insee.kraftwerk.core.data.model.SurveyUnitId;
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
	private Path inDirectory;
	/**
	 * Map by mode
	 */
	@Getter
	private Map<String, MetadataModel> metadataModels;

	private final GenesisClient client;

	public MainProcessingGenesis(ConfigProperties config, FileUtilsInterface fileUtilsInterface, boolean withDDI) {
		this.client = new GenesisClient(new RestTemplateBuilder(), config);
		this.fileUtilsInterface = fileUtilsInterface;
		this.withDDI = withDDI;
	}

	public MainProcessingGenesis(ConfigProperties config, FileUtilsInterface fileUtilsInterface) {
		this.client = new GenesisClient(new RestTemplateBuilder(), config);
		this.fileUtilsInterface = fileUtilsInterface;
		this.withDDI = true;
	}

	/** for tests*/
	public MainProcessingGenesis(GenesisClient client, FileUtilsInterface fileUtilsInterface) {
		this.client = client;
		this.fileUtilsInterface = fileUtilsInterface;
		this.withDDI = true;
	}

	public void init(String idCampaign) throws KraftwerkException {
		kraftwerkExecutionContext = new KraftwerkExecutionContext();
		log.info("Kraftwerk main service started for campaign: {} {}", idCampaign, withDDI ? "with DDI": "without DDI");
		this.controlInputSequenceGenesis = new ControlInputSequenceGenesis(client.getConfigProperties().getDefaultDirectory(), fileUtilsInterface);
		inDirectory = controlInputSequenceGenesis.getInDirectory(idCampaign);
		//First we check the modes present in database for the given questionnaire
		//We build userInputs for the given questionnaire
		userInputs = new UserInputsGenesis(controlInputSequenceGenesis.isHasConfigFile(), inDirectory, client.getModes(idCampaign), fileUtilsInterface);
		if (!userInputs.getModes().isEmpty()) {
            try {
                metadataModels = withDDI ? MetadataUtilsGenesis.getMetadata(userInputs.getModeInputsMap(), fileUtilsInterface): MetadataUtilsGenesis.getMetadataFromLunatic(userInputs.getModeInputsMap(), fileUtilsInterface);
			} catch (MetadataParserException e) {
                throw new KraftwerkException(500, e.getMessage());
            }
        } else {
            log.error("No source found for campaign {}", idCampaign);
		}
	}

	public void runMain(String idCampaign) throws KraftwerkException, IOException {
		// We limit the size of the batch to 1000 survey units at a time
		int batchSize = 1000;
		init(idCampaign);
		//Try with resources to close database when done
		try (Connection tryDatabase = SqlUtils.openConnection()) {
			this.database = tryDatabase.createStatement();
			List<String> questionnaireModelIds = client.getQuestionnaireModelIds(idCampaign);
			if (questionnaireModelIds.isEmpty()) {
				throw new KraftwerkException(204, null);
			}
			for (String questionnaireId : questionnaireModelIds) {
				List<SurveyUnitId> ids = client.getSurveyUnitIds(questionnaireId);
				List<List<SurveyUnitId>> listIds = ListUtils.partition(ids, batchSize);
				for (List<SurveyUnitId> listId : listIds) {
					List<SurveyUnitUpdateLatest> suLatest = client.getUEsLatestState(questionnaireId, listId);
					log.info("Number of documents retrieved from database : {}", suLatest.size());
					vtlBindings = new VtlBindings();
					unimodalProcess(suLatest);
					multimodalProcess();
					insertDatabase();
					outputFileWriter();
					writeErrors();
				}
			}
		}catch (SQLException e){
			log.error(e.toString());
			throw new KraftwerkException(500,"SQL error");
		}
	}

	private void unimodalProcess(List<SurveyUnitUpdateLatest> suLatest) throws KraftwerkException {
		BuildBindingsSequenceGenesis buildBindingsSequenceGenesis = new BuildBindingsSequenceGenesis(fileUtilsInterface);
		for (String dataMode : userInputs.getModeInputsMap().keySet()) {
			buildBindingsSequenceGenesis.buildVtlBindings(dataMode, vtlBindings, metadataModels, suLatest, inDirectory);
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
		writerSequence.writeOutputFiles(inDirectory, vtlBindings, userInputs.getModeInputsMap(), metadataModels, kraftwerkExecutionContext, database, fileUtilsInterface);
	}

	/* Step 6 : Write errors */
	private void writeErrors() {
		TextFileWriter.writeErrorsFile(inDirectory, kraftwerkExecutionContext, fileUtilsInterface);
	}

}
