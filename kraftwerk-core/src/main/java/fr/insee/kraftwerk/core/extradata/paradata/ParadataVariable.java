package fr.insee.kraftwerk.core.extradata.paradata;

public class ParadataVariable extends Event{

	private String idSurveyUnit;

	private String variableName;

	private Object value;

	private long timestamp;

	public ParadataVariable() {
		super();
	}
	
	public ParadataVariable(String idSurveyUnit) {
		super();
		this.idSurveyUnit = idSurveyUnit;
	}
	
	public ParadataVariable(String idSurveyUnit, String idSession) {
		super();
		this.idSurveyUnit = idSurveyUnit;
	}
	
	public ParadataVariable(String idSurveyUnit, String idSession, String variableName) {
		super();
		this.idSurveyUnit = idSurveyUnit;
		this.variableName = variableName;
	}

	public String getIdSurveyUnit() {
		return idSurveyUnit;
	}

	public void setIdSurveyUnit(String idSurveyUnit) {
		this.idSurveyUnit = idSurveyUnit;
	}

	public String getVariableName() {
		return variableName;
	}

	public void setVariableName(String variableName) {
		this.variableName = variableName;
	}

	public Object getValue() {
		return value;
	}

	public void setValue(Object object) {
		this.value = object;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

}
