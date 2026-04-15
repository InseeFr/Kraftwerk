package fr.insee.kraftwerk.api.services.async;

import java.time.Instant;

public record JobExecution(
        String jobId,
        JobStatus status,
        String errorMessage,
        Instant startedAt,
        Instant endedAt
) {}
