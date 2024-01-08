package fr.insee.kraftwerk.core.data.model;

import lombok.Getter;

import java.util.List;

public class ExternalVariable {

	@Getter
	private String idVar;
	@Getter
	private List<String> values;
}
