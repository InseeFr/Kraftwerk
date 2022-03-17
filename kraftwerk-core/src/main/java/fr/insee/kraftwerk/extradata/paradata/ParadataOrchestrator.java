package fr.insee.kraftwerk.extradata.paradata;

public class ParadataOrchestrator extends Event{

	private String idSurveyUnit;

	// a garder ?
	private String idSession;

	private String objectName;

	private long timestamp;

	public ParadataOrchestrator() {
		super();
	}
	
	public ParadataOrchestrator(String idSurveyUnit) {
		super();
		this.idSurveyUnit = idSurveyUnit;
	}
	
	public ParadataOrchestrator(String idSurveyUnit, String idSession) {
		super();
		this.idSurveyUnit = idSurveyUnit;
		this.idSession = idSession;
	}
	
	public ParadataOrchestrator(String idSurveyUnit, String idSession, String objectName) {
		super();
		this.idSurveyUnit = idSurveyUnit;
		this.idSession = idSession;
		this.objectName = objectName;
	}

	public String getIdSurveyUnit() {
		return idSurveyUnit;
	}

	public void setIdSurveyUnit(String idSurveyUnit) {
		this.idSurveyUnit = idSurveyUnit;
	}

	public String getObjectName() {
		return objectName;
	}

	public void setObjectName(String objectName) {
		this.objectName = objectName;
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
