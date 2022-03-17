package fr.insee.kraftwerk.extradata.reportingdata;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class ReportingDataUE {

	private String identifier;
	
	private List<State> states;

	public ReportingDataUE() {
		super();
		this.states = new ArrayList<State>();
	}

	public ReportingDataUE(String identifier) {
		super();
		this.identifier = identifier;
		this.states = new ArrayList<State>();
	}
	
	public String getIdentifier() {
		return identifier;
	}

	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}

	public List<State> getStates() {
		return states;
	}

	public void setStates(List<State> states) {
		this.states = states;
	}

	public void putStates(List<State> states) {
		for (State state : states) {
			this.states.add(state);
		}
	}

	public void addState(State state) {
		this.states.add(state);
	}

	public int size() {
		return this.states.size();
	}


	/**
	 * Sort the events according to their timestamps. Need to be careful with
	 * paradata, as it tends to not be structured chronologically
	 *
	 * @param paraData the paradata
	 */
	public void sortStates() {
		List<State>statesToSort = this.getStates();
		Collections.sort(statesToSort, new Comparator<State>() {
			public int compare(State s1, State s2) {
				return (int) (s1.getTimestamp() - s2.getTimestamp());
			}
		});
		for (int index = 0; index < statesToSort.size() - 1; index++) {
			if (statesToSort.get(index).getTimestamp() == statesToSort.get(index + 1).getTimestamp()) {
				statesToSort.remove(index + 1);
			}
		}
		this.setStates(statesToSort);
	}
	
}
