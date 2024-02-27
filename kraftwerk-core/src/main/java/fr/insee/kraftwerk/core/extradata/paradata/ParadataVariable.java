package fr.insee.kraftwerk.core.extradata.paradata;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@EqualsAndHashCode(callSuper = true)
public class ParadataVariable extends Event {
	private String variableName;

	public ParadataVariable(String idSurveyUnit) {
		super(idSurveyUnit);
	}


}
