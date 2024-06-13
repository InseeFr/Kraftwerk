package fr.insee.kraftwerk.api.process;

import fr.insee.kraftwerk.api.client.GenesisClient;
import fr.insee.kraftwerk.api.configuration.ConfigProperties;
import fr.insee.kraftwerk.core.KraftwerkError;
import fr.insee.kraftwerk.core.data.model.SurveyUnitId;
import fr.insee.kraftwerk.core.data.model.SurveyUnitUpdateLatest;
import fr.insee.kraftwerk.core.exceptions.KraftwerkException;
import fr.insee.kraftwerk.core.inputs.UserInputsGenesis;
import fr.insee.kraftwerk.core.metadata.MetadataModel;
import fr.insee.kraftwerk.core.metadata.MetadataUtilsGenesis;
import fr.insee.kraftwerk.core.sequence.BuildBindingsSequenceGenesis;
import fr.insee.kraftwerk.core.sequence.ControlInputSequenceGenesis;
import fr.insee.kraftwerk.core.sequence.MultimodalSequence;
import fr.insee.kraftwerk.core.sequence.UnimodalSequence;
import fr.insee.kraftwerk.core.sequence.WriterSequence;
import fr.insee.kraftwerk.core.utils.SqlUtils;
import fr.insee.kraftwerk.core.utils.TextFileWriter;
import fr.insee.kraftwerk.core.vtl.VtlBindings;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.collections4.ListUtils;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Path;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Log4j2
@Component
public class MainProcessingGenesis {

	@Setter
	private ControlInputSequenceGenesis controlInputSequenceGenesis;
	@Getter
	private VtlBindings vtlBindings = new VtlBindings();
	private final List<KraftwerkError> errors = new ArrayList<>();
	@Getter
	private UserInputsGenesis userInputs;
	private LocalDateTime executionDateTime;
	private Statement database;

	/* SPECIFIC VARIABLES */
	@Getter
	private Path inDirectory;
	/**
	 * Map by mode
	 */
	@Getter
	private Map<String, MetadataModel> metadataModels;

	private final GenesisClient client;

	public MainProcessingGenesis(ConfigProperties config) {
		this.client = new GenesisClient(new RestTemplateBuilder(), config);
	}

	public void init(String idCampaign) throws KraftwerkException, IOException {
		log.info("Kraftwerk main service started for campaign: " + idCampaign);
		this.executionDateTime = LocalDateTime.now();
		inDirectory = controlInputSequenceGenesis.getInDirectory(idCampaign);
		//First we check the modes present in database for the given questionnaire
		//We build userInputs for the given questionnaire
		userInputs = new UserInputsGenesis(controlInputSequenceGenesis.isHasConfigFile(), inDirectory, client.getModes(idCampaign));
		if (!userInputs.getModes().isEmpty()) {
			metadataModels = MetadataUtilsGenesis.getMetadata(userInputs.getModeInputsMap());
		} else {
			log.error("No source found for campaign " + idCampaign);
		}
	}

	public void runMain(String idCampaign) throws KraftwerkException, IOException {
		// We limit the size of the batch to 1000 survey units at a time
		int batchSize = 1000;
		init(idCampaign);
		//Try with ressources to close database when done
		try (Statement trydatabase = SqlUtils.openConnection().createStatement()) {
			this.database = trydatabase;
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
		BuildBindingsSequenceGenesis buildBindingsSequenceGenesis = new BuildBindingsSequenceGenesis();
		for (String dataMode : userInputs.getModeInputsMap().keySet()) {
			buildBindingsSequenceGenesis.buildVtlBindings(dataMode, vtlBindings, metadataModels, suLatest, inDirectory, database);
			UnimodalSequence unimodal = new UnimodalSequence();
			unimodal.applyUnimodalSequence(userInputs, dataMode, vtlBindings, errors, metadataModels);
		}
	}

	/* Step 3 : multimodal VTL data processing */
	private void multimodalProcess() {
		MultimodalSequence multimodalSequence = new MultimodalSequence();
		multimodalSequence.multimodalProcessing(userInputs, vtlBindings, errors, metadataModels);
	}

	/* Step 4 : Write output files */
	private void outputFileWriter() throws KraftwerkException {
		WriterSequence writerSequence = new WriterSequence();
		writerSequence.writeOutputFiles(inDirectory, executionDateTime, vtlBindings, userInputs.getModeInputsMap(), metadataModels, errors, database);
	}

	/* Step 5 : Write errors */
	private void writeErrors() {
		TextFileWriter.writeErrorsFile(inDirectory, executionDateTime, errors);
	}

}
