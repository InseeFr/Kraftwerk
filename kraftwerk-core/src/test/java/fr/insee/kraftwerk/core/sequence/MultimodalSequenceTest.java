package fr.insee.kraftwerk.core.sequence;

import fr.insee.bpm.metadata.model.MetadataModel;
import fr.insee.kraftwerk.core.Constants;
import fr.insee.kraftwerk.core.inputs.UserInputs;
import fr.insee.kraftwerk.core.utils.files.FileSystemImpl;
import fr.insee.kraftwerk.core.utils.files.FileUtilsInterface;
import fr.insee.kraftwerk.core.utils.log.KraftwerkExecutionContext;
import fr.insee.kraftwerk.core.vtl.VtlBindings;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;

class MultimodalSequenceTest {

    private MultimodalSequence multimodalSequence;
    private VtlBindings vtlBindings;
    private KraftwerkExecutionContext kraftwerkExecutionContext;
    private FileUtilsInterface fileUtilsInterface;
    private UserInputs userInputs;
    private Map<String, MetadataModel> metadataModels;

    @BeforeEach
    void setUp() {
        multimodalSequence = new MultimodalSequence();
        vtlBindings = new VtlBindings();
        kraftwerkExecutionContext = new KraftwerkExecutionContext();
        fileUtilsInterface = new FileSystemImpl("defaultDir") {
            @Override
            public Path getTempVtlFilePath(UserInputs inputs, String processName, String datasetName) {
                return Path.of("target/tmp/" + processName + "_" + datasetName + ".vtl"); // Utilisation d'un répertoire temporaire
            }
        };

        userInputs = new UserInputs(Path.of("inputDir"), fileUtilsInterface);
        userInputs.setVtlReconciliationFile(Path.of("reconciliation.vtl"));
        userInputs.setVtlTransformationsFile(Path.of("transformations.vtl"));
        userInputs.setVtlInformationLevelsFile(Path.of("infoLevels.vtl"));

        metadataModels = new HashMap<>();
        metadataModels.put("mockDataset", new MetadataModel());
    }

    @Test
    void testMultimodalProcessing() {
        // Vérifier que le dossier temporaire existe
        new File("target/tmp").mkdirs();

        multimodalSequence.multimodalProcessing(userInputs, vtlBindings, kraftwerkExecutionContext, metadataModels, fileUtilsInterface);

        // Vérifier que les fichiers VTL ont bien été créés
        assertTrue(new File("target/tmp/ReconciliationProcessing_" + Constants.MULTIMODE_DATASET_NAME + ".vtl").exists());
        assertTrue(new File("target/tmp/CleanUpProcessing_" + Constants.MULTIMODE_DATASET_NAME + ".vtl").exists());
        assertTrue(new File("target/tmp/MultimodeTransformations_" + Constants.MULTIMODE_DATASET_NAME + ".vtl").exists());
        assertTrue(new File("target/tmp/InformationLevelsProcessing_" + Constants.MULTIMODE_DATASET_NAME + ".vtl").exists());
    }
}
