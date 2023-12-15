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
  private String organizationUnitId;
  
  @Getter@Setter
  private String interviewerId;
  
  @Getter@Setter
  private InseeSampleIdentifier inseeSampleIdentifier;
  
  @Getter@Setter
  private ContactOutcome contactOutcome;
  
  @Getter@Setter
  private List<ContactAttempt> contactAttempts;

  @Getter@Setter
  private Identification identification;

  @Getter@Setter
  private List<Comment> comments;
  
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
