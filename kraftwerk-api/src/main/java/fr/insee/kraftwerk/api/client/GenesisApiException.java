package fr.insee.kraftwerk.api.client;

import org.springframework.http.HttpStatus;

public class GenesisApiException extends RuntimeException {

    private final HttpStatus status;

    public GenesisApiException(HttpStatus status, String message, Throwable cause) {
        super(message, cause);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
