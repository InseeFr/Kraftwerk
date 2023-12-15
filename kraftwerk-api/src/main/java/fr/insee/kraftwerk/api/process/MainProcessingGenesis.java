package fr.insee.kraftwerk.api.process;

import fr.insee.kraftwerk.api.client.GenesisClient;
import fr.insee.kraftwerk.api.configuration.ConfigProperties;
import fr.insee.kraftwerk.core.data.model.SurveyUnitId;
import fr.insee.kraftwerk.core.data.model.SurveyUnitUpdateLatest;
import fr.insee.kraftwerk.core.KraftwerkError;
import fr.insee.kraftwerk.core.exceptions.KraftwerkException;
import fr.insee.kraftwerk.core.exceptions.NullException;
import fr.insee.kraftwerk.core.inputs.UserInputsGenesis;
import fr.insee.kraftwerk.core.metadata.MetadataUtilsGenesis;
import fr.insee.kraftwerk.core.metadata.VariablesMap;
import fr.insee.kraftwerk.core.sequence.*;
import fr.insee.kraftwerk.core.utils.TextFileWriter;
import fr.insee.kraftwerk.core.vtl.VtlBindings;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.collections4.ListUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Path;
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
	private List<KraftwerkError> errors = new ArrayList<>();
	@Getter
	private UserInputsGenesis userInputs;

	/* SPECIFIC VARIABLES */
	@Getter
	private Path inDirectory;
	@Getter
	private Map<String, VariablesMap> metadataVariables;

	private GenesisClient client;

	public MainProcessingGenesis(ConfigProperties config) {
		this.client = new GenesisClient(new RestTemplateBuilder(), config);
	}

	public void init(String idQuestionnaire) throws KraftwerkException, IOException {
		log.info("Kraftwerk main service started for questionnaire: " + idQuestionnaire);
		inDirectory = controlInputSequenceGenesis.getInDirectory(idQuestionnaire);
		//First we check the modes present in database for the given questionnaire
		//We build userInputs for the given questionnaire
		userInputs = new UserInputsGenesis(controlInputSequenceGenesis.isHasConfigFile(), inDirectory, client.getModes(idQuestionnaire));
		if (!userInputs.getModes().isEmpty()) {
			metadataVariables = MetadataUtilsGenesis.getMetadata(userInputs.getModeInputsMap());
		} else {
			log.error("No source found for questionnaire " + idQuestionnaire);
		}
	}

	public void runMain(String idQuestionnaire) throws KraftwerkException, IOException {
		// We limit the size of the batch to 1000 survey units at a time
		int batchSize = 1000;
		init(idQuestionnaire);
		List <SurveyUnitId> ids = client.getSurveyUnitIds(idQuestionnaire);
		List <List<SurveyUnitId>> listIds = ListUtils.partition(ids, batchSize);
		for (List<SurveyUnitId> listId : listIds) {
			List<SurveyUnitUpdateLatest> suLatest = client.getUEsLatestState(idQuestionnaire, listId);
			log.info("Number of documents retrieved from database : {}", suLatest.size());
			vtlBindings = new VtlBindings();
			unimodalProcess(suLatest);
			multimodalProcess();
			outputFileWriter();
			writeErrors();
		}
	}

	private void unimodalProcess(List<SurveyUnitUpdateLatest> suLatest) throws NullException {
		BuildBindingsSequenceGenesis buildBindingsSequenceGenesis = new BuildBindingsSequenceGenesis();
		for (String dataMode : userInputs.getModeInputsMap().keySet()) {
			buildBindingsSequenceGenesis.buildVtlBindings(dataMode, vtlBindings, metadataVariables, suLatest, inDirectory);
			UnimodalSequence unimodal = new UnimodalSequence();
			unimodal.applyUnimodalSequence(userInputs, dataMode, vtlBindings, errors, metadataVariables);
		}
	}

	/* Step 3 : multimodal VTL data processing */
	private void multimodalProcess() {
		MultimodalSequence multimodalSequence = new MultimodalSequence();
		multimodalSequence.multimodalProcessing(userInputs, vtlBindings, errors, metadataVariables);
	}

	/* Step 4 : Write output files */
	private void outputFileWriter() throws KraftwerkException {
		WriterSequence writerSequence = new WriterSequence();
		writerSequence.writeOutputFiles(inDirectory, vtlBindings, userInputs.getModeInputsMap(), metadataVariables, errors);
	}

	/* Step 5 : Write errors */
	private void writeErrors() {
		TextFileWriter.writeErrorsFile(inDirectory, errors);
	}

}
