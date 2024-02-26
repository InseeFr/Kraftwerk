package fr.insee.kraftwerk.core.dataprocessing;

import fr.insee.kraftwerk.core.KraftwerkError;
import fr.insee.kraftwerk.core.TestConstants;
import fr.insee.kraftwerk.core.metadata.MetadataModel;
import fr.insee.kraftwerk.core.metadata.Sequence;
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
import java.util.List;

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
        // VTL Test files generation
        String vtlScript1 = "/* Instruction TCM_ACT_ANTE */";
        Path path1 = Files.createDirectories(Path.of(TestConstants.UNIT_TESTS_DIRECTORY).resolve("vtl").resolve("tcm")).resolve("TCM_ACT_ANTE.vtl");
        if(!Files.exists(path1)) Files.createFile(path1);
        Files.write(path1,vtlScript1.getBytes());

        String vtlScript2 = "/* Instruction TCM_ACT_BIS */";
        Path path2 = Files.createDirectories(Path.of(TestConstants.UNIT_TESTS_DIRECTORY).resolve("vtl").resolve("tcm")).resolve("TCM_ACT_BIS.vtl");
        if(!Files.exists(path2)) Files.createFile(path2);
        Files.write(path2,vtlScript2.getBytes());

        String vtlScript3 = "/* Instruction TCM_THL_DET */";
        Path path3 = Files.createDirectories(Path.of(TestConstants.UNIT_TESTS_DIRECTORY).resolve("vtl").resolve("tcm")).resolve("TCM_THL_DET.vtl");
        if(!Files.exists(path3)) Files.createFile(path3);
        Files.write(path3,vtlScript3.getBytes());

        String vtlScript4 = "/* Instruction TCM_THL_SIMPLE */";
        Path path4 = Files.createDirectories(Path.of(TestConstants.UNIT_TESTS_DIRECTORY).resolve("vtl").resolve("tcm")).resolve("TCM_THL_SIMPLE.vtl");
        if(!Files.exists(path4)) Files.createFile(path4);
        Files.write(path4,vtlScript4.getBytes());

        vtlBindings.put("TEST", unimodalDataset);
    }

    //When + Then
    @Test
    @DisplayName("We should generate corresponding script depending on sequences presence")
    void check_standard_vtl_execution(){
        //GIVEN
        MetadataModel metadataModel = new MetadataModel();
        metadataModel.getSequences().add(new Sequence("TCM_ACT_ANT"));
        metadataModel.getSequences().add(new Sequence("TCM_ACTI_BIS"));
        metadataModel.getSequences().add(new Sequence("TCM_THLHAB"));

        // Errors list
        List<KraftwerkError> errors = new ArrayList<>();
        StringBuilder expectedScriptBuilder = new StringBuilder();
        expectedScriptBuilder.append("/* Instruction TCM_ACT_ANTE */");
        expectedScriptBuilder.append(System.lineSeparator());
        expectedScriptBuilder.append("/* Instruction TCM_ACT_BIS */");
        expectedScriptBuilder.append(System.lineSeparator());
        expectedScriptBuilder.append("/* Instruction TCM_THL_DET */");
        expectedScriptBuilder.append(System.lineSeparator());
        expectedScriptBuilder.append("/* Instruction TCM_THL_SIMPLE */");
        expectedScriptBuilder.append(System.lineSeparator());
        String expectedScript = expectedScriptBuilder.toString();

        //WHEN
        TCMSequencesProcessing processing = new TCMSequencesProcessing(vtlBindings, metadataModel, Path.of(TestConstants.UNIT_TESTS_DIRECTORY).resolve("vtl").toString());
        String scriptString = processing.applyAutomatedVtlInstructions("TEST", errors);

        //THEN
        Assertions.assertThat(scriptString).isEqualToIgnoringNewLines(expectedScript);
    }

    @AfterAll
    static void clean() throws IOException {
        Path path1 = Path.of(TestConstants.UNIT_TESTS_DIRECTORY).resolve("vtl").resolve("tcm").resolve("TCM_ACT_ANTE.vtl");
        Files.deleteIfExists(path1);
        Path path2 = Path.of(TestConstants.UNIT_TESTS_DIRECTORY).resolve("vtl").resolve("tcm").resolve("TCM_ACT_BIS.vtl");
        Files.deleteIfExists(path2);
        Path path3 = Path.of(TestConstants.UNIT_TESTS_DIRECTORY).resolve("vtl").resolve("tcm").resolve("TCM_THL_DET.vtl");
        Files.deleteIfExists(path3);
        Path path4= Path.of(TestConstants.UNIT_TESTS_DIRECTORY).resolve("vtl").resolve("tcm").resolve("TCM_THL_SIMPLE.vtl");
        Files.deleteIfExists(path4);
    }
}
