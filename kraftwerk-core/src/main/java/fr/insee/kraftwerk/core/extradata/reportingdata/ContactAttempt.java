package fr.insee.kraftwerk.core.extradata.reportingdata;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
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
  
  public LocalDateTime getDate() {
	  return LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp), ZoneId.of("CET"));
  }
  
  
}
