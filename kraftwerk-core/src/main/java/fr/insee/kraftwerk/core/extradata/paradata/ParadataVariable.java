package fr.insee.kraftwerk.core.extradata.paradata;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@EqualsAndHashCode(callSuper = true)
public class ParadataVariable extends Event {
	@Getter@Setter
	private String variableName;

	public ParadataVariable(String idSurveyUnit) {
		super(idSurveyUnit);
	}


}
