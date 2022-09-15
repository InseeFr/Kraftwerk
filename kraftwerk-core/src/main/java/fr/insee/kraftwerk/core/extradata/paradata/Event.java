package fr.insee.kraftwerk.core.extradata.paradata;

import lombok.Getter;
import lombok.Setter;

public class Event {
	@Getter	@Setter
	protected String idSurveyUnit;
	@Getter	@Setter
	private String idParadataObject;
	@Getter	@Setter
	protected String idSession;
	@Getter	@Setter
	protected Object value;
	@Getter	@Setter
	protected long timestamp;

	public Event(String idSurveyUnit) {
		this.idSurveyUnit = idSurveyUnit;
	}

	public Event(String idSurveyUnit, String idSession) {
		this.idSurveyUnit = idSurveyUnit;
		this.idSession = idSession;
	}

}
