package fr.insee.kraftwerk.core.extradata.reportingdata;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import lombok.Getter;
import lombok.Setter;
@Getter@Setter
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
   
  public void putStates(List<State> states) {
    for (State state : states) addState(state);
  }
  
  public void addState(State state) {
	  if (state != null) {
		 long nbEquals = states.stream().filter(s -> s.equals(state)).count(); //count nb states equals to the state to insert
		 if (nbEquals==0) this.states.add(state);
	  }
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
