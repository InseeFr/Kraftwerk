package fr.insee.kraftwerk.core.extradata.paradata;

import lombok.Getter;
import lombok.Setter;

public class Orchestrator {
	@Getter	@Setter
  private String identifier;
	@Getter	@Setter
  private long initialization;
	@Getter	@Setter
  private long validation;
    
  
  public Orchestrator(String identifier, long initialization, long validation) {
    this.identifier = identifier;
    this.initialization = initialization;
    this.validation = validation;
  }
}
