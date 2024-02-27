package fr.insee.kraftwerk.core.extradata.reportingdata;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import lombok.Getter;
import lombok.Setter;

@Getter
public class ReportingDataUE {
  @Setter
  private String identifier;
  
  @Setter
  private List<State> states;
  
  @Setter
  private String organizationUnitId;
  
  @Setter
  private String interviewerId;
  
  @Setter
  private InseeSampleIdentifier inseeSampleIdentifier;
  
  @Setter
  private ContactOutcome contactOutcome;
  
  @Setter
  private List<ContactAttempt> contactAttempts;

  @Setter
  private Identification identification;

  @Setter
  private List<Comment> comments;

  @Setter
  private Long surveyValidationDateTimeStamp;
  
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
		 if (nbEquals==0) this.states.add(state);
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
