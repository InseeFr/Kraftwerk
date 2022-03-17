package fr.insee.kraftwerk.core.extradata.reportingdata;

public class State {

	private String stateType;

	private long timestamp;

	public State() {
		super();
	}
	
	public State(String stateType) {
		super();
		this.stateType = stateType;
	}
	
	public State(String stateType, long timestamp) {
		super();
		this.stateType = stateType;
		this.timestamp = timestamp;
	}

	public String getStateType() {
		return stateType;
	}

	public void setStateType(String stateType) {
		this.stateType = stateType;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

}
