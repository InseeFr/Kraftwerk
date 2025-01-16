package fr.insee.kraftwerk.core.data.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class ExternalVariable {

	private String varId;
	private List<String> values;

	public String getFirstValue(){
		return values.getFirst();
	}
}
