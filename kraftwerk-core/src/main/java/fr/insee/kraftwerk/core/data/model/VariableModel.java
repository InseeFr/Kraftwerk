package fr.insee.kraftwerk.core.data.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class VariableModel {

	private String varId;
	private String loopId;
	private String parentId;
	private List<String> values;

	public String getFirstValue(){
		return values.getFirst();
	}
}
