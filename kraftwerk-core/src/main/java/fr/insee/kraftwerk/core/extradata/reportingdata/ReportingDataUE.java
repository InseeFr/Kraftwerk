package fr.insee.kraftwerk.core.extradata.reportingdata;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.TreeSet;
import java.util.stream.Collectors;

public class ReportingDataUE {
  private String identifier;
  
  private List<State> states;
  
  private String OrganizationUnitId;
  
  private String InterviewerId;
  
  private InseeSampleIdentiers inseeSampleIdentiers;
  
  private ContactOutcome contactOutcome;
  
  private List<ContactAttempt> contactAttempts;
  
  public ReportingDataUE() {
    this.states = new ArrayList<>();
    this.contactAttempts = new ArrayList<>();
  }
  
  public ReportingDataUE(String identifier) {
    this.identifier = identifier;
    this.states = new ArrayList<>();
    this.contactAttempts = new ArrayList<>();
  }
  
  public String getIdentifier() {
    return this.identifier;
  }
  
  public void setIdentifier(String identifier) {
    this.identifier = identifier;
  }
  
  public List<State> getStates() {
    return this.states;
  }
  
  public void setStates(List<State> states) {
    this.states = states;
  }
  
  public void putStates(List<State> states) {
    for (State state : states)
      this.states.add(state); 
  }
  
  public void addState(State state) {
    this.states.add(state);
  }
  
  public int size() {
    return this.states.size();
  }
  
  public String getOrganizationUnitId() {
    return this.OrganizationUnitId;
  }
  
  public void setOrganizationUnitId(String organizationUnitId) {
    this.OrganizationUnitId = organizationUnitId;
  }
  
  public String getInterviewerId() {
    return this.InterviewerId;
  }
  
  public void setInterviewerId(String interviewerId) {
    this.InterviewerId = interviewerId;
  }
  
  public InseeSampleIdentiers getInseeSampleIdentiers() {
    return this.inseeSampleIdentiers;
  }
  
  public void setInseeSampleIdentiers(InseeSampleIdentiers inseeSampleIdentiers) {
    this.inseeSampleIdentiers = inseeSampleIdentiers;
  }
  
  public ContactOutcome getContactOutcome() {
    return this.contactOutcome;
  }
  
  public void setContactOutcome(ContactOutcome contactOutcome) {
    this.contactOutcome = contactOutcome;
  }
  
  public List<ContactAttempt> getContactAttempts() {
    return this.contactAttempts;
  }
  
  public void setContactAttempts(List<ContactAttempt> contactAttempts) {
    this.contactAttempts = contactAttempts;
  }
  
  public void addContactAttempts(ContactAttempt contactAttempt) {
    this.contactAttempts.add(contactAttempt);
  }
  
  public void sortStates() {
    this.setStates((List<State>) this.getStates()
        .stream()
        .distinct()
        .collect(Collectors.collectingAndThen(Collectors.toCollection(() -> new TreeSet(Comparator.comparingLong(State::getTimestamp))), 
                
                ArrayList::new)));;
  }
  
}
