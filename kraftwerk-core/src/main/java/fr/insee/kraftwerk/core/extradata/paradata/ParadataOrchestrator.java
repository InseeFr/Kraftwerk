package fr.insee.kraftwerk.core.extradata.paradata;

public class ParadataOrchestrator extends Event {
  
  private String objectName;
  
  public ParadataOrchestrator() {}
  
  public ParadataOrchestrator(String idSurveyUnit) {
    super(idSurveyUnit);
  }
  
  public ParadataOrchestrator(String idSurveyUnit, String idSession) {
    super(idSurveyUnit, idSession);
  }
  
  public ParadataOrchestrator(String idSurveyUnit, String idSession, String objectName) {
    super(idSurveyUnit, idSession);
    this.objectName = objectName;
  }

  public String getObjectName() {
    return this.objectName;
  }
  
  public void setObjectName(String objectName) {
    this.objectName = objectName;
  }
  
}
