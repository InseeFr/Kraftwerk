package fr.insee.kraftwerk.core.data.model;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class ExternalVariable {

	private String idVar;
	private List<String> values;

	public String getFirstValue(){
		return values.getFirst();
	}
}
