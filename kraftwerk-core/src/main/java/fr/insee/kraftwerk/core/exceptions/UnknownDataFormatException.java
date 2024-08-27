package fr.insee.kraftwerk.core.exceptions;

import fr.insee.kraftwerk.core.inputs.ModeInputs;

import java.io.Serial;

public class UnknownDataFormatException extends IllegalArgumentException {

	/**
	 * Exception raised if a user specifies an unknown data format.
	 * @see ModeInputs
	 */
	@Serial
	private static final long serialVersionUID = 1L;

	public UnknownDataFormatException(String s) {
		super(s);
	}
}
