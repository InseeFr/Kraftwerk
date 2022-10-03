package fr.insee.kraftwerk.core.exceptions;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public class KraftwerkException extends Exception {

	private static final long serialVersionUID = -7959158367542389147L;

	@Getter
	private final int status;
	@Getter
	private final String message;
	
}
