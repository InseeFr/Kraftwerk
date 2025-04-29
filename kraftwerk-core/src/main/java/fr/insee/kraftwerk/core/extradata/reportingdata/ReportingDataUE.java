package fr.insee.kraftwerk.core.extradata.reportingdata;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Setter
@Getter
public class ReportingDataUE {
  private String identifier;
  
  private List<State> states;
  
  private String organizationUnitId;
  
  private String interviewerId;
  
  private InseeSampleIdentifier inseeSampleIdentifier;
  
  private ContactOutcome contactOutcome;
  
  private List<ContactAttempt> contactAttempts;

  private ReportingIdentification identification;

  private List<Comment> comments;

  private Long surveyValidationDateTimeStamp;
  private ReportingDataClosingCause reportingDataClosingCause;
  
  public ReportingDataUE() {
    this.states = new ArrayList<>();
    this.contactAttempts = new ArrayList<>();
    this.comments = new ArrayList<>();
  }
  
  public ReportingDataUE(String identifier) {
    this.identifier = identifier;
    this.states = new ArrayList<>();
    this.contactAttempts = new ArrayList<>();
    this.comments = new ArrayList<>();
  }
   
  public void putStates(List<State> states) {
    for (State state : states) addState(state);
  }
  
  public void addState(State state) {
	  if (state != null) {
		 long nbEquals = states.stream().filter(s -> s.equals(state)).count(); //count nb states equals to the state to insert
		 if (nbEquals==0){ this.states.add(state);}
	  }
  }
  
  public int size() {
    return this.states.size();
  }
      
  public void addContactAttempt(ContactAttempt contactAttempt) {
    this.contactAttempts.add(contactAttempt);
  }
  public void addComment(Comment comment) {
    this.comments.add(comment);
  }
  
  public void sortStates() {
    this.setStates(this.getStates()
    		.stream()
            .distinct()
            .sorted(Comparator.comparingLong(State::getTimestamp))
            .collect(Collectors.toList()));
  }
  
}
