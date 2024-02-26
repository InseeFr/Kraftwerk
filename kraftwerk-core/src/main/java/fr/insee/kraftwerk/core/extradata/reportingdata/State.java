package fr.insee.kraftwerk.core.extradata.reportingdata;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

@AllArgsConstructor
@Getter@Setter
@EqualsAndHashCode
public class State{
	@NonNull
  private String stateType;
  private long timestamp;
   
  public State(String stateType) {
    this.stateType = stateType;
  }

  /**
   * @return the priority order if it's considered as a validation state, null otherwise
   */
  private Integer getPriorityOrder(){
    return switch (stateType){
      case "PARTIELINT" -> 3;
      case "VALPAP" -> 2;
      case "VALINT" -> 1;
      default -> null;
    };
  }

  /**
   * @return true if it's considered as a validation state
   */
  public boolean isValidationState(){
    return (getPriorityOrder() != null);
  }

  public boolean isPriorTo(State otherState){
    if(otherState == null) {
      return true;
    }
    return (
            // True if this state is more priority than the other
            this.getPriorityOrder() < otherState.getPriorityOrder()
            //True if the other state has the same priority and this state is more recent
            || (isValidationState() && this.getPriorityOrder().equals(otherState.getPriorityOrder()) && this.getTimestamp() > otherState.getTimestamp())
    );
  }
  
}
