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

  
  
}
