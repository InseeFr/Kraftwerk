package fr.insee.kraftwerk.core.dataprocessing;

import fr.insee.kraftwerk.core.Constants;
import fr.insee.kraftwerk.core.KraftwerkError;
import fr.insee.kraftwerk.core.vtl.VtlBindings;
import fr.insee.vtl.model.Dataset;
import fr.insee.vtl.model.InMemoryDataset;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class ReportingDataProcessingTest {
    private final String rootId = Constants.ROOT_IDENTIFIER_NAME;

    Dataset testDataset = new InMemoryDataset(
            List.of(
                    Map.of(rootId, "001", "DATA1", "TEST1", "ATTEMPT_1", "REF", "ATTEMPT_1_DATE", "2022-01-01 00:00:01"),
                    Map.of(rootId, "002", "DATA1", "TEST2", "ATTEMPT_1", "APT", "ATTEMPT_1_DATE", "2022-01-01 00:00:02", "ATTEMPT_2", "INA", "ATTEMPT_2_DATE", "2022-02-02 00:00:02")
            ),
            Map.of(rootId, String.class, "DATA1", String.class,
                    "ATTEMPT_1", String.class, "ATTEMPT_1_DATE", Date.class,
                    "ATTEMPT_2", String.class, "ATTEMPT_2_DATE", Date.class),
            Map.of(rootId, Dataset.Role.IDENTIFIER, "DATA1", Dataset.Role.MEASURE,
                    "ATTEMPT_1", Dataset.Role.MEASURE, "ATTEMPT_1_DATE", Dataset.Role.MEASURE,
                    "ATTEMPT_2", Dataset.Role.MEASURE, "ATTEMPT_2_DATE", Dataset.Role.MEASURE)
    );

    Dataset testDatasetNoContactAttempt = new InMemoryDataset(
            List.of(
                    Map.of(rootId, "001", "DATA1", "TEST1")
            ),
            Map.of(rootId, String.class, "DATA1", String.class),
            Map.of(rootId, Dataset.Role.IDENTIFIER, "DATA1", Dataset.Role.MEASURE)
    );

    @Test
    void applyContactAttemptsProcessing() {
        List<KraftwerkError> errors = new ArrayList<>();
        //
        VtlBindings vtlBindings = new VtlBindings();
        vtlBindings.put("MULTIMODE", testDataset);
        //
        ReportingDataProcessing processing = new ReportingDataProcessing(vtlBindings);
        processing.applyAutomatedVtlInstructions("MULTIMODE",errors);
        //
        Dataset contactAttemptsDataset = vtlBindings.getDataset(Constants.REPORTING_DATA_DATASET_NAME);


        assertThat(contactAttemptsDataset).isNotNull();

        Map<String, Object> contactAttemptsMap0 = contactAttemptsDataset.getDataAsMap().get(0);
        assertThat(contactAttemptsMap0).containsEntry(rootId,"001");
        assertThat(contactAttemptsMap0).containsEntry("ATTEMPT_1","REF");
        assertThat(contactAttemptsMap0).containsEntry("ATTEMPT_1_DATE","2022-01-01 00:00:01");
        assertThat(contactAttemptsMap0).doesNotContainEntry("DATA1","TEST1");

        Map<String, Object> contactAttemptsMap1 = contactAttemptsDataset.getDataAsMap().get(1);
        assertThat(contactAttemptsMap1).containsEntry("ATTEMPT_1","APT");
        assertThat(contactAttemptsMap1).containsEntry("ATTEMPT_1_DATE","2022-01-01 00:00:02");
        assertThat(contactAttemptsMap1).containsEntry("ATTEMPT_2","INA");
        assertThat(contactAttemptsMap1).containsEntry("ATTEMPT_2_DATE","2022-02-02 00:00:02");
        assertThat(contactAttemptsMap1).doesNotContainEntry("DATA1","TEST2");
    }

    @Test
    void applyContactAttemptsProcessingWithoutContactAttempt() {
        List<KraftwerkError> errors = new ArrayList<>();
        //
        VtlBindings vtlBindings = new VtlBindings();
        vtlBindings.put("MULTIMODE", testDatasetNoContactAttempt);
        //
        ReportingDataProcessing processing = new ReportingDataProcessing(vtlBindings);
        processing.applyAutomatedVtlInstructions("MULTIMODE",errors);
        //
        Dataset contactAttemptsDataset = vtlBindings.getDataset(Constants.REPORTING_DATA_DATASET_NAME);


        assertThat(contactAttemptsDataset).isNull();
    }

}
