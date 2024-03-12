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

    static final String VTL_EXTENSION = ".vtl";
    static final String FORMAT_INSTRUCTION = "/* Instruction %s */";
    static final List<TCMModuleEnum> modules = new ArrayList<>();

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

    @BeforeAll
    static void init() throws IOException {
        modules.add(TCMModuleEnum.TCM_ACT_ANTE);
        modules.add(TCMModuleEnum.TCM_ACT_BIS);
        modules.add(TCMModuleEnum.TCM_THL_SIMPLE);

        // VTL Test files generation
        for (TCMModuleEnum module : modules){
            Path path = Files.createDirectories(Path.of(TestConstants.UNIT_TESTS_DIRECTORY).resolve("vtl").resolve("tcm")).resolve(module + VTL_EXTENSION);
            if(!Files.exists(path)) Files.createFile(path);
            Files.write(path,String.format(FORMAT_INSTRUCTION,module).getBytes());
        }

        vtlBindings.put("TEST", unimodalDataset);
    }

    //When + Then
    @Test
    @DisplayName("We should generate corresponding script depending on sequences presence")
    void check_standard_vtl_execution(){
        //GIVEN
        MetadataModel metadataModel = new MetadataModel();
        metadataModel.getSequences().add(new Sequence(TCMSequenceEnum.TCM_ACT_ANT.name()));
        metadataModel.getSequences().add(new Sequence(TCMSequenceEnum.TCM_ACTI_BIS.name()));
        metadataModel.getSequences().add(new Sequence(TCMSequenceEnum.TCM_THLHAB.name()));

        // Errors list
        List<KraftwerkError> errors = new ArrayList<>();
        StringBuilder expectedScriptBuilder = new StringBuilder();
        for (TCMModuleEnum module : modules){
            expectedScriptBuilder.append(String.format(FORMAT_INSTRUCTION,module));
            expectedScriptBuilder.append(System.lineSeparator());
        }
        String expectedScript = expectedScriptBuilder.toString();

        //WHEN
        TCMSequencesProcessing processing = new TCMSequencesProcessing(vtlBindings, metadataModel, Path.of(TestConstants.UNIT_TESTS_DIRECTORY).resolve("vtl").toString());
        String scriptString = processing.applyAutomatedVtlInstructions("TEST", errors);

        //THEN
        Assertions.assertThat(scriptString).isEqualToIgnoringNewLines(expectedScript);
    }

    @AfterAll
    static void clean() throws IOException {
        for (TCMModuleEnum module : modules){
            Files.deleteIfExists(Path.of(TestConstants.UNIT_TESTS_DIRECTORY).resolve("vtl").resolve("tcm").resolve(module + VTL_EXTENSION));
        }
    }
}
