package fr.insee.kraftwerk.core.data.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class SurveyUnitUpdateLatest {

	private String collectionInstrumentId;
	/**
	 * @deprecated We will not receive this piece of information anymore
	 */
	@Deprecated(forRemoval = true)
	private String campaignId;
	private String interrogationId;
	private String usualSurveyUnitId;
	private DataState state;
	private Mode mode;
	private Boolean isCapturedIndirectly;
	private LocalDateTime validationDate;
	private String questionnaireState;
	@JsonProperty("variablesUpdate")
	private List<VariableModel> collectedVariables;
	private List<VariableModel> externalVariables;

}
