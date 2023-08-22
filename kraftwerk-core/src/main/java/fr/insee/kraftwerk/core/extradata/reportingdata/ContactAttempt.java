package fr.insee.kraftwerk.core.extradata.reportingdata;

import java.util.Date;

import lombok.Getter;
import lombok.Setter;

@Getter@Setter
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
  
  public Date getDate() {
	  return new Date(timestamp);
  }
  
  
}
