package fr.insee.kraftwerk.core.extradata.reportingdata;

public class ContactAttempt {
  private String status;
  
  private long timestamp;
  
  public ContactAttempt() {}
  
  public ContactAttempt(String status) {
    this.status = status;
  }
  
  public ContactAttempt(String status, long timestamp) {
    this.status = status;
    this.timestamp = timestamp;
  }
  
  public String getStatus() {
    return this.status;
  }
  
  public void setStatus(String status) {
    this.status = status;
  }
  
  public long getTimestamp() {
    return this.timestamp;
  }
  
  public void setTimestamp(long timestamp) {
    this.timestamp = timestamp;
  }
}
