package fr.insee.kraftwerk.core.extradata.paradata;

import lombok.Getter;
import lombok.Setter;

public class ParadataOrchestrator extends Event {

	@Getter	@Setter
	private String objectName;


	public ParadataOrchestrator(String idSurveyUnit, String idSession) {
		super(idSurveyUnit, idSession);
	}


}
