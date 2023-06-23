package fr.insee.kraftwerk.core.extradata.paradata;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class Orchestrator {

	private String identifier;
	private long initialization;
	private long validation;
	
	public long getDuration() {
		return getValidation() - getInitialization();
	}

	public Orchestrator(String identifier) {
		super();
		this.identifier = identifier;
		this.initialization = 0L;
		this.validation = 0L;
	}

}
