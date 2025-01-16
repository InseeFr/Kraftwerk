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
	private List<VariableState> variablesUpdate;
	private List<ExternalVariable> externalVariables;
}
