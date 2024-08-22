package fr.insee.kraftwerk.core.extradata.paradata;

import fr.insee.kraftwerk.core.Constants;
import fr.insee.kraftwerk.core.exceptions.NullException;
import fr.insee.kraftwerk.core.metadata.Group;
import fr.insee.kraftwerk.core.metadata.Variable;
import fr.insee.kraftwerk.core.metadata.VariableType;
import fr.insee.kraftwerk.core.metadata.VariablesMap;
import fr.insee.kraftwerk.core.rawdata.QuestionnaireData;
import fr.insee.kraftwerk.core.rawdata.SurveyRawData;
import fr.insee.kraftwerk.core.utils.files.FileUtilsInterface;
import lombok.extern.log4j.Log4j2;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Stream;

@Log4j2
public class ParadataParser {

	private static final String NEW_VALUE = "newValue";

	private final List<String> inputFields = Arrays.asList("RADIO", "CHECKBOX", "INPUT", "DATEPICKER");

	private final FileUtilsInterface fileUtilsInterface;

	public ParadataParser(FileUtilsInterface fileUtilsInterface) {
		this.fileUtilsInterface = fileUtilsInterface;
	}

	public void parseParadata(Paradata paradata, SurveyRawData surveyRawData) throws NullException {

		log.info("Paradata parser being implemented for Survey Unit : {} !",
				surveyRawData.getIdSurveyUnits().toString());
		Path filePath = paradata.getFilepath();
		if (filePath == null)
			throw new NullException("JSONFile not defined");

		if (!filePath.toString().contentEquals("")) {
			try {
				// Parse each ParaDataUE
				List<ParaDataUE> listParaDataUE = new ArrayList<>();
				// Get all filepaths for each ParadataUE
				for (String fileParaDataPath : fileUtilsInterface.listFilePaths(filePath.toString()).stream().filter(
						s -> surveyRawData.getIdSurveyUnits().contains(getIdFromFilename(Path.of(s)))
				).toList()) {
					ParaDataUE paraDataUE = new ParaDataUE();
					paraDataUE.setFilepath(Path.of(fileParaDataPath));
					parseParadataUE(paraDataUE, surveyRawData);
					paraDataUE.sortEvents();
					paraDataUE.setSurveyValidationDateTimeStamp(Constants.PARADATA_SURVEY_VALIDATION_EVENT_NAME);
					if (paraDataUE.getEvents().size() > 2) {
						paraDataUE.createOrchestratorsAndSessions();
						integrateParaDataVariablesIntoUE(paraDataUE, surveyRawData);
						listParaDataUE.add(paraDataUE);
					}
				}
				paradata.setListParadataUE(listParaDataUE);
			} catch (NullException e){
				log.error("Error parsing paradata : {}", e.getMessage());
			}
		}
	}

	private String getIdFromFilename(Path file) {
		String[] splitFilename = file.getFileName().toString().split("\\.");
		return splitFilename[splitFilename.length - 2];
	}

	private void parseParadataUE(ParaDataUE paradataUE, SurveyRawData surveyRawData) throws NullException {
		// To convert to an entire folder instead of a single file
		VariablesMap variablesMap = surveyRawData.getMetadataModel().getVariables();
		JSONObject jsonObject = getParadataFromJson(paradataUE);
		// Get Identifier
		String identifier = (String) jsonObject.get("idSu");
		paradataUE.setIdentifier(identifier);

		// Now we get each event recorded
		ArrayList<Event> events = new ArrayList<>();
		JSONArray collectedEvents = (JSONArray) jsonObject.get("events");
		for (Object collectedEvent : collectedEvents) {
			JSONArray subParadata = (JSONArray) collectedEvent;
			for (int j = 0; j < subParadata.size(); j++) {
				parseEventFromParadataUE(paradataUE, variablesMap, identifier, events, subParadata, j);
			}
		}
		paradataUE.setEvents(events);
	}

