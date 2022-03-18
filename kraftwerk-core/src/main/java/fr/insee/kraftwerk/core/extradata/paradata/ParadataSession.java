package fr.insee.kraftwerk.core.extradata.paradata;

public class ParadataSession extends Event{

	private String idSurveyUnit;

	private String idSession;

	private long timestamp;

	public ParadataSession() {
		super();
	}
	
	public ParadataSession(String idSurveyUnit) {
		super();
		this.idSurveyUnit = idSurveyUnit;
	}
	
	public ParadataSession(String idSurveyUnit, String idSession) {
		super();
		this.idSurveyUnit = idSurveyUnit;
		this.idSession = idSession;
	}

	public String getIdSurveyUnit() {
		return idSurveyUnit;
	}

	public void setIdSurveyUnit(String idSurveyUnit) {
		this.idSurveyUnit = idSurveyUnit;
	}

	public String getIdSession() {
		return idSession;
	}

	public void setIdSession(String idSession) {
		this.idSession = idSession;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

}
