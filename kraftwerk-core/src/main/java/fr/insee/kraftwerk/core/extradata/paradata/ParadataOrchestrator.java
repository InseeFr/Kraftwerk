package fr.insee.kraftwerk.core.extradata.paradata;

public class ParadataOrchestrator extends Event {
  private String idSurveyUnit;
  
  private String idSession;
  
  private String objectName;
  
  private long timestamp;
  
  public ParadataOrchestrator() {}
  
  public ParadataOrchestrator(String idSurveyUnit) {
    this.idSurveyUnit = idSurveyUnit;
  }
  
  public ParadataOrchestrator(String idSurveyUnit, String idSession) {
    this.idSurveyUnit = idSurveyUnit;
    this.idSession = idSession;
  }
  
  public ParadataOrchestrator(String idSurveyUnit, String idSession, String objectName) {
    this.idSurveyUnit = idSurveyUnit;
    this.idSession = idSession;
    this.objectName = objectName;
  }
  
  public String getIdSurveyUnit() {
    return this.idSurveyUnit;
  }
  
  public void setIdSurveyUnit(String idSurveyUnit) {
    this.idSurveyUnit = idSurveyUnit;
  }
  
  public String getObjectName() {
    return this.objectName;
  }
  
  public void setObjectName(String objectName) {
    this.objectName = objectName;
  }
  
  public String getIdSession() {
    return this.idSession;
  }
  
  public void setIdSession(String idSession) {
    this.idSession = idSession;
  }
  
  public long getTimestamp() {
    return this.timestamp;
  }
  
  public void setTimestamp(long timestamp) {
    this.timestamp = timestamp;
  }
}
