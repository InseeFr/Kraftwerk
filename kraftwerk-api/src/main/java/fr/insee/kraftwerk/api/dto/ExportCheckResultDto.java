package fr.insee.kraftwerk.api.dto;

public record ExportCheckResultDto(
        String collectionInstrumentId,
        long interrogationsCount
) {
}
