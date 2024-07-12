package fr.insee.kraftwerk.core.data.model;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

public class ExternalVariable {

	@Getter
	@Setter
	private String idVar;
	@Getter
	@Setter
	private List<String> values;
}
