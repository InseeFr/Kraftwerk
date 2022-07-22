package fr.insee.kraftwerk.core.extradata.reportingdata;

public class State {
  private String stateType;
  
  private long timestamp;
  
  public State() {}
  
  public State(String stateType) {
    this.stateType = stateType;
  }
  
  public State(String stateType, long timestamp) {
    this.stateType = stateType;
    this.timestamp = timestamp;
  }
  
  public String getStateType() {
    return this.stateType;
  }
  
  public void setStateType(String stateType) {
    this.stateType = stateType;
  }
  
  public long getTimestamp() {
    return this.timestamp;
  }
  
  public void setTimestamp(long timestamp) {
    this.timestamp = timestamp;
  }
}
