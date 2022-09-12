package fr.insee.kraftwerk.core.extradata.paradata;

public class Event {
  protected String idSurveyUnit;
  
  private String idParadataObject;
  
  protected String idSession;
  
  private Object value;
  
  protected long timestamp;
  
  public Event() {}
  
  public Event(String idSurveyUnit) {
    this.idSurveyUnit = idSurveyUnit;
  }
  
  public Event(String idSurveyUnit, String idSession) {
    this.idSurveyUnit = idSurveyUnit;
    this.idSession = idSession;
  }
  
  public String getIdSurveyUnit() {
    return this.idSurveyUnit;
  }
  
  public void setIdSurveyUnit(String idSurveyUnit) {
    this.idSurveyUnit = idSurveyUnit;
  }
  
  public String getIdParadataObject() {
    return this.idParadataObject;
  }
  
  public void setIdParadataObject(String idParadataObject) {
    this.idParadataObject = idParadataObject;
  }
  
  public String getIdSession() {
    return this.idSession;
  }
  
  public void setIdSession(String idSession) {
    this.idSession = idSession;
  }
  
  public Object getValue() {
    return this.value;
  }
  
  public void setValue(Object value) {
    this.value = value;
  }
  
  public long getTimestamp() {
    return this.timestamp;
  }
  
  public void setTimestamp(long timestamp) {
    this.timestamp = timestamp;
  }
}
