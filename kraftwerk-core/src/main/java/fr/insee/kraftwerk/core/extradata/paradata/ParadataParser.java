package fr.insee.kraftwerk.core.extradata.paradata;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import fr.insee.kraftwerk.core.Constants;
import fr.insee.kraftwerk.core.exceptions.NullException;
import fr.insee.kraftwerk.core.metadata.Variable;
import fr.insee.kraftwerk.core.metadata.VariableType;
import fr.insee.kraftwerk.core.metadata.VariablesMap;
import fr.insee.kraftwerk.core.rawdata.QuestionnaireData;
import fr.insee.kraftwerk.core.rawdata.SurveyRawData;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class ParadataParser {

	private String timestamp = "timestamp";

	private static final String NEW_VALUE = "newValue";
	
	private List<String> inputFields = Arrays.asList("RADIO", "CHECKBOX", "INPUT", "DATEPICKER");


	public void parseParadata(Paradata paradata, SurveyRawData surveyRawData) throws NullException {

		log.info("Paradata parser being implemented for Survey Unit : {} !",
				surveyRawData.getIdSurveyUnits().toString());
		Path filePath = paradata.getFilepath();
		if (filePath == null) 	throw new NullException("JSONFile not defined");

		if (!filePath.toString().contentEquals("")) {

			// Get all filepaths for each ParadataUE
			try (Stream<Path> walk = Files.walk(filePath)) {
				List<Path> listFilePaths = walk.filter(Files::isRegularFile)
						.filter(file -> surveyRawData.getIdSurveyUnits().contains(getIdFromFilename(file))).toList();
				// Parse each ParaDataUE
				List<ParaDataUE> listParaDataUE = new ArrayList<>();

				for (Path fileParaDataPath : listFilePaths) {
					ParaDataUE paraDataUE = new ParaDataUE();
					paraDataUE.setFilepath(fileParaDataPath);
					parseParadataUE(paraDataUE, surveyRawData);
					paraDataUE.sortEvents();
					if (paraDataUE.getEvents().size() > 2) {
						paraDataUE.createOrchestratorsAndSessions();
						integrateParaDataVariablesIntoUE(paraDataUE, surveyRawData);
						listParaDataUE.add(paraDataUE);
					}
				}
				paradata.setListParadataUE(listParaDataUE);
			} catch (Exception e) {
				log.error(e.getMessage());
			}
		}

	}

	private String getIdFromFilename(Path file) {
		String[] splitFilename = file.getFileName().toString().split("\\.");
		return splitFilename[splitFilename.length - 2];
	}

	private void parseParadataUE(ParaDataUE paradataUE, SurveyRawData surveyRawData) throws NullException {
		// To convert to a entire folder instead of a single file
		VariablesMap variablesMap = surveyRawData.getVariablesMap();
		JSONObject jsonObject = getParadataFromJson(paradataUE);
		// Get Identifier
		String identifier = (String) jsonObject.get("idSu");
		paradataUE.setIdentifier(identifier);

		// Now we get each event recorded
		ArrayList<Event> events = new ArrayList<>();
		JSONArray collectedEvents = (JSONArray) jsonObject.get("events");
		for (int i = 0; i < collectedEvents.size(); i++) {

			JSONArray subParadata = (JSONArray) collectedEvents.get(i);
			for (int j = 0; j < subParadata.size(); j++) {
				JSONObject collectedEvent = (JSONObject) subParadata.get(j);
				if (collectedEvent.containsKey("idOrchestrator")
						&& collectedEvent.get("idOrchestrator").equals("orchestrator-collect")) { //check that paradata are linked to collect (not vizualisation or readonly))

					Event event = new Event(identifier);
					event.setIdParadataObject((String) collectedEvent.get("idParadataObject"));
					event.setIdSession((String) collectedEvent.get("idSession"));
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
		}
		paradataUE.setEvents(events);
	}

	private JSONObject getParadataFromJson(ParaDataUE paradataUE) throws NullException {
		Path filePath = paradataUE.getFilepath();
		JSONObject jsonObject = null;
		try {
			jsonObject = (JSONObject) Constants.readJsonSimple(filePath);
		} catch (Exception e) {
			throw new NullException("Can't read JSON file - " + e.getClass() + " " + e.getMessage());
		}
		if (jsonObject == null)
			throw new NullException("Error reading file - NullPointer");
		return jsonObject;
	}

	private Object getValue(Object object) {
		if (object instanceof String) {
			return object;
		} else if (object instanceof Long) {
			return object.toString();
		} else if (object instanceof JSONArray jsonArray) {
			List<String> values = new ArrayList<>();
			for (int index = 0; index < jsonArray.size(); index++) {
				values.add((String) getValue(jsonArray.get(index)));
			}
			return values;
		} else if (object instanceof Integer) {
			// do what you want
		}

		return null;

	}

	/**
	 * Save paradata information in a variable hardcoded in the dataset
	 *
	 * @param paradataUE    the paradata
	 * @param surveyRawData dataset where the paradata will be saved
	 */
	private void integrateParaDataVariablesIntoUE(ParaDataUE paraDataUE, SurveyRawData surveyRawData) {
		VariablesMap variablesMap = surveyRawData.getVariablesMap();
		Set<String> paradataVariables = paraDataUE.getParaDataVariables().keySet();
		Variable variableDuree = new Variable(Constants.LENGTH_ORCHESTRATORS_NAME, variablesMap.getRootGroup(),
				VariableType.STRING, "30");
		Variable variableDureeBrute = new Variable(Constants.LENGTH_ORCHESTRATORS_NAME + "_LONG",
				variablesMap.getRootGroup(), VariableType.INTEGER, "20.");
		Variable variableDureeSession = new Variable(Constants.LENGTH_SESSIONS_NAME, variablesMap.getRootGroup(),
				VariableType.STRING, "30");
		Variable variableDureeSessionBrute = new Variable(Constants.LENGTH_SESSIONS_NAME + "_LONG",
				variablesMap.getRootGroup(), VariableType.INTEGER, "20.");
		Variable variableStart = new Variable(Constants.START_SESSION_NAME, variablesMap.getRootGroup(),
				VariableType.INTEGER, "20.");
		Variable variableEnd = new Variable(Constants.FINISH_SESSION_NAME, variablesMap.getRootGroup(),
				VariableType.INTEGER, "20.");
		Variable variableNbOrch = new Variable(Constants.NUMBER_ORCHESTRATORS_NAME, variablesMap.getRootGroup(),
				VariableType.INTEGER, "3.");
		Variable variableNbSessions = new Variable(Constants.NUMBER_SESSIONS_NAME, variablesMap.getRootGroup(),
				VariableType.INTEGER, "3.");
		
	//	Add variables to map : some variables are calculated but not used (variableDuree,variableDureeBrute, variableNbOrch)
		variablesMap.putVariable(variableDureeSession);
		variablesMap.putVariable(variableDureeSessionBrute);
		variablesMap.putVariable(variableNbSessions);
		variablesMap.putVariable(variableStart);
		variablesMap.putVariable(variableEnd);
		for (String variableName : paradataVariables) {
			if (variableName.contentEquals("PRENOM")) {
				Variable variable = new Variable(Constants.PARADATA_VARIABLES_PREFIX + variableName,
						variablesMap.getRootGroup(), VariableType.STRING, "3");
				variablesMap.putVariable(variable);
			}
		}

		if (!paraDataUE.getOrchestrators().isEmpty()) {
			long lengthOrchestrators = paraDataUE.createLengthOrchestratorsVariable();
			long lengthSessions = paraDataUE.createLengthSessionsVariable();

			QuestionnaireData questionnaire = surveyRawData.getQuestionnaires().stream()
					.filter(questionnaireToSearch -> paraDataUE.getOrchestrators().get(0).getIdentifier()
							.equals(questionnaireToSearch.getIdentifier()))
					.findAny().orElse(null);
			if (questionnaire != null) {
				questionnaire.getAnswers().putValue(variableDuree.getName(),Constants.convertToDateFormat(lengthOrchestrators));
				questionnaire.getAnswers().putValue(variableDureeBrute.getName(), Long.toString(lengthOrchestrators));
				questionnaire.getAnswers().putValue(variableDureeSession.getName(),Constants.convertToDateFormat(lengthSessions));
				questionnaire.getAnswers().putValue(variableDureeSessionBrute.getName(), Long.toString(lengthSessions));
				questionnaire.getAnswers().putValue(variableStart.getName(), paraDataUE.getVariableStart());
				questionnaire.getAnswers().putValue(variableEnd.getName(), paraDataUE.getVariableEnd());
				questionnaire.getAnswers().putValue(variableNbOrch.getName(), Long.toString(paraDataUE.getOrchestrators().size()));
				questionnaire.getAnswers().putValue(variableNbSessions.getName(), Long.toString(paraDataUE.getSessions().size()));
				for (String variableName : paradataVariables) {
					if (variableName.contentEquals("PRENOM")) {
						questionnaire.getAnswers().putValue(Constants.PARADATA_VARIABLES_PREFIX + variableName,
								String.valueOf(paraDataUE.getParadataVariable(variableName).size()));
					}
				}
			}
		}
	}

}
