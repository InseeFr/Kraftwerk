package fr.insee.kraftwerk.core.data.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class SurveyUnitUpdateLatest {

	private String questionnaireId;
	private String campaignId;
	private String interrogationId;
	private Mode mode;
	@JsonProperty("variablesUpdate")
	private List<VariableModel> collectedVariables;
	private List<VariableModel> externalVariables;
}
