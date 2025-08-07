package fr.insee.kraftwerk.api.process;

import fr.insee.bpm.exceptions.MetadataParserException;
import fr.insee.bpm.metadata.model.MetadataModel;
import fr.insee.kraftwerk.api.client.GenesisClient;
import fr.insee.kraftwerk.api.configuration.ConfigProperties;
import fr.insee.kraftwerk.core.data.model.InterrogationId;
import fr.insee.kraftwerk.core.data.model.Mode;
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
import fr.insee.kraftwerk.core.utils.TextFileWriter;
import fr.insee.kraftwerk.core.utils.files.FileUtilsInterface;
import fr.insee.kraftwerk.core.vtl.VtlBindings;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.collections4.ListUtils;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Statement;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Log4j2
public abstract class AbstractMainProcessingGenesis {

    @Setter
    protected ControlInputSequenceGenesis controlInputSequenceGenesis;
    @Getter
    protected VtlBindings vtlBindings = new VtlBindings();
    @Getter
    protected UserInputsGenesis userInputs;
    protected final FileUtilsInterface fileUtilsInterface;
    protected Statement database;
    protected final KraftwerkExecutionContext kraftwerkExecutionContext;
    /* SPECIFIC VARIABLES */
    @Getter
    protected Path specsDirectory;
    /**
     * Map by mode
     */
    @Getter
    protected Map<String, MetadataModel> metadataModelsByMode;
    protected final GenesisClient client;
    protected final ConfigProperties config;

    protected AbstractMainProcessingGenesis(
            ConfigProperties config,
            GenesisClient genesisClient,
            FileUtilsInterface fileUtilsInterface,
            KraftwerkExecutionContext kraftwerkExecutionContext
    ) {
        this.config = config;
        this.client = genesisClient;
        this.fileUtilsInterface = fileUtilsInterface;
        this.kraftwerkExecutionContext = kraftwerkExecutionContext;
        this.metadataModelsByMode = new HashMap<>();
    }

    public void init(String dataSelectionIdentifier, List<Mode> modes) throws KraftwerkException {
        specsDirectory= Paths.get(config.getDefaultDirectory(),"specs", dataSelectionIdentifier);
        //First we check the modes present in database for the given questionnaire
        //We build userInputs for the given questionnaire
        userInputs = new UserInputsGenesis(specsDirectory,
                modes, fileUtilsInterface, kraftwerkExecutionContext.isWithDDI());
        if (!userInputs.getModes().isEmpty()) {
            loadMetadatasAndStoreIfNotExists(dataSelectionIdentifier, userInputs.getModes());
        } else {
            throw new KraftwerkException(404, String.format("No modes found in genesis for %s", dataSelectionIdentifier));
        }
    }

    private void loadMetadatasAndStoreIfNotExists(String questionnaireId, List<String> modes) throws KraftwerkException {
        try {
            for (String modeString : modes) {
                loadMetadatasFromDatabase(questionnaireId.toUpperCase(), modeString);
            }
        }catch (KraftwerkException e) {
            //Parse if genesis error
            log.info("Got error {} during get metadatas call : {}\n trying to parse directly from file...",
                    e.getStatus(),
                    e.getMessage()
            );
            loadMetadataFromFile(questionnaireId);
        }
    }

    private void loadMetadatasFromDatabase(String questionnaireId, String modeString) throws KraftwerkException {
        try{
            Mode mode = Mode.getEnumFromModeName(modeString);
            MetadataModel metadataModel = client.getMetadataByQuestionnaireIdAndMode(questionnaireId, mode);
            metadataModelsByMode.put(modeString, metadataModel);
        }catch (IllegalStateException e){
            log.warn("Incorrect mode detected in Genesis for questionnaire {} : {}",
                    questionnaireId,
                    modeString
                    );
        }
    }

    private void loadMetadataFromFile(String questionnaireId) throws KraftwerkException {
        try {
            metadataModelsByMode = kraftwerkExecutionContext.isWithDDI() ? MetadataUtilsGenesis.getMetadata(userInputs.getModeInputsMap(), fileUtilsInterface) : MetadataUtilsGenesis.getMetadataFromLunatic(userInputs.getModeInputsMap(), fileUtilsInterface);
        } catch (MetadataParserException mpe) {
            throw new KraftwerkException(500, mpe.getMessage());
        }
        //Update Genesis metadatas
        for (Map.Entry<String, MetadataModel> entry : metadataModelsByMode.entrySet()) {
            client.saveMetadata(questionnaireId.toUpperCase(), Mode.getEnumFromModeName(entry.getKey()), entry.getValue());
        }
    }

    protected void processDataByBatch(String questionnaireModelId, int batchSize, Mode dataMode) throws KraftwerkException {
        List<InterrogationId> ids = client.getInterrogationIds(questionnaireModelId);
        List<List<InterrogationId>> listIds = ListUtils.partition(ids, batchSize);
        int nbPartitions = listIds.size();
        int indexPartition = 1;
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
            indexPartition++;
        }
    }

    protected void unimodalProcess(List<SurveyUnitUpdateLatest> suLatest) throws KraftwerkException {
        BuildBindingsSequenceGenesis buildBindingsSequenceGenesis = new BuildBindingsSequenceGenesis(fileUtilsInterface);
        for (String dataMode : userInputs.getModeInputsMap().keySet()) {
            buildBindingsSequenceGenesis.buildVtlBindings(dataMode, vtlBindings, metadataModelsByMode, suLatest, specsDirectory);
            UnimodalSequence unimodal = new UnimodalSequence();
            unimodal.applyUnimodalSequence(userInputs, dataMode, vtlBindings, kraftwerkExecutionContext, metadataModelsByMode, fileUtilsInterface);
        }
    }

    /* Step 3 : multimodal VTL data processing */
    protected void multimodalProcess() throws KraftwerkException {
        MultimodalSequence multimodalSequence = new MultimodalSequence();
        multimodalSequence.multimodalProcessing(userInputs, vtlBindings, kraftwerkExecutionContext, metadataModelsByMode,
                fileUtilsInterface);
    }

    /* Step 4 : Insert into SQL database */
    protected void insertDatabase(){
        InsertDatabaseSequence insertDatabaseSequence = new InsertDatabaseSequence();
        insertDatabaseSequence.insertDatabaseProcessing(vtlBindings, database);
    }

    /* Step 5 : Write output files */
    protected void outputFileWriter() throws KraftwerkException {
        WriterSequence writerSequence = new WriterSequence();
        writerSequence.writeOutputFiles(specsDirectory, vtlBindings, userInputs.getModeInputsMap(), metadataModelsByMode, kraftwerkExecutionContext, database, fileUtilsInterface);
    }

    /* Step 6 : Write errors */
    protected void writeErrors() {
        TextFileWriter.writeErrorsFile(specsDirectory, kraftwerkExecutionContext, fileUtilsInterface);
    }

}
