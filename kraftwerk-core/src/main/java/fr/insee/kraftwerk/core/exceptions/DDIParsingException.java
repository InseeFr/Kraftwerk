package fr.insee.kraftwerk.core.exceptions;

public class DDIParsingException extends Exception {

    public DDIParsingException(String message, Exception exception) {
        super(message, exception);
    }

}
