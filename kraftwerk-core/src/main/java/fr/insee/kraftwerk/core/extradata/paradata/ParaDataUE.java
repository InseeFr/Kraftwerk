package fr.insee.kraftwerk.core.extradata.paradata;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import lombok.Getter;
import lombok.Setter;

public class ParaDataUE {
	private static final String INITIALIZATION_ONGOING = "Initialization ongoing";
	@Getter
	@Setter
	private Path filepath;
	@Getter
	@Setter
	private String identifier;
	@Getter
	@Setter
	private List<Event> events = new ArrayList<>();
	@Getter
	private Map<String, List<ParadataVariable>> paraDataVariables = new LinkedHashMap<>();

	private List<ParadataOrchestrator> paraDataOrchestrators = new ArrayList<>();

	private List<Event> paraDataSessions = new ArrayList<>();
	@Getter
	private List<Session> sessions = new ArrayList<>();
	@Getter
	private List<Orchestrator> orchestrators = new ArrayList<>();

	public List<ParadataVariable> getParadataVariable(String variableName) {
		return this.paraDataVariables.get(variableName);
	}

	public void addParadataVariable(ParadataVariable paraDataVariable) {
		String variableName = paraDataVariable.getVariableName();
		if (this.paraDataVariables.containsKey(variableName)) {
			this.paraDataVariables.get(variableName).add(paraDataVariable);
		} else {
			this.paraDataVariables.put(variableName, new ArrayList<>());
			this.paraDataVariables.get(variableName).add(paraDataVariable);
		}
	}

	public void addParadataOrchestrator(ParadataOrchestrator paraDataOrchestrator) {
		this.paraDataOrchestrators.add(paraDataOrchestrator);
	}

	public void addParadataSession(Event paraDataSession) {
		this.paraDataSessions.add(paraDataSession);
	}

	public void addSession(Session session) {
		this.sessions.add(session);
	}

	public void addOrchestrator(Orchestrator orchestrator) {
		this.orchestrators.add(orchestrator);
	}

	public void sortEvents() {
		this.setEvents(this.getEvents().stream().distinct()
				.sorted(Comparator.comparingLong(Event::getTimestamp).thenComparing(Event::getIdParadataObject))
				.collect(Collectors.toList()));
	}

	public long createLengthOrchestratorsVariable() {
		return getOrchestrators().stream().mapToLong(orchestrator -> orchestrator.getDuration()).sum();
	}
	

	public String getVariableStart() {
		return Long.toString(getSessions().get(0).getInitialization());
	}
	
	public String getVariableEnd() {
		return Long.toString(getLastSession().getTermination());
	}

	private Session getLastSession() {
		return getSessions().get(getSessions().size()-1);
	}

	public void createOrchestratorsAndSessions() {
		List<Event> listParadataEvents = getEvents();

		// initialize
		Session session = new Session(INITIALIZATION_ONGOING);
		Orchestrator orchestrator = new Orchestrator(identifier);

		if (listParadataEvents.isEmpty()) 	return;
		Event previousEvent = null ;
		
		// iterate on paradata events
		for (int j = 0; j < listParadataEvents.size(); j++) {
			Event currentEvent = listParadataEvents.get(j);

			if (session.getIdentifier().contentEquals(INITIALIZATION_ONGOING)) {// idSession was never set
				session.setIdentifier(currentEvent.getIdSession());
			}
			String idParadataObject = currentEvent.getIdParadataObject();
			
			switch (idParadataObject) {
			case "init-session":
				if (session.getInitialization() == 0L) {
					session.setInitialization(currentEvent.getTimestamp());
				} else {
					session = changeCurrentSession(session, currentEvent, previousEvent);
					if (orchestrator.getInitialization() != 0L	&& orchestrator.getInitialization() < previousEvent.getTimestamp()) {
						orchestrator = changeCurrentOrchestrator(orchestrator, previousEvent);
					}
				}
				break;
			
			case "init-orchestrator-collect" :
				if (orchestrator.getInitialization() != 0L) {
						orchestrator = changeCurrentOrchestrator(orchestrator, previousEvent);
				}
				orchestrator.setInitialization(currentEvent.getTimestamp());
				break;
			//"agree-sending-modal-button" => when click on confirm in popup
			case "validate-button-orchestrator-collect" :
				if (orchestrator.getInitialization() == 0L) {
					if (getSessions().isEmpty()) {
						orchestrator.setInitialization(session.getInitialization());
					} else {
						orchestrator.setInitialization(getLastSession().getInitialization());
					}
				}
				if (orchestrator.getInitialization() < previousEvent.getTimestamp()) {
					orchestrator.setValidation(currentEvent.getTimestamp());
					addOrchestrator(orchestrator);
				}
				orchestrator = new Orchestrator(identifier);
				break;
				
			default:
				break;
			}
			
			previousEvent = currentEvent;
		}
		Event event = listParadataEvents.get(listParadataEvents.size() - 1);
		if (session.getInitialization() != 0L && session.getTermination() == 0L) { //close last session
			session.setTermination(event.getTimestamp());
			addSession(session);
		}
		if (orchestrator.getInitialization() != 0L && orchestrator.getValidation() == 0L
				&& orchestrator.getInitialization() < event.getTimestamp()) { //close last orchestrator
			orchestrator.setValidation(event.getTimestamp());
			addOrchestrator(orchestrator);
		}

	}

	private Orchestrator changeCurrentOrchestrator(Orchestrator orchestrator, Event previousEvent) {
		orchestrator.setValidation(previousEvent.getTimestamp());
		addOrchestrator(orchestrator);
		orchestrator = new Orchestrator(identifier);
		return orchestrator;
	}

	private Session changeCurrentSession(Session session, Event currentEvent, Event previousEvent) {
		session.setTermination(previousEvent.getTimestamp());
		addSession(session);
		session = new Session(currentEvent.getIdSession(), currentEvent.getTimestamp(), 0L);
		return session;
	}
}
