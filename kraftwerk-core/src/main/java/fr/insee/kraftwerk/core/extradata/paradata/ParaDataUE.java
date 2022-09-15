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
		long result = 0L;
		for (Orchestrator orchestrator : getOrchestrators())
			result += orchestrator.getValidation() - orchestrator.getInitialization();
		return result;
	}

	public void createOrchestratorsAndSessions() {
		List<Event> listParadataEvents = getEvents();

		// initialize
		Session session = new Session("Initialization ongoing", 0L, 0L);
		Orchestrator orchestrator = new Orchestrator(identifier, 0L, 0L);

		// iterate on paradata events in reverse order
		if (!listParadataEvents.isEmpty()) {
			for (int j = 0; j < listParadataEvents.size() - 1; j++) {
				Event event1 = listParadataEvents.get(j);
				Event previousEvent;
				if (session.getIdentifier().contentEquals("Initialization ongoing")) {//idSession was never set
					session.setIdentifier(event1.getIdSession());
				}
				if (event1.getIdParadataObject().contentEquals("init-session")) {
					if (session.getInitialization() == 0L) {
						session.setInitialization(event1.getTimestamp());
					} else {
						previousEvent = listParadataEvents.get(j - 1);
						session.setTermination(previousEvent.getTimestamp());
						addSession(session);
						session = new Session(event1.getIdSession(), event1.getTimestamp(), 0L);
						if (orchestrator.getInitialization() != 0L
								&& orchestrator.getInitialization() < previousEvent.getTimestamp()) {
							orchestrator.setValidation(previousEvent.getTimestamp());
							addOrchestrator(orchestrator);
							orchestrator = new Orchestrator(identifier, 0L, 0L);
						}
					}
				}
				if (orchestrator.getInitialization() == 0L
						&& event1.getIdParadataObject().contentEquals("init-orchestrator-collect")) {
					orchestrator.setInitialization(event1.getTimestamp());
				} else if (orchestrator.getInitialization() != 0L
						&& event1.getIdParadataObject().contentEquals("init-orchestrator-collect")) {
					Session previousSession = getSessions().get(getSessions().size() - 1);
					if (previousSession.getTermination() != orchestrator.getInitialization()
							&& orchestrator.getInitialization() != 0L) {
						previousEvent = listParadataEvents.get(j - 1);
						addOrchestrator(new Orchestrator(identifier, orchestrator.getInitialization(),
								previousEvent.getTimestamp()));
						orchestrator.setInitialization(event1.getTimestamp());
					}
				} else if (event1.getIdParadataObject().contentEquals("validate-button-orchestrator-collect")) {
					previousEvent = listParadataEvents.get(j - 1);
					if (orchestrator.getInitialization() == 0L) {
						if (getSessions().isEmpty()) {
							orchestrator.setInitialization(session.getInitialization());
						} else {
							orchestrator.setInitialization(
									(getSessions().get(getSessions().size() - 1)).getInitialization());
						}
					}
					if (orchestrator.getInitialization() < previousEvent.getTimestamp()) {
						orchestrator.setValidation(event1.getTimestamp());
						addOrchestrator(orchestrator);
					}
					orchestrator = new Orchestrator(identifier, 0L, 0L);
				}
			}
			Event event = listParadataEvents.get(listParadataEvents.size() - 1);
			if (session.getInitialization() != 0L && session.getTermination() == 0L) {
				session.setTermination(event.getTimestamp());
				addSession(session);
			}
			if (orchestrator.getInitialization() != 0L && orchestrator.getValidation() == 0L
					&& orchestrator.getInitialization() < event.getTimestamp()) {
				orchestrator.setValidation(event.getTimestamp());
				addOrchestrator(orchestrator);
			}
		}
	}
}