	private void parseEventFromParadataUE(ParaDataUE paradataUE, VariablesMap variablesMap, String identifier,
			ArrayList<Event> events, JSONArray subParadata, int j) {
		JSONObject collectedEvent = (JSONObject) subParadata.get(j);
		if (isCollectedParadata(collectedEvent)) { // check that paradata are linked to collect (not visualisation or
													// readonly))

			Event event = new Event(identifier);
			event.setIdParadataObject((String) collectedEvent.get("idParadataObject"));
			event.setIdSession((String) collectedEvent.get("idSession"));
			String timestamp = "timestamp";
			event.setTimestamp((long) collectedEvent.get(timestamp));

			ParadataVariable paradataVariable = new ParadataVariable(identifier);
			paradataVariable.setTimestamp((long) collectedEvent.get(timestamp));
			paradataVariable.setValue(collectedEvent.get(NEW_VALUE));

			if (variablesMap.getVariableNames().contains(event.getIdParadataObject())) {
				paradataVariable.setVariableName(event.getIdParadataObject().toUpperCase());
				// Change value -> not String dependant
				Object newValue = getValue(new JSONObject(collectedEvent).get(NEW_VALUE));
				event.setValue(newValue);
				paradataVariable.setValue(newValue);
				paradataUE.addParadataVariable(paradataVariable);

			}
			if (inputFields.stream().anyMatch(event.getIdParadataObject().toUpperCase()::contains)) {
				paradataVariable.setVariableName((String) collectedEvent.get("responseName"));
				paradataUE.addParadataVariable(paradataVariable);
			}
			if (event.getIdParadataObject().toUpperCase().contains(Constants.FILTER_RESULT_PREFIX)) {
				paradataVariable.setVariableName(event.getIdParadataObject());
				paradataUE.addParadataVariable(paradataVariable);
			}
			events.add(event);
		}
	}

	private boolean isCollectedParadata(JSONObject collectedEvent) {
		return collectedEvent.containsKey("idOrchestrator")
				&& collectedEvent.get("idOrchestrator").equals("orchestrator-collect");
	}

	private JSONObject getParadataFromJson(ParaDataUE paradataUE) throws NullException {
		Path filePath = paradataUE.getFilepath();
		JSONObject jsonObject;
		try {
			jsonObject = (JSONObject) Constants.readJsonSimple(filePath, fileUtilsInterface);
		} catch (Exception e) {
			throw new NullException("Can't read JSON file - " + e.getClass() + " " + e.getMessage());
		}
		if (jsonObject == null)
			throw new NullException("Error reading file - NullPointer");
		return jsonObject;
	}

	private Object getValue(Object object) {
		if (object instanceof String ) {
			return object;
		} else if (object instanceof Long) {
			return object.toString();
		} else if (object instanceof JSONArray jsonArray) {
			List<String> values = new ArrayList<>();
			for (Object jsonValue : jsonArray) {
				values.add((String) getValue(jsonValue));
			}
			return values;
		} else if (object instanceof Integer) {
			return object;
		}

		return null;

	}

