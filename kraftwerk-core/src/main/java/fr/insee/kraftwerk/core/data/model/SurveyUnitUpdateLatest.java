package fr.insee.kraftwerk.core.data.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class SurveyUnitUpdateLatest {

	private String questionnaireId;
	private String campaignId;
	private String interrogationId;
	private String surveyUnitId;
	private DataState state;
	private Mode mode;
	private String contextualId;
	private Boolean isCapturedIndirectly;
	private LocalDateTime validationDate;
	@JsonProperty("variablesUpdate")
	private List<VariableModel> collectedVariables;
	private List<VariableModel> externalVariables;

}
