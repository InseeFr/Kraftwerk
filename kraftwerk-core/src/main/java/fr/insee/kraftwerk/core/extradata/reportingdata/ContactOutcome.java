package fr.insee.kraftwerk.core.extradata.reportingdata;

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
  
  public String getOutcomeType() {
    return this.outcomeType;
  }
  
  public void setOutcomeType(String outcomeType) {
    this.outcomeType = outcomeType;
  }
  
  public int getTotalNumberOfContactAttempts() {
    return this.totalNumberOfContactAttempts;
  }
  
  public void setTotalNumberOfContactAttempts(int totalNumberOfContactAttempts) {
    this.totalNumberOfContactAttempts = totalNumberOfContactAttempts;
  }
  
  public long getDateEndContact() {
    return this.dateEndContact;
  }
  
  public void setDateEndContact(long dateEndContact) {
    this.dateEndContact = dateEndContact;
  }
}
