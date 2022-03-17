package fr.insee.kraftwerk.extradata.paradata;

public class Event {

	private String idSurveyUnit;

	private String idParadataObject;

	private String idSession;

	private Object value;

	private long timestamp;

	public Event() {
		super();
	}
	
	public Event(String idSurveyUnit) {
		super();
		this.idSurveyUnit = idSurveyUnit;
	}
	
	public Event(String idSurveyUnit, String idSession) {
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

	public String getIdParadataObject() {
		return idParadataObject;
	}

	public void setIdParadataObject(String idParadataObject) {
		this.idParadataObject = idParadataObject;
	}

	public String getIdSession() {
		return idSession;
	}

	public void setIdSession(String idSession) {
		this.idSession = idSession;
	}

	public Object getValue() {
		return value;
	}

	public void setValue(Object value) {
		this.value = value;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

}
