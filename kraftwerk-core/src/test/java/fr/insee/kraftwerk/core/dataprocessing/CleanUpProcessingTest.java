package fr.insee.kraftwerk.core.dataprocessing;

import fr.insee.kraftwerk.core.Constants;
import fr.insee.kraftwerk.core.KraftwerkError;
import fr.insee.kraftwerk.core.metadata.*;
import fr.insee.kraftwerk.core.vtl.VtlBindings;
import fr.insee.vtl.model.Dataset;
import fr.insee.vtl.model.Dataset.Role;
import fr.insee.vtl.model.InMemoryDataset;
import fr.insee.vtl.model.Structured;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class CleanUpProcessingTest {

    Dataset cawiDataset = new InMemoryDataset(
            List.of(
                    List.of("T01", "foo", "2"),
                    List.of("T03", "foo", "2"),
                    List.of("T05", "foo", "1")
            ),
            List.of(
                    new Structured.Component("ID", String.class, Role.IDENTIFIER),
                    new Structured.Component("FOO", String.class, Role.IDENTIFIER),
                    new Structured.Component("GENDER", String.class, Role.IDENTIFIER)
            )
    );

    Dataset papiDataset = new InMemoryDataset(
            List.of(
                    List.of("T02", "foo", "1", "1", "0"),
                    List.of("T04", "foo", "2", "0", "1")
            ),
            List.of(
                    new Structured.Component("ID", String.class, Role.IDENTIFIER),
                    new Structured.Component("FOO", String.class, Role.IDENTIFIER),
                    new Structured.Component("GENDER", String.class, Role.IDENTIFIER),
                    new Structured.Component("GENDER_1", String.class, Role.IDENTIFIER),
                    new Structured.Component("GENDER_2", String.class, Role.IDENTIFIER)
            )
    );

    Dataset multimodeDataset = new InMemoryDataset(
            List.of(
                    List.of("T01", "foo", "2", "", "", "CAWI"),
                    List.of("T03", "foo", "2", "", "", "CAWI"),
                    List.of("T05", "foo", "1", "", "", "CAWI"),
                    List.of("T02", "foo", "1", "1", "0", "PAPI"),
                    List.of("T04", "foo", "2", "0", "1", "PAPI")
            ),
            List.of(
                    new Structured.Component("ID", String.class, Role.IDENTIFIER),
                    new Structured.Component("FOO", String.class, Role.IDENTIFIER),
                    new Structured.Component("GENDER", String.class, Role.IDENTIFIER),
                    new Structured.Component("GENDER_1", String.class, Role.IDENTIFIER),
                    new Structured.Component("GENDER_2", String.class, Role.IDENTIFIER),
                    new Structured.Component(Constants.MODE_VARIABLE_NAME, String.class, Role.IDENTIFIER)
            )
    );

    @Test
    void applyCleanUp() {
        // Metadata variables object
        Map<String, MetadataModel> metadatas = new LinkedHashMap<>();
        // Errors list
        List<KraftwerkError> errors = new ArrayList<>();
        //
        MetadataModel cawiMetadata = new MetadataModel();
        cawiMetadata.getVariables().putVariable(new Variable("FOO", cawiMetadata.getRootGroup(), VariableType.STRING));
        UcqVariable cawiUcq = new UcqVariable("GENDER", cawiMetadata.getRootGroup(), VariableType.STRING);
        cawiUcq.addModality("1", "Male");
        cawiUcq.addModality("2", "Female");
        cawiMetadata.getVariables().putVariable(cawiUcq);
        //
        MetadataModel papiMetadata = new MetadataModel();
        papiMetadata.getVariables().putVariable(new Variable("FOO", papiMetadata.getRootGroup(), VariableType.STRING));
        UcqVariable papiUcq = new UcqVariable("GENDER", papiMetadata.getRootGroup(), VariableType.STRING);
        papiUcq.addModality("1", "Male");
        papiUcq.addModality("2", "Female");
        papiMetadata.getVariables().putVariable(papiUcq);
        papiMetadata.getVariables().putVariable(new PaperUcq("GENDER_1", papiUcq, "1"));
        papiMetadata.getVariables().putVariable(new PaperUcq("GENDER_2", papiUcq, "2"));
        //
        metadatas.put("CAWI", cawiMetadata);
        metadatas.put("PAPI", papiMetadata);

        // Datasets
        VtlBindings vtlBindings = new VtlBindings();
        vtlBindings.put("CAWI", cawiDataset);
        vtlBindings.put("PAPI", papiDataset);
        vtlBindings.put("MULTIMODE", multimodeDataset);

        // Apply clean up
        CleanUpProcessing cleanUpProcessing = new CleanUpProcessing(vtlBindings, metadatas);
        cleanUpProcessing.applyVtlTransformations("MULTIMODE", null,errors);

        // Are paper indicator variables removed in VTL multimode dataset ?
        assertFalse(vtlBindings.getDataset("MULTIMODE").getDataStructure().containsKey("GENDER_1"));
        assertFalse(vtlBindings.getDataset("MULTIMODE").getDataStructure().containsKey("GENDER_2"));
        // Are these also removed in VariablesMap object ?
        assertFalse(papiMetadata.getVariables().hasVariable("GENDER_1"));
        assertFalse(papiMetadata.getVariables().hasVariable("GENDER_2"));
        // Are unimodal datasets removed ?
        assertEquals(Set.of("MULTIMODE"), vtlBindings.keySet());
    }
}
