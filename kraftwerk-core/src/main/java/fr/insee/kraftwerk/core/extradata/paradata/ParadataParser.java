package fr.insee.kraftwerk.core.extradata.paradata;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
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
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ParadataParser {

	private String timestamp = "timestamp";

	private static final String NEW_VALUE="newValue";

	public void parseParadata(Paradata paradata, SurveyRawData surveyRawData) throws NullException {

		log.info("Paradata parser being implemented!");
		Path filePath = paradata.getFilepath();
		if (!filePath.toString().contentEquals("")) {

			// Get all filepaths for each ParadataUE
			try (Stream<Path> walk = Files.walk(filePath)) {
				List<Path> listFilePaths = walk.filter(Files::isRegularFile).toList();
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
		ArrayList<Event> events = new ArrayList<>();
		JSONArray collectedEvents = (JSONArray) jsonObject.get("events");
		for (int i = 0; i < collectedEvents.size(); i++) {

			JSONArray subParadata = (JSONArray) collectedEvents.get(i);

			for (int j = 0; j < subParadata.size(); j++) {
				Event event = new Event(identifier);
				JSONObject collectedEvent = (JSONObject) subParadata.get(j);
				event.setIdParadataObject((String) collectedEvent.get("idParadataObject"));
				event.setIdSession((String) collectedEvent.get("idSession"));
				event.setTimestamp((long) collectedEvent.get(timestamp));
				ParadataVariable paradataVariable = new ParadataVariable(identifier);
				ParadataOrchestrator paradataOrchestrator = new ParadataOrchestrator(identifier, event.getIdSession());
				Event paradataSession = new Event(identifier, event.getIdSession());
				
				if (variablesMap.getVariableNames().contains(event.getIdParadataObject())) {
					paradataVariable.setVariableName(event.getIdParadataObject().toUpperCase());
					JSONObject jsonObj = new JSONObject(collectedEvent);
					// Change value -> not String dependant

					event.setValue(getValue(jsonObj.get(NEW_VALUE)));
					paradataVariable.setValue(event.getValue());
					paradataVariable.setTimestamp((long) collectedEvent.get(timestamp));
					paradataUE.addParadataVariable(paradataVariable);

				} else if (event.getIdParadataObject().toUpperCase().contains("RADIO")
						|| event.getIdParadataObject().toUpperCase().contains("CHECKBOX")
						|| event.getIdParadataObject().toUpperCase().contains("INPUT")
						|| event.getIdParadataObject().toUpperCase().contains("DATEPICKER")) {
					paradataVariable.setVariableName((String) collectedEvent.get("responseName"));
					paradataVariable.setTimestamp((long) collectedEvent.get(timestamp));
					paradataVariable.setValue(collectedEvent.get(NEW_VALUE));
					paradataUE.addParadataVariable(paradataVariable);

				} else if (event.getIdParadataObject().toUpperCase().contains("ORCHESTRATOR")) {
					paradataOrchestrator.setTimestamp((long) collectedEvent.get(timestamp));
					paradataOrchestrator.setObjectName((String) collectedEvent.get("idParadataObject"));
					paradataUE.addParadataOrchestrator(paradataOrchestrator);

				} else if (event.getIdParadataObject().toUpperCase().contains("SESSION")) {
					paradataSession.setTimestamp((long) collectedEvent.get(timestamp));
					paradataUE.addParadataSession(paradataSession);

				} else {
					if (event.getIdParadataObject().contains(Constants.FILTER_RESULT_PREFIX)) {
						paradataVariable.setVariableName(event.getIdParadataObject());
						paradataVariable.setTimestamp((long) collectedEvent.get(timestamp));
						paradataVariable.setValue(collectedEvent.get(NEW_VALUE));
						paradataUE.addParadataVariable(paradataVariable);
					}
				}
			
				events.add(event);
			}
		}
		paradataUE.setEvents(events);
	}

	public Object getValue(Object object) {
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
	public void integrateParaDataVariablesIntoUE(ParaDataUE paraDataUE, SurveyRawData surveyRawData){
		VariablesMap variablesMap = surveyRawData.getVariablesMap();
		Set<String> paradataVariables = paraDataUE.getParaDataVariables().keySet();
		Variable variableDuree = new Variable(Constants.LENGTH_ORCHESTRATORS_NAME, variablesMap.getRootGroup(),
				VariableType.STRING, "30");
		Variable variableDureeBrute = new Variable(Constants.LENGTH_ORCHESTRATORS_NAME + "_LONG",
				variablesMap.getRootGroup(), VariableType.INTEGER, "20.");
		Variable variableStart = new Variable(Constants.START_SESSION_NAME, variablesMap.getRootGroup(),
				VariableType.INTEGER, "20.");
		Variable variableEnd = new Variable(Constants.FINISH_SESSION_NAME, variablesMap.getRootGroup(),
				VariableType.INTEGER, "20.");
		Variable variableNombre = new Variable(Constants.NUMBER_ORCHESTRATORS_NAME, variablesMap.getRootGroup(),
				VariableType.INTEGER, "3.");
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
