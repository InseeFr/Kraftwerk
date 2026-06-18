package fr.insee.kraftwerk.api.dto;

import java.time.ZonedDateTime;

public record JsonReplayResponse(
        String message,
        ZonedDateTime localSinceDate,
        ZonedDateTime localEndDate
) {}
