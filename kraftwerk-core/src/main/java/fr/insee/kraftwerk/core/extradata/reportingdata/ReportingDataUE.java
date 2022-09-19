package fr.insee.kraftwerk.core.extradata.reportingdata;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import lombok.Getter;
import lombok.Setter;

public class ReportingDataUE {
	@Getter@Setter
  private String identifier;
  
  @Getter@Setter
  private List<State> states;
  
  @Getter@Setter
  private String OrganizationUnitId;
  
  @Getter@Setter
  private String InterviewerId;
  
  @Getter@Setter
  private InseeSampleIdentiers inseeSampleIdentiers;
  
  @Getter@Setter
  private ContactOutcome contactOutcome;
  
  @Getter@Setter
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
   
  public void putStates(List<State> states) {
    for (State state : states) addState(state);
  }
  
  public void addState(State state) {
	  if (state != null) this.states.add(state);
  }
  
  public int size() {
    return this.states.size();
  }
      
  public void addContactAttempts(ContactAttempt contactAttempt) {
    this.contactAttempts.add(contactAttempt);
  }
  
  public void sortStates() {
    this.setStates(this.getStates()
    		.stream()
            .distinct()
            .sorted(Comparator.comparingLong(State::getTimestamp))
            .collect(Collectors.toList()));
  }
  
}
