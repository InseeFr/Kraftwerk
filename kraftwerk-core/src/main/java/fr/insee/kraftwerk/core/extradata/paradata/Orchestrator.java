package fr.insee.kraftwerk.core.extradata.paradata;


public class Orchestrator {

	private String identifier;

	private long initialization;

	private long validation;

	public String getIdentifier() {
		return identifier;
	}

	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}

	public long getInitialization() {
		return initialization;
	}

	public void setInitialization(long initialization) {
		this.initialization = initialization;
	}

	public long getValidation() {
		return validation;
	}

	public void setValidation(long validation) {
		this.validation = validation;
	}
	
	public Orchestrator() {
		super();
	}
	
	public Orchestrator(String identifier) {
		super();
		this.identifier = identifier;
	}

	public Orchestrator(String identifier, long initialization, long validation) {
		super();
		this.identifier = identifier;
		this.initialization = initialization;
		this.validation = validation;
	}
	
}
