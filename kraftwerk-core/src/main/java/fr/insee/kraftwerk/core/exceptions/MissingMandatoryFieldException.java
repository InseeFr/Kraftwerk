package fr.insee.kraftwerk.core.exceptions;

import fr.insee.kraftwerk.core.inputs.UserInputsFile;

import java.io.Serial;

public class MissingMandatoryFieldException extends IllegalArgumentException {

    /**
     * Exception raised if a user specifies an unknown data format.
     * @see UserInputsFile
     */
    @Serial
    private static final long serialVersionUID = 1L;

    public MissingMandatoryFieldException(String s) {
        super(s);
    }
}
