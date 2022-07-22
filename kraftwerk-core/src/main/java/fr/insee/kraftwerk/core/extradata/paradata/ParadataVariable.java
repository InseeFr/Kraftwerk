package fr.insee.kraftwerk.core.extradata.paradata;

public class ParadataVariable extends Event {
	private String idSurveyUnit;

	private String variableName;

	private Object value;

	private long timestamp;

	public ParadataVariable() {
	}

	public ParadataVariable(String idSurveyUnit) {
		this.idSurveyUnit = idSurveyUnit;
	}

	public ParadataVariable(String idSurveyUnit, String idSession) {
		this.idSurveyUnit = idSurveyUnit;
	}

	public ParadataVariable(String idSurveyUnit, String idSession, String variableName) {
		this.idSurveyUnit = idSurveyUnit;
		this.variableName = variableName;
	}

	public String getIdSurveyUnit() {
		return this.idSurveyUnit;
	}

	public void setIdSurveyUnit(String idSurveyUnit) {
		this.idSurveyUnit = idSurveyUnit;
	}

	public String getVariableName() {
		return this.variableName;
	}

	public void setVariableName(String variableName) {
		this.variableName = variableName;
	}

	public Object getValue() {
		return this.value;
	}

	public void setValue(Object object) {
		this.value = object;
	}

	public long getTimestamp() {
		return this.timestamp;
	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}
}
