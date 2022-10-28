package fr.insee.kraftwerk.core.dataprocessing;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Test;

import fr.insee.kraftwerk.core.Constants;
import fr.insee.kraftwerk.core.metadata.PaperUcq;
import fr.insee.kraftwerk.core.metadata.UcqVariable;
import fr.insee.kraftwerk.core.metadata.Variable;
import fr.insee.kraftwerk.core.metadata.VariableType;
import fr.insee.kraftwerk.core.metadata.VariablesMap;
import fr.insee.kraftwerk.core.vtl.VtlBindings;
import fr.insee.vtl.model.Dataset;
import fr.insee.vtl.model.Dataset.Role;
import fr.insee.vtl.model.InMemoryDataset;
import fr.insee.vtl.model.Structured;

public class CleanUpProcessingTest {

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
    public void applyCleanUp() {
        // Metadata variables object
        Map<String, VariablesMap> metadataVariables = new LinkedHashMap<>();
        //
        VariablesMap cawiVariables = new VariablesMap();
        cawiVariables.putVariable(new Variable("FOO", cawiVariables.getRootGroup(), VariableType.STRING));
        UcqVariable cawiUcq = new UcqVariable("GENDER", cawiVariables.getRootGroup(), VariableType.STRING);
        cawiUcq.addModality("1", "Male");
        cawiUcq.addModality("2", "Female");
        cawiVariables.putVariable(cawiUcq);
        //
        VariablesMap papiVariables = new VariablesMap();
        papiVariables.putVariable(new Variable("FOO", papiVariables.getRootGroup(), VariableType.STRING));
        UcqVariable papiUcq = new UcqVariable("GENDER", papiVariables.getRootGroup(), VariableType.STRING);
        papiUcq.addModality("1", "Male");
        papiUcq.addModality("2", "Female");
        papiVariables.putVariable(papiUcq);
        papiVariables.putVariable(new PaperUcq("GENDER_1", papiUcq, "1"));
        papiVariables.putVariable(new PaperUcq("GENDER_2", papiUcq, "2"));
        //
        metadataVariables.put("CAWI", cawiVariables);
        metadataVariables.put("PAPI", papiVariables);

        // Datasets
        VtlBindings vtlBindings = new VtlBindings();
        vtlBindings.put("CAWI", cawiDataset);
        vtlBindings.put("PAPI", papiDataset);
        vtlBindings.put("MULTIMODE", multimodeDataset);

        // Apply clean up
        CleanUpProcessing cleanUpProcessing = new CleanUpProcessing(vtlBindings, metadataVariables);
        cleanUpProcessing.applyVtlTransformations("MULTIMODE", null);

        // Are paper indicator variables removed in VTL multimode dataset ?
        assertFalse(vtlBindings.getDataset("MULTIMODE").getDataStructure().containsKey("GENDER_1"));
        assertFalse(vtlBindings.getDataset("MULTIMODE").getDataStructure().containsKey("GENDER_2"));
        // Are these also removed in VariablesMap object ?
        assertFalse(papiVariables.hasVariable("GENDER_1"));
        assertFalse(papiVariables.hasVariable("GENDER_2"));
        // Are unimodal datasets removed ?
        assertEquals(Set.of("MULTIMODE"), vtlBindings.keySet());
    }
}
