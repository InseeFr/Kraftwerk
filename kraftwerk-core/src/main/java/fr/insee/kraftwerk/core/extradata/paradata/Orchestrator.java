package fr.insee.kraftwerk.core.extradata.paradata;

public class Orchestrator {
  private String identifier;
  
  private long initialization;
  
  private long validation;
  
  public String getIdentifier() {
    return this.identifier;
  }
  
  public void setIdentifier(String identifier) {
    this.identifier = identifier;
  }
  
  public long getInitialization() {
    return this.initialization;
  }
  
  public void setInitialization(long initialization) {
    this.initialization = initialization;
  }
  
  public long getValidation() {
    return this.validation;
  }
  
  public void setValidation(long validation) {
    this.validation = validation;
  }
  
  public Orchestrator() {}
  
  public Orchestrator(String identifier) {
    this.identifier = identifier;
  }
  
  public Orchestrator(String identifier, long initialization, long validation) {
    this.identifier = identifier;
    this.initialization = initialization;
    this.validation = validation;
  }
}
