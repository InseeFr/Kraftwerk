package fr.insee.kraftwerk.core.extradata.paradata;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = {"timestamp", "idParadataObject"})
@ToString
public class Event {
	protected String idSurveyUnit;
	private String idParadataObject;
	protected String idSession;
	protected Object value;
	protected long timestamp;

	public Event(String idSurveyUnit) {
		this.idSurveyUnit = idSurveyUnit;
	}

	public Event(String idSurveyUnit, String idSession) {
		this.idSurveyUnit = idSurveyUnit;
		this.idSession = idSession;
	}

}
