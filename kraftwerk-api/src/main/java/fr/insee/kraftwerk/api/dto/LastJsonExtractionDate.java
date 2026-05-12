package fr.insee.kraftwerk.api.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
public class LastJsonExtractionDate {
    private Instant lastExtractionDate;
}