	/**
	 * Save paradata information in a variable hardcoded in the dataset
	 *
	 * @param paraDataUE    the paradata
	 * @param surveyRawData dataset where the paradata will be saved
	 */
	private void integrateParaDataVariablesIntoUE(ParaDataUE paraDataUE, SurveyRawData surveyRawData) {
		VariablesMap variablesMap = surveyRawData.getMetadataModel().getVariables();
		Group rootGroup = surveyRawData.getMetadataModel().getRootGroup();

		Set<String> paradataVariables = paraDataUE.getParaDataVariables().keySet();
		Variable variableDuree = new Variable(Constants.LENGTH_ORCHESTRATORS_NAME,rootGroup,
				VariableType.STRING, "30");
		Variable variableDureeBrute = new Variable(
				Constants.LENGTH_ORCHESTRATORS_NAME + Constants.PARADATA_TIMESTAMP_SUFFIX, rootGroup,
				VariableType.INTEGER, "20.");
		Variable variableDureeSession = new Variable(Constants.LENGTH_SESSIONS_NAME, rootGroup,
				VariableType.STRING, "30");
		Variable variableDureeSessionBrute = new Variable(
				Constants.LENGTH_SESSIONS_NAME + Constants.PARADATA_TIMESTAMP_SUFFIX, rootGroup,
				VariableType.INTEGER, "20.");
		Variable variableStart = new Variable(Constants.START_SESSION_NAME, rootGroup,
				VariableType.INTEGER, "20.");
		Variable variableEnd = new Variable(Constants.FINISH_SESSION_NAME, rootGroup,
				VariableType.INTEGER, "20.");
		Variable variableNbOrch = new Variable(Constants.NUMBER_ORCHESTRATORS_NAME, rootGroup,
				VariableType.INTEGER, "3.");
		Variable variableNbSessions = new Variable(Constants.NUMBER_SESSIONS_NAME, rootGroup,
				VariableType.INTEGER, "3.");
		Variable variableDateCollecteBrute = new Variable(
				Constants.SURVEY_VALIDATION_DATE_NAME + Constants.PARADATA_TIMESTAMP_SUFFIX,
				rootGroup, VariableType.STRING, "3");
		Variable variableDateCollecte = new Variable(Constants.SURVEY_VALIDATION_DATE_NAME, rootGroup,
				VariableType.STRING, "3");

		// Add variables to map : some variables are calculated but not used
		// (variableDuree,variableDureeBrute, variableNbOrch)
		variablesMap.putVariable(variableDureeSession);
		variablesMap.putVariable(variableDureeSessionBrute);
		variablesMap.putVariable(variableNbSessions);
		variablesMap.putVariable(variableStart);
		variablesMap.putVariable(variableEnd);
		variablesMap.putVariable(variableDateCollecte);
		variablesMap.putVariable(variableDateCollecteBrute);
		for (String variableName : paradataVariables) {
			if (variableName.contentEquals("PRENOM")) {
				Variable variable = new Variable(Constants.PARADATA_VARIABLES_PREFIX + variableName,
						rootGroup, VariableType.STRING, "3");
				variablesMap.putVariable(variable);
			}
		}

		if (paraDataUE.getOrchestrators().isEmpty()) return;
		
		long lengthOrchestrators = paraDataUE.createLengthOrchestratorsVariable();
		long lengthSessions = paraDataUE.createLengthSessionsVariable();

		QuestionnaireData questionnaire = surveyRawData.getQuestionnaires().stream()
				.filter(questionnaireToSearch -> paraDataUE.getOrchestrators().getFirst().getIdentifier()
						.equals(questionnaireToSearch.getIdentifier()))
				.findAny().orElse(null);
		
		if (questionnaire == null)	return;
		
		questionnaire.getAnswers().putValue(variableDuree.getName(),Constants.convertToDateFormat(lengthOrchestrators));
		questionnaire.getAnswers().putValue(variableDureeBrute.getName(), Long.toString(lengthOrchestrators));
		questionnaire.getAnswers().putValue(variableDureeSession.getName(),
				Constants.convertToDateFormat(lengthSessions));
		questionnaire.getAnswers().putValue(variableDureeSessionBrute.getName(), Long.toString(lengthSessions));
		questionnaire.getAnswers().putValue(variableStart.getName(), paraDataUE.getVariableStart());
		questionnaire.getAnswers().putValue(variableEnd.getName(), paraDataUE.getVariableEnd());
		questionnaire.getAnswers().putValue(variableNbOrch.getName(),
				Long.toString(paraDataUE.getOrchestrators().size()));
		questionnaire.getAnswers().putValue(variableNbSessions.getName(),
				Long.toString(paraDataUE.getSessions().size()));
		questionnaire.getAnswers().putValue(variableDateCollecte.getName(),
				LocalDateTime.ofInstant(Instant.ofEpochMilli(paraDataUE.getSurveyValidationDateTimeStamp()),
						TimeZone.getDefault().toZoneId()).toString());
		questionnaire.getAnswers().putValue(variableDateCollecteBrute.getName(),
				Long.toString(paraDataUE.getSurveyValidationDateTimeStamp()));
		for (String variableName : paradataVariables) {
			if (variableName.contentEquals("PRENOM")) {
				questionnaire.getAnswers().putValue(Constants.PARADATA_VARIABLES_PREFIX + variableName,
						String.valueOf(paraDataUE.getParadataVariable(variableName).size()));
			}
		}

	}

}
