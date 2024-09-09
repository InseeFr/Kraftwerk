package fr.insee.kraftwerk.core.exceptions;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.io.Serial;

@AllArgsConstructor
@Getter
public class KraftwerkException extends Exception {

	@Serial
	private static final long serialVersionUID = -7959158367542389147L;

	private final int status;
	private final String message;
	
}
