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
    protected Map<String, MetadataModel> metadataModels;
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
    }

    public void init(String dataSelectionIdentifier) throws KraftwerkException {
        specsDirectory= Paths.get(config.getDefaultDirectory(),"specs", dataSelectionIdentifier);
        //First we check the modes present in database for the given questionnaire
        //We build userInputs for the given questionnaire
        userInputs = new UserInputsGenesis(specsDirectory,
                client.getModes(dataSelectionIdentifier), fileUtilsInterface, kraftwerkExecutionContext.isWithDDI());
        if (!userInputs.getModes().isEmpty()) {
            try {
                metadataModels = kraftwerkExecutionContext.isWithDDI() ? MetadataUtilsGenesis.getMetadata(userInputs.getModeInputsMap(), fileUtilsInterface): MetadataUtilsGenesis.getMetadataFromLunatic(userInputs.getModeInputsMap(), fileUtilsInterface);
            } catch (MetadataParserException e) {
                throw new KraftwerkException(500, e.getMessage());
            }
        } else {
            log.error("No specs found in folder {}", dataSelectionIdentifier);
        }
    }

    protected void processDataByBatch(String questionnaireModelId, int batchSize) throws KraftwerkException {
        List<InterrogationId> ids = client.getInterrogationIds(questionnaireModelId);
        List<List<InterrogationId>> listIds = ListUtils.partition(ids, batchSize);
        int nbPartitions = listIds.size();
        int indexPartition = 1;
        for (List<InterrogationId> listId : listIds) {
            List<SurveyUnitUpdateLatest> suLatest = client.getUEsLatestState(questionnaireModelId, listId);
            log.info("Number of documents retrieved from database : {}, partition {}/{}", suLatest.size(), indexPartition, nbPartitions);
            vtlBindings = new VtlBindings();
            unimodalProcess(suLatest);
            multimodalProcess();
            insertDatabase();
            indexPartition++;
        }
    }

    protected void unimodalProcess(List<SurveyUnitUpdateLatest> suLatest) throws KraftwerkException {
        BuildBindingsSequenceGenesis buildBindingsSequenceGenesis = new BuildBindingsSequenceGenesis(fileUtilsInterface);
        for (String dataMode : userInputs.getModeInputsMap().keySet()) {
            buildBindingsSequenceGenesis.buildVtlBindings(dataMode, vtlBindings, metadataModels, suLatest, specsDirectory);
            UnimodalSequence unimodal = new UnimodalSequence();
            unimodal.applyUnimodalSequence(userInputs, dataMode, vtlBindings, kraftwerkExecutionContext, metadataModels, fileUtilsInterface);
        }
    }

    /* Step 3 : multimodal VTL data processing */
    protected void multimodalProcess() throws KraftwerkException {
        MultimodalSequence multimodalSequence = new MultimodalSequence();
        multimodalSequence.multimodalProcessing(userInputs, vtlBindings, kraftwerkExecutionContext, metadataModels,
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
        writerSequence.writeOutputFiles(specsDirectory, vtlBindings, userInputs.getModeInputsMap(), metadataModels, kraftwerkExecutionContext, database, fileUtilsInterface);
    }

    /* Step 6 : Write errors */
    protected void writeErrors() {
        TextFileWriter.writeErrorsFile(specsDirectory, kraftwerkExecutionContext, fileUtilsInterface);
    }

}
