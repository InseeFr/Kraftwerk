package fr.insee.kraftwerk.extradata.paradata;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

public class ParaDataUE {

	private String filepath;

	private String identifier;
	
	private List<Event> events;
	
	private HashMap<String, List<ParadataVariable>> paraDataVariables;
	
	private List<ParadataOrchestrator> paraDataOrchestrators;
	
	private List<ParadataSession> paraDataSessions;
	
	private List<Session> sessions;
	
	private List<Orchestrator> orchestrators;

	public ParaDataUE() {
		super();
		this.events = new ArrayList<Event>();
		this.paraDataVariables = new LinkedHashMap<String, List<ParadataVariable>>();
		this.paraDataOrchestrators = new ArrayList<ParadataOrchestrator>();
		this.paraDataSessions = new ArrayList<ParadataSession>();
		this.sessions = new ArrayList<Session>();
		this.orchestrators = new ArrayList<Orchestrator>();
	}

	public String getFilepath() {
		return filepath;
	}

	public void setFilepath(String filepath) {
		this.filepath = filepath;
	}

	public String getIdentifier() {
		return identifier;
	}
	
	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}

	public List<Event> getEvents() {
		return events;
	}

	public void setEvents(List<Event> events) {
		this.events = events;
	}

	public HashMap<String, List<ParadataVariable>> getParadataVariables() {
		return paraDataVariables;
	}
	
	public List<ParadataVariable> getParadataVariable(String variableName) {
		return this.paraDataVariables.get(variableName);
	}
	
	public void setParadataVariables(HashMap<String, List<ParadataVariable>> paraDataVariables) {
		this.paraDataVariables = paraDataVariables;
	}

	public void addParadataVariable(ParadataVariable paraDataVariable) {
		String variableName = paraDataVariable.getVariableName();
		if (this.paraDataVariables.containsKey(variableName)){
			this.paraDataVariables.get(variableName).add(paraDataVariable);
		} else {
			this.paraDataVariables.put(variableName, new ArrayList<ParadataVariable>());
			this.paraDataVariables.get(variableName).add(paraDataVariable);
		}
		
	}

	public List<ParadataOrchestrator> getParadataOrchestrators() {
		return paraDataOrchestrators;
	}

	public void setParadataOrchestrators(List<ParadataOrchestrator> paraDataOrchestrators) {
		this.paraDataOrchestrators = paraDataOrchestrators;
	}

	public void addParadataOrchestrators(ParadataOrchestrator paraDataOrchestrator) {
		this.paraDataOrchestrators.add(paraDataOrchestrator);
	}

	public List<ParadataSession> getParadataSessions() {
		return paraDataSessions;
	}

	public void setParadataSessions(List<ParadataSession> paraDataSessions) {
		this.paraDataSessions = paraDataSessions;
	}

	public void addParadataSessions(ParadataSession paraDataSession) {
		this.paraDataSessions.add(paraDataSession);
	}

	public List<Session> getSessions() {
		return sessions;
	}

	public void setSessions(List<Session> sessions) {
		this.sessions = sessions;
	}
	
	public void addSession(Session session) {
		this.sessions.add(session);
	}
	
	public List<Orchestrator> getOrchestrators() {
		return orchestrators;
	}
	public void setOrchestrators(List<Orchestrator> orchestrators) {
		this.orchestrators = orchestrators;
	}

	public void addOrchestrator(Orchestrator orchestrator) {
		this.orchestrators.add(orchestrator);
	}

	/**
	 * Sort the events according to their timestamps. Need to be careful with
	 * paradata, as it tends to not be structured chronologically
	 *
	 * @param paraData the paradata
	 */
	public void sortEvents() {
		List<Event> eventsToSort = this.getEvents();
		Collections.sort(eventsToSort, new Comparator<Event>() {
			public int compare(Event e1, Event e2) {
				return (int) (e1.getTimestamp() - e2.getTimestamp());
			}
		});
		for (int index = 0; index < eventsToSort.size() - 1; index++) {
			if (eventsToSort.get(index).getTimestamp() == eventsToSort.get(index + 1).getTimestamp()) {
				eventsToSort.remove(index + 1);
			}
		}
		this.setEvents(eventsToSort);
	}

	/**
	 * summarize data from the Orchestrators to display their length
	 * paradata, as it tends to not be structured chronologically
	 */
	public long createLengthOrchestratorsVariable() {
		long result = 0;
		List<Orchestrator> orchestrators = this.getOrchestrators();
		for (Orchestrator orchestrator : orchestrators) {
			result += orchestrator.getValidation() - orchestrator.getInitialization();
		}
		return result;
	}
	
	/**
	 * Create all information related to the orchestrators
	 */
	public void createOrchestratorsAndSessions() {
			// Entering a specific UE
			String identifier = this.getIdentifier();
			List<Event> listParadataEvents = this.getEvents();
			Session session = new Session("Initialization ongoing", 0, 0);
			Orchestrator orchestrator = new Orchestrator(identifier, 0, 0);
			if (listParadataEvents.size() > 0) {
				for (int j = 0; j < listParadataEvents.size() - 1; j++) {
					// Entering a specific event
					Event event = listParadataEvents.get(j);
					Event previous_event = new Event();
					// We change the session values
					if (session.getIdentifier().contentEquals("Initialization ongoing")) {
						session.setIdentifier(event.getIdSession());
					}
					if (event.getIdParadataObject().contentEquals("init-session")) {
						if (session.getInitialization() == 0) {
							session.setInitialization((long) event.getTimestamp());
						} else {
							// End of current session, beginning of a new
							previous_event = listParadataEvents.get(j - 1);
							session.setTermination(previous_event.getTimestamp());
							this.addSession(session);
							session = new Session(event.getIdSession(), event.getTimestamp(), 0);
							// End of current orchestrator, beginning of a new
							orchestrator.setValidation((long) previous_event.getTimestamp());
							orchestrator.setDuree(orchestrator.getValidation() - orchestrator.getInitialization());
							this.addOrchestrator(orchestrator);
							orchestrator = new Orchestrator(identifier, 0, 0);
						}
					}
					// We change the orchestrator values
					if (orchestrator.getInitialization() == 0
							&& event.getIdParadataObject().contentEquals("init-orchestrator-collect")) {
						orchestrator.setInitialization((long) event.getTimestamp());
					} else if (orchestrator.getInitialization() != 0
							&& event.getIdParadataObject().contentEquals("init-orchestrator-collect")) {
						Session previous_session = this.getSessions().get(this.getSessions().size() - 1);
						if (previous_session.getTermination() != orchestrator.getInitialization()) {

							// By default, if a new orchestrator is declared, the previous one is terminated
							// at the last timestamp given in the last subParadata
							previous_event = listParadataEvents.get(j - 1);
							this.addOrchestrator(new Orchestrator(identifier, orchestrator.getInitialization(),
									(long) previous_event.getTimestamp()));
							orchestrator.setInitialization((long) event.getTimestamp());
						}
					} else if (event.getIdParadataObject().contentEquals("validate-button-orchestrator-collect")) {
						orchestrator.setValidation((long) event.getTimestamp());
						orchestrator.setDuree(orchestrator.getValidation() - orchestrator.getInitialization());
						this.addOrchestrator(orchestrator);
						orchestrator = new Orchestrator(identifier, 0, 0);
					}

					
				}

				Event event = listParadataEvents.get(listParadataEvents.size() - 1);
				if (session.getInitialization() != 0 && session.getTermination() == 0) {
					session.setTermination((long) event.getTimestamp());
					this.addSession(session);
				}
				if (orchestrator.getInitialization() != 0 && orchestrator.getValidation() == 0) {
					orchestrator.setValidation((long) event.getTimestamp());
					orchestrator.setDuree(orchestrator.getValidation() - orchestrator.getInitialization());
					this.addOrchestrator(orchestrator);
				}
			}
		
		
	}
}
