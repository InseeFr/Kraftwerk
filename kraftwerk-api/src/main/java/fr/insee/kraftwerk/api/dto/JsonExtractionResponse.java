package fr.insee.kraftwerk.api.dto;

import java.time.Instant;

import java.time.ZonedDateTime;

public record JsonExtractionResponse(
        String message,
        Instant sinceUtc,
        ZonedDateTime sinceLocal
) {}
