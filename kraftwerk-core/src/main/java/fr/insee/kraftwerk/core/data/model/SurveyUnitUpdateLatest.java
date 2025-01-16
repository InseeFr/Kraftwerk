package fr.insee.kraftwerk.core.data.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class SurveyUnitUpdateLatest {

	@JsonProperty("questionnaireId")
	private String idQuest;
	@JsonProperty("campaignId")
	private String idCampaign;
	@JsonProperty("interrogationId")
	private String idUE;
	private Mode mode;
	private List<VariableState> variablesUpdate;
	private List<ExternalVariable> externalVariables;
}
