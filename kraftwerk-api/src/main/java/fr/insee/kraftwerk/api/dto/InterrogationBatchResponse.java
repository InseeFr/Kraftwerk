package fr.insee.kraftwerk.api.dto;

import fr.insee.kraftwerk.core.data.model.InterrogationId;
import lombok.Data;

import java.time.Instant;
import java.util.List;

@Data
public class InterrogationBatchResponse {

    private List<InterrogationId> interrogationIds;
    private Instant nextSince;

}
