package fr.insee.kraftwerk.extradata.paradata;


public class Session {

	private String identifier;

	private long initialization;

	private long termination;

	public Session(String identifier) {
		super();
		this.identifier = identifier;
	}

	public Session(String identifier, long initialization) {
		super();
		this.identifier = identifier;
		this.initialization = initialization;
	}

	public Session(String identifier, long initialization, long termination) {
		super();
		this.identifier = identifier;
		this.initialization = initialization;
		this.termination = termination;
	}

	public Session(long initialization, long termination) {
		super();
		this.initialization = initialization;
		this.termination = termination;
	}

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

	public long getTermination() {
		return termination;
	}

	public void setTermination(long termination) {
		this.termination = termination;
	}
	
}
