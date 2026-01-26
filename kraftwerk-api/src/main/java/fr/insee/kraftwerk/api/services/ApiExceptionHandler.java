package fr.insee.kraftwerk.api.services;

import fr.insee.kraftwerk.api.client.GenesisApiException;
import fr.insee.kraftwerk.api.dto.ApiError;
import fr.insee.kraftwerk.core.exceptions.KraftwerkException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(GenesisApiException.class)
    public ResponseEntity<ApiError> handleGenesisException(GenesisApiException e) {

        ApiError error = new ApiError(
                e.getStatus().value(),
                e.getMessage()
        );

        return ResponseEntity
                .status(e.getStatus())
                .body(error);
    }

    @ExceptionHandler(KraftwerkException.class)
    public ResponseEntity<ApiError> handleKraftwerkException(KraftwerkException e) {
        ApiError error = new ApiError(
                e.getStatus(),
                e.getMessage()
        );

        return ResponseEntity
                .status(e.getStatus())
                .body(error);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleGenericException(Exception ex) {

        ApiError error = new ApiError(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Erreur interne du serveur"
        );

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(error);
    }


}
