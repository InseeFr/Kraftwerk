package fr.insee.kraftwerk.core.extradata.reportingdata;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

@AllArgsConstructor
@Getter@Setter
public class State{
	@NonNull
  private String stateType;
  private long timestamp;
   
  public State(String stateType) {
    this.stateType = stateType;
  }

  @Override
  public boolean equals(Object obj){
	    if(obj instanceof State){
	       return (((State) obj).getStateType().equals(this.getStateType()) && ((State)obj).getTimestamp()==this.getTimestamp()); 
	    }
	    return false;
	}
  
  
}
