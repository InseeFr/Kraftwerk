package fr.insee.kraftwerk.core.extradata.paradata;

import lombok.Getter;
import lombok.Setter;

import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

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
	@Getter
	private List<Session> sessions = new ArrayList<>();
	@Getter
	private List<Orchestrator> orchestrators = new ArrayList<>();
	@Getter
	private long surveyValidationDateTimeStamp;

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
		return getOrchestrators().stream().mapToLong(Orchestrator::getDuration).sum();
	}
	

	public long createLengthSessionsVariable() {
		return getSessions().stream().mapToLong(Session::getDuration).sum();
	}

	public String getVariableStart() {
		if (getSessions().isEmpty()) { return "0";}
		return Long.toString(getSessions().getFirst().getInitialization());
	}
	
	public String getVariableEnd() {
		if (getSessions().isEmpty()) { return "0";}
		return Long.toString(getLastSession().getTermination());
	}

	private Session getLastSession() {
		return getSessions().getLast();
	}

	public void createOrchestratorsAndSessions() {
		List<Event> listParadataEvents = getEvents();

		// initialize
		Session session = new Session(INITIALIZATION_ONGOING);
		Orchestrator orchestrator = new Orchestrator(identifier);

		if (checkNoSessionToCreate(listParadataEvents)) {return;}
		Event previousEvent = null ;
		
		// iterate on paradata events
		for (Event currentEvent : listParadataEvents) {
			initializeSessionIdentifier(session, currentEvent);
			String idParadataObject = currentEvent.getIdParadataObject();

			switch (idParadataObject) {
				case "init-session":
					session = initializeOrChangeSession(session, previousEvent, currentEvent);
					if (isOrchestratorStartBeforePreviousEvent(orchestrator, previousEvent)) {
						orchestrator = changeCurrentOrchestrator(orchestrator, previousEvent);
					}
					break;

				case "init-orchestrator-collect":
					orchestrator = initializeOrChangeOrchestrator(orchestrator, previousEvent, currentEvent);
					break;
				case "agree-sending-modal-button-orchestrator-collect":
					//validate the modal popup
					initOrchestratorWithSessionIfNeeded(session, orchestrator);
					if (orchestrator.getInitialization() < currentEvent.getTimestamp()) {
						orchestrator = changeCurrentOrchestrator(orchestrator, currentEvent);
						orchestrator.setInitialization(currentEvent.getTimestamp());
					}
					break;
				case "logout-close-button-orchestrator-collect":
					initOrchestratorWithSessionIfNeeded(session, orchestrator);
					if (orchestrator.getInitialization() < currentEvent.getTimestamp()) {
						orchestrator = changeCurrentOrchestrator(orchestrator, currentEvent);
					}
					break;
				default:
					break;
			}

			previousEvent = currentEvent;
		}
		Event event = listParadataEvents.get(listParadataEvents.size() - 1);
		closeLastSession(session, event);
		closeLastOrchestrator(orchestrator, event);

	}

	private Orchestrator initializeOrChangeOrchestrator(Orchestrator orchestrator, Event previousEvent,
			Event currentEvent) {
		if (isOrchestratorStartBeforePreviousEvent(orchestrator, previousEvent)) {
				orchestrator = changeCurrentOrchestrator(orchestrator, previousEvent);
		}
		orchestrator.setInitialization(currentEvent.getTimestamp());
		return orchestrator;
	}

	private Session initializeOrChangeSession(Session session, Event previousEvent, Event currentEvent) {
		if (session.getInitialization() == 0L) {
			session.setInitialization(currentEvent.getTimestamp());
		} else {
			session = changeCurrentSession(session, currentEvent, previousEvent);
		}
		return session;
	}

	private boolean checkNoSessionToCreate(List<Event> listParadataEvents) {
		return listParadataEvents.isEmpty() || listParadataEvents.size()==1;
	}

	private void initializeSessionIdentifier(Session session, Event currentEvent) {
		if (session.getIdentifier().contentEquals(INITIALIZATION_ONGOING)) {// idSession was never set
			session.setIdentifier(currentEvent.getIdSession());
		}
	}

	private void closeLastOrchestrator(Orchestrator orchestrator, Event event) {
		if (isOrchestratorStartButNotFinish(orchestrator)&& orchestrator.getInitialization() < event.getTimestamp()) { //close last orchestrator
			orchestrator.setValidation(event.getTimestamp());
			addOrchestrator(orchestrator);
		}
	}

	private void closeLastSession(Session session, Event event) {
		if (isSessionStartButNotFinish(session)) { //close last session
			session.setTermination(event.getTimestamp());
			addSession(session);
		}
	}

	private void initOrchestratorWithSessionIfNeeded(Session session, Orchestrator orchestrator) {
		if (orchestrator.getInitialization() == 0L) {
				orchestrator.setInitialization(session.getInitialization());
		}
	}

	private boolean isOrchestratorStartBeforePreviousEvent(Orchestrator orchestrator, Event previousEvent) {
		if (previousEvent == null) return false;
		return orchestrator.getInitialization() != 0L	&& orchestrator.getInitialization() < previousEvent.getTimestamp();
	}

	private boolean isOrchestratorStartButNotFinish(Orchestrator orchestrator) {
		return orchestrator.getInitialization() != 0L && orchestrator.getValidation() == 0L;
	}

	private boolean isSessionStartButNotFinish(Session session) {
		return session.getInitialization() != 0L && session.getTermination() == 0L;
	}

	private Orchestrator changeCurrentOrchestrator(Orchestrator orchestrator, Event previousEvent) {
		orchestrator.setValidation(previousEvent.getTimestamp());
		addOrchestrator(orchestrator);
		orchestrator = new Orchestrator(identifier);
		return orchestrator;
	}

	private Session changeCurrentSession(Session session, Event currentEvent, Event previousEvent) {
		if (previousEvent == null) return session;
		session.setTermination(previousEvent.getTimestamp());
		addSession(session);
		session = new Session(currentEvent.getIdSession(), currentEvent.getTimestamp(), 0L);
		return session;
	}

	/**
	 * Gets the validation timestamp from events and put it into the surveyValidationDateTimeStamp
	 * @param validationParadataObjectId idParadataObject considered as validation event
	 */
	public void setSurveyValidationDateTimeStamp(String validationParadataObjectId) {
		long maxTimestamp = 0;
		for(Event event : this.events){
			if(event.getIdParadataObject().equals(validationParadataObjectId) && event.getTimestamp() >= maxTimestamp) {
				this.surveyValidationDateTimeStamp = event.getTimestamp();
				maxTimestamp = event.getTimestamp();
			}
		}
	}
}
