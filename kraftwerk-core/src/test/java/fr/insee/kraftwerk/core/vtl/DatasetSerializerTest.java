package fr.insee.kraftwerk.core.vtl;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import fr.insee.kraftwerk.core.Constants;
import fr.insee.vtl.jackson.TrevasModule;
import fr.insee.vtl.model.Dataset;
import fr.insee.vtl.model.InMemoryDataset;
import fr.insee.vtl.model.Structured;

class DatasetSerializerTest {

    Dataset ds1 = new InMemoryDataset(
            List.of(
                    List.of("UE001", "Lille", "INDIVIDU-1", "Jean", 30),
                    List.of("UE001", "Lille", "INDIVIDU-2", "Frédéric", 42),
                    List.of("UE004", "Amiens", "INDIVIDU-1", "David", 26),
                    List.of("UE005", "", "INDIVIDU-1", "Thibaud ", 18)
            ),
            List.of(
                    new Structured.Component(Constants.ROOT_IDENTIFIER_NAME, String.class, Dataset.Role.IDENTIFIER),
                    new Structured.Component("LIB_COMMUNE", String.class, Dataset.Role.MEASURE),
                    new Structured.Component("INDIVIDU", String.class, Dataset.Role.IDENTIFIER),
                    new Structured.Component("INDIVIDU.PRENOM", String.class, Dataset.Role.MEASURE),
                    new Structured.Component("INDIVIDU.AGE", Integer.class, Dataset.Role.MEASURE)
            )
    );
    
    String expectedJson = "{\"dataStructure\":[{\"name\":\"IdUE\",\"type\":\"STRING\",\"role\":\"IDENTIFIER\",\"nullable\":false},{\"name\":\"LIB_COMMUNE\",\"type\":\"STRING\",\"role\":\"MEASURE\",\"nullable\":true},{\"name\":\"INDIVIDU\",\"type\":\"STRING\",\"role\":\"IDENTIFIER\",\"nullable\":false},{\"name\":\"INDIVIDU.PRENOM\",\"type\":\"STRING\",\"role\":\"MEASURE\",\"nullable\":true},{\"name\":\"INDIVIDU.AGE\",\"type\":null,\"role\":\"MEASURE\",\"nullable\":true}],\"dataPoints\":[[\"UE001\",\"Lille\",\"INDIVIDU-1\",\"Jean\",30],[\"UE001\",\"Lille\",\"INDIVIDU-2\",\"Frédéric\",42],[\"UE004\",\"Amiens\",\"INDIVIDU-1\",\"David\",26],[\"UE005\",\"\",\"INDIVIDU-1\",\"Thibaud \",18]]}";

    @Test
    void testSerializeDataset() throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new TrevasModule());
        String res = objectMapper.writeValueAsString(ds1);
        assertEquals(expectedJson, res);
    }
}
