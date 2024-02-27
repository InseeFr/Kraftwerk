package fr.insee.kraftwerk.core.extradata.reportingdata;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ContactOutcome {
  private String outcomeType;
  
  private long dateEndContact;
  
  private int totalNumberOfContactAttempts;
  
  public ContactOutcome() {}
  
  public ContactOutcome(String outcomeType, int totalNumberOfContactAttempts, long dateEndContact) {
    this.outcomeType = outcomeType;
    this.totalNumberOfContactAttempts = totalNumberOfContactAttempts;
    this.dateEndContact = dateEndContact;
  }

}
