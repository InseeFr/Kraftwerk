package fr.insee.kraftwerk.inputs;

public class UnknownDataFormatException extends IllegalArgumentException {

	/**
	 * Exception raised if a user specifies an unknown data format.
	 * @see ModeInputs
	 */
	private static final long serialVersionUID = 1L;

	public UnknownDataFormatException(String s) {
		super(s);
	}
}
