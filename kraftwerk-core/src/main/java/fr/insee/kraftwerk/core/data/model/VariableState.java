package fr.insee.kraftwerk.core.data.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class VariableState {

	@JsonProperty("varId")
	private String idVar;
	@JsonProperty("loopId")
	private String idLoop;
	@JsonProperty("parentId")
	private String idParent;
	private List<String> values;
}
