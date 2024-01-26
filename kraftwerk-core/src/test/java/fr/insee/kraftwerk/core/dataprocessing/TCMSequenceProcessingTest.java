package fr.insee.kraftwerk.core.dataprocessing;

import fr.insee.kraftwerk.core.KraftwerkError;
import fr.insee.kraftwerk.core.TestConstants;
import fr.insee.kraftwerk.core.metadata.MetadataModel;
import fr.insee.kraftwerk.core.metadata.Variable;
import fr.insee.kraftwerk.core.metadata.VariableType;
import fr.insee.kraftwerk.core.vtl.VtlBindings;
import fr.insee.vtl.model.Dataset;
import fr.insee.vtl.model.InMemoryDataset;
import fr.insee.vtl.model.Structured;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TCMSequenceProcessingTest {

    //Given
    static Dataset unimodalDataset = new InMemoryDataset(
            List.of(
                    List.of("ID1", "TEST1", "PRENOM1"),
                    List.of("ID2", "TEST2", "PRENOM2"),
                    List.of("ID3", "", "PRENOM3")
            ),
            List.of(
                    new Structured.Component("ID", String.class, Dataset.Role.IDENTIFIER),
                    new Structured.Component("TCM_THLHAB", String.class, Dataset.Role.MEASURE),
                    new Structured.Component("PRENOM", String.class, Dataset.Role.MEASURE)
            )
    );
    static VtlBindings vtlBindings = new VtlBindings();
    Dataset outDataset;
    @BeforeAll
    static void init() throws IOException {
        // VTL Test file
        String vtlScript = "TEST := TEST[calc TCM_THL_DET := \"TESTTCM1\"];";

        Path path = Files.createDirectories(Path.of(TestConstants.UNIT_TESTS_DIRECTORY).resolve("vtl").resolve("tcm")).resolve("TCM_THL_DET.vtl");
        if(!Files.exists(path)) Files.createFile(path);
        Files.write(path,vtlScript.getBytes());

        vtlBindings.put("TEST", unimodalDataset);
    }

    //When + Then
    @Test
    @DisplayName("We should have TCM Module variable in binding")
    void check_standard_vtl_execution(){
        //Metadata variables object
        Map<String, MetadataModel> metadatas = new LinkedHashMap<>();
        MetadataModel metadataModel = new MetadataModel();

        metadataModel.getVariables().putVariable(new Variable("TCM_THLHAB", metadataModel.getRootGroup(), VariableType.STRING));
        metadataModel.getVariables().putVariable(new Variable("PRENOM", metadataModel.getRootGroup(), VariableType.STRING));

        metadatas.put("TEST",metadataModel);

        // Errors list
        List<KraftwerkError> errors = new ArrayList<>();

        TCMSequencesProcessing processing = new TCMSequencesProcessing(vtlBindings, metadataModel, Path.of(TestConstants.UNIT_TESTS_DIRECTORY).resolve("vtl").toString());
        processing.applyAutomatedVtlInstructions("TEST", errors);
        outDataset = vtlBindings.getDataset("TEST");

        Assertions.assertThat(errors).isEmpty();
        assertTrue(outDataset.getDataStructure().containsKey("TCM_THL_DET"));
        assertEquals("TESTTCM1", outDataset.getDataPoints().getFirst().get("TCM_THL_DET"));
    }

    @AfterAll
    static void clean() throws IOException {
        Path path = Path.of(TestConstants.UNIT_TESTS_DIRECTORY).resolve("vtl").resolve("tcm").resolve("TCM_THL_DET.vtl");
        Files.deleteIfExists(path);
    }
}
