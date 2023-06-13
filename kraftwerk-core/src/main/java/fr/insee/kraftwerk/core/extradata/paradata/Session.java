package fr.insee.kraftwerk.core.extradata.paradata;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class Session {

	private String identifier;
	private long initialization;
	private long termination;

	
	public Session(String identifier) {
		super();
		this.identifier = identifier;
		this.initialization = 0L;
		this.termination = 0L;
	}


	public long getDuration() {
		return getTermination() - getInitialization();
	}
}
