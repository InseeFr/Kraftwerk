package fr.insee.kraftwerk.core.extradata.paradata;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import fr.insee.kraftwerk.core.Constants;
import fr.insee.kraftwerk.core.metadata.Variable;
import fr.insee.kraftwerk.core.metadata.VariableType;
import fr.insee.kraftwerk.core.rawdata.QuestionnaireData;
import fr.insee.kraftwerk.core.rawdata.SurveyRawData;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ParadataParser {

	private String timestamp = "timestamp";

	public void parseParadata(Paradata paradata, SurveyRawData surveyRawData) {

		log.info("Para data parser being implemented!");
		Path filePath = paradata.getFilepath();
		if (!filePath.toString().contentEquals("")) {

			// Get all filepaths for each ParadataUE
			try (Stream<Path> walk = Files.walk(filePath)) {
				List<String> listFilePaths = walk.filter(Files::isRegularFile).map(x -> x.toString())
						.collect(Collectors.toList());
				// Parse each ParaDataUE
				List<ParaDataUE> listParaDataUE = new ArrayList<ParaDataUE>();

				for (String fileParaDataPath : listFilePaths) {
					ParaDataUE paraDataUE = new ParaDataUE();
					paraDataUE.setFilepath(Paths.get(fileParaDataPath));
					parseParadataUE(paraDataUE, surveyRawData);
					paraDataUE.sortEvents();
					paraDataUE.createOrchestratorsAndSessions();
					try {
						integrateParaDataVariablesIntoUE(paraDataUE, surveyRawData);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					listParaDataUE.add(paraDataUE);

				}
				paradata.setListParadataUE(listParaDataUE);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}

	public void parseParadataUE(ParaDataUE paradataUE, SurveyRawData surveyRawData) {
		// To convert to a entire folder instead of a single file
		Path filePath = paradataUE.getFilepath();

		JSONObject jsonObject = null;
		try {
			jsonObject = (JSONObject) Constants.readJsonSimple(filePath);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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
				if (surveyRawData.getVariablesMap().getVariables().containsKey(event.getIdParadataObject().toUpperCase())) {
					/*
					 * For now, only the PRENOM variable is getting the treatment -> generalize
					 * later
					 */
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
					paradataUE.addParadataOrchestrators(paradataOrchestrator);

				} else if (event.getIdParadataObject().toUpperCase().contains("SESSION")) {
					paradataSession.setTimestamp((long) collected_event.get(timestamp));
					paradataUE.addParadataSessions(paradataSession);

				} else {
					log.warn("Unexpected paradata for SurveyUnit " + event.getIdSurveyUnit() + " at timestamp "
							+ collected_event.get(timestamp));
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
		Set<String> paradataVariables = paraDataUE.getParadataVariables().keySet();
		Variable variableDuree = new Variable(Constants.LENGTH_ORCHESTRATORS_NAME, surveyRawData.getVariablesMap().getRootGroup(), VariableType.STRING, "30");
		Variable variableDureeBrute = new Variable(Constants.LENGTH_ORCHESTRATORS_NAME + "_LONG", surveyRawData.getVariablesMap().getRootGroup(), VariableType.INTEGER, "20");
		Variable variableNombre = new Variable(Constants.NUMBER_ORCHESTRATORS_NAME, surveyRawData.getVariablesMap().getRootGroup(), VariableType.INTEGER, "3");
		try {
			surveyRawData.getVariablesMap().putVariable(variableDuree);
			surveyRawData.getVariablesMap().putVariable(variableDureeBrute);
			surveyRawData.getVariablesMap().putVariable(variableNombre);
			for (String variableName : paradataVariables) {
				if (variableName.contentEquals("PRENOM")) {
					Variable variable = new Variable(Constants.PARADATA_VARIABLES_PREFIX + variableName, surveyRawData.getVariablesMap().getRootGroup(),
							VariableType.STRING, "3");
					surveyRawData.getVariablesMap().putVariable(variable);
				}
			}
		} catch (Exception e) {
			// TODO: handle exception
		}

		if (paraDataUE.getOrchestrators().size() > 0) {
			long lengthOrchestrators = paraDataUE.createLengthOrchestratorsVariable();
			QuestionnaireData questionnaire = surveyRawData.getQuestionnaires().stream()
					.filter(questionnaireToSearch -> paraDataUE.getOrchestrators().get(0).getIdentifier()
							.equals(questionnaireToSearch.getIdentifier()))
					.findAny().orElse(null);
			if (questionnaire != null) {
				questionnaire.getAnswers().putValue(variableDuree.getName(),
						Constants.convertToDateFormat(lengthOrchestrators));
				questionnaire.getAnswers().putValue(variableDureeBrute.getName(), Long.toString(lengthOrchestrators));
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
