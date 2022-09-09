package fr.insee.kraftwerk.core.extradata.paradata;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
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
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ParadataParser {

	private String timestamp = "timestamp";

	public void parseParadata(Paradata paradata, SurveyRawData surveyRawData) throws NullException {

		log.info("Paradata parser being implemented!");
		Path filePath = paradata.getFilepath();
		if (!filePath.toString().contentEquals("")) {

			// Get all filepaths for each ParadataUE
			try (Stream<Path> walk = Files.walk(filePath)) {
				List<Path> listFilePaths = walk.filter(Files::isRegularFile)
												.collect(Collectors.toList());
				// Parse each ParaDataUE
				List<ParaDataUE> listParaDataUE = new ArrayList<>();

				for (Path fileParaDataPath : listFilePaths) {
					ParaDataUE paraDataUE = new ParaDataUE();
					paraDataUE.setFilepath(fileParaDataPath);
					parseParadataUE(paraDataUE, surveyRawData);
					paraDataUE.sortEvents();			
					if (paraDataUE.getEvents().size() > 2) {
						paraDataUE.createOrchestratorsAndSessions();
						try {
							integrateParaDataVariablesIntoUE(paraDataUE, surveyRawData);
						} catch (Exception e) {
							log.error(e.getMessage());
						}
						listParaDataUE.add(paraDataUE);
					}
				}
				paradata.setListParadataUE(listParaDataUE);
			} catch (IOException e) {
				log.error(e.getMessage());
			}
		}

	}

	public void parseParadataUE(ParaDataUE paradataUE, SurveyRawData surveyRawData) throws NullException {
		// To convert to a entire folder instead of a single file
		Path filePath = paradataUE.getFilepath();
		VariablesMap variablesMap = surveyRawData.getVariablesMap();
		JSONObject jsonObject = null;
		try {
			jsonObject = (JSONObject) Constants.readJsonSimple(filePath);
		} catch (Exception e) {
			throw new NullException("Can't read JSON file - "+e.getClass()+" "+ e.getMessage());
		}
		if (jsonObject== null) throw new NullException("Error reading file - NullPointer");
		// Get Identifier
		String identifier = (String) jsonObject.get("idSu");
		paradataUE.setIdentifier(identifier);

		// Now we get each event recorded
		ArrayList<Event> events = new ArrayList<Event>();
		JSONArray collected_events = (JSONArray) jsonObject.get("events");
		for (int i = 0; i < collected_events.size(); i++) {

			JSONArray subParadata = (JSONArray) collected_events.get(i);

			for (int j = 0; j < subParadata.size(); j++) {
				Event event = new Event(identifier);
				JSONObject collected_event = (JSONObject) subParadata.get(j);
				event.setIdParadataObject((String) collected_event.get("idParadataObject"));
				event.setIdSession((String) collected_event.get("idSession"));
				event.setTimestamp((long) collected_event.get(timestamp));
				ParadataVariable paradataVariable = new ParadataVariable(identifier, event.getIdSession());
				ParadataOrchestrator paradataOrchestrator = new ParadataOrchestrator(identifier, event.getIdSession());
				ParadataSession paradataSession = new ParadataSession(identifier, event.getIdSession());
				
				if (variablesMap.getVariableNames().contains(event.getIdParadataObject())) {
					paradataVariable.setVariableName(event.getIdParadataObject().toUpperCase());
					JSONObject jsonObj = new JSONObject(collected_event);
					// Change value -> not String dependant

					event.setValue(getValue(jsonObj.get("newValue")));
					paradataVariable.setValue(event.getValue());
					paradataVariable.setTimestamp((long) collected_event.get(timestamp));
					paradataUE.addParadataVariable(paradataVariable);

				} else if (event.getIdParadataObject().toUpperCase().contains("RADIO")
						|| event.getIdParadataObject().toUpperCase().contains("CHECKBOX")
						|| event.getIdParadataObject().toUpperCase().contains("INPUT")
						|| event.getIdParadataObject().toUpperCase().contains("DATEPICKER")) {
					paradataVariable.setVariableName((String) collected_event.get("responseName"));
					paradataVariable.setTimestamp((long) collected_event.get(timestamp));
					paradataVariable.setValue(collected_event.get("newValue"));
					paradataUE.addParadataVariable(paradataVariable);

				} else if (event.getIdParadataObject().toUpperCase().contains("ORCHESTRATOR")) {
					paradataOrchestrator.setTimestamp((long) collected_event.get(timestamp));
					paradataOrchestrator.setObjectName((String) collected_event.get("idParadataObject"));
					paradataUE.addParadataOrchestrator(paradataOrchestrator);

				} else if (event.getIdParadataObject().toUpperCase().contains("SESSION")) {
					paradataSession.setTimestamp((long) collected_event.get(timestamp));
					paradataUE.addParadataSession(paradataSession);

				} else {
					if (event.getIdParadataObject().contains(Constants.FILTER_RESULT_PREFIX)) {
						String variableName = event.getIdParadataObject()
								.substring(Constants.FILTER_RESULT_PREFIX.length());
						if (!variablesMap.getVariableNames().contains(variableName)
								&& !variablesMap.getUcqVariablesNames().contains(variableName)
								&& !variablesMap.getMcqVariablesNames().contains(variableName)) {
							// log.warn("Unexpected paradata (unknown variable " + variableName + ") with
							// SurveyUnit " + event.getIdSurveyUnit() + " at timestamp "
							// + collected_event.get(timestamp));

						}
						paradataVariable.setVariableName(event.getIdParadataObject());
						paradataVariable.setTimestamp((long) collected_event.get(timestamp));
						paradataVariable.setValue(collected_event.get("newValue"));
						paradataUE.addParadataVariable(paradataVariable);

					} else {
						// log.warn("Unexpected paradata (unknown variable " +
						// event.getIdParadataObject() + ") with SurveyUnit " + event.getIdSurveyUnit()
						// + " at timestamp "
						// + collected_event.get(timestamp));
					}
				}
			
				events.add(event);
			}
		}
		paradataUE.setEvents(events);
	}

	public Object getValue(Object object) {
		if (object instanceof String) {
			return (String) object;
		} else if (object instanceof Long) {
			return (String) object.toString();
		} else if (object instanceof JSONArray) {
			JSONArray jsonArray = (JSONArray) object;
			List<String> values = new ArrayList<String>();
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
	public void integrateParaDataVariablesIntoUE(ParaDataUE paraDataUE, SurveyRawData surveyRawData) throws Exception {
		VariablesMap variablesMap = surveyRawData.getVariablesMap();
		Set<String> paradataVariables = paraDataUE.getParadataVariables().keySet();
		Variable variableDuree = new Variable(Constants.LENGTH_ORCHESTRATORS_NAME, variablesMap.getRootGroup(),
				VariableType.STRING, "30");
		Variable variableDureeBrute = new Variable(Constants.LENGTH_ORCHESTRATORS_NAME + "_LONG",
				variablesMap.getRootGroup(), VariableType.INTEGER, "20");
		Variable variableStart = new Variable(Constants.START_SESSION_NAME, variablesMap.getRootGroup(),
				VariableType.INTEGER, "20");
		Variable variableEnd = new Variable(Constants.FINISH_SESSION_NAME, variablesMap.getRootGroup(),
				VariableType.INTEGER, "20");
		Variable variableNombre = new Variable(Constants.NUMBER_ORCHESTRATORS_NAME, variablesMap.getRootGroup(),
				VariableType.INTEGER, "3");
		try {
			variablesMap.putVariable(variableDuree);
			variablesMap.putVariable(variableDureeBrute);
			variablesMap.putVariable(variableNombre);
			variablesMap.putVariable(variableStart);
			variablesMap.putVariable(variableEnd);
			for (String variableName : paradataVariables) {
				if (variableName.contentEquals("PRENOM")) {
					Variable variable = new Variable(Constants.PARADATA_VARIABLES_PREFIX + variableName,
							variablesMap.getRootGroup(), VariableType.STRING, "3");
					variablesMap.putVariable(variable);
				}
			}
		} catch (Exception e) {
			// TODO: handle exception
		}

		if (!paraDataUE.getOrchestrators().isEmpty()) {
			long lengthOrchestrators = paraDataUE.createLengthOrchestratorsVariable();
			QuestionnaireData questionnaire = surveyRawData.getQuestionnaires().stream()
					.filter(questionnaireToSearch -> paraDataUE.getOrchestrators().get(0).getIdentifier()
							.equals(questionnaireToSearch.getIdentifier()))
					.findAny().orElse(null);
			if (questionnaire != null) {
				questionnaire.getAnswers().putValue(variableDuree.getName(),
						Constants.convertToDateFormat(lengthOrchestrators));
				questionnaire.getAnswers().putValue(variableDureeBrute.getName(), Long.toString(lengthOrchestrators));
				questionnaire.getAnswers().putValue(variableStart.getName(), Long.toString(paraDataUE.getSessions().get(0).getInitialization()));
				questionnaire.getAnswers().putValue(variableEnd.getName(), Long.toString(paraDataUE.getSessions().get(paraDataUE.getSessions().size()-1).getTermination()));
				questionnaire.getAnswers().putValue(variableNombre.getName(),
						Long.toString(paraDataUE.getOrchestrators().size()));
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
