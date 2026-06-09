package fr.insee.kraftwerk.api.exceptions;

public class BatchExecutionFailedException extends RuntimeException {
    public BatchExecutionFailedException(Throwable cause) {
        super(cause);
    }
}
