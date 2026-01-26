package fr.insee.kraftwerk.api.dto;

public record ApiError(
        int status,
        String message
) {}
