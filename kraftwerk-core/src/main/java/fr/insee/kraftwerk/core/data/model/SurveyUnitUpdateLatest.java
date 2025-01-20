package fr.insee.kraftwerk.core.data.model;

import lombok.Data;

import java.util.List;

@Data
public class SurveyUnitUpdateLatest {

	private String idQuest;
	private String idCampaign;
	private String idUE;
	private Mode mode;
	private List<VariableModel> collectedVariables;
	private List<VariableModel> externalVariables;
}
