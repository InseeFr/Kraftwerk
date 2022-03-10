package fr.insee.kraftwerk.parsers;

import org.json.simple.JSONObject;

import fr.insee.kraftwerk.Constants;
import fr.insee.kraftwerk.rawdata.GroupInstance;
import fr.insee.kraftwerk.rawdata.QuestionnaireData;
import fr.insee.kraftwerk.rawdata.SurveyRawData;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class LunaticJsonDataParser implements DataParser {

	public void parseSurveyData(SurveyRawData surveyRawData) {
		log.warn("Lunatic data parser being implemented!");

		//
		String filePath = surveyRawData.getDataFilePath();

		//
		JSONObject jsonObject = null;
		try {
			jsonObject = (JSONObject) Constants.readJsonSimple(filePath);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//JSONObject stateData = (JSONObject) jsonObject.get("stateData");
		JSONObject data = (JSONObject) jsonObject.get("data");
		String identifier = (String) jsonObject.get("id");

		QuestionnaireData questionnaireData = new QuestionnaireData();

		// Root identifier
		questionnaireData.setIdentifier(identifier);

		// Survey answers
		GroupInstance answers = questionnaireData.getAnswers();

		// Now we get each variable calculated during the survey
		// TODO

		// Now we get each variable collected during the survey
		JSONObject collected_variables = (JSONObject) data.get("COLLECTED");

		for (Object variable : collected_variables.keySet()) {
			String variableLabel = (String) variable;
			JSONObject variable_data = (JSONObject) collected_variables.get(variableLabel);
			if (surveyRawData.getVariablesMap().getVariables().containsKey(variableLabel)) {
				String value = "";
				if (variable_data.get("COLLECTED") != null){
					value = (String) variable_data.get("COLLECTED").toString();
				} 
				answers.putValue(variableLabel, value);
			} else {
				//TODO fix log, lots of useless lines
				log.warn(String.format("WARNING: Variable %s not expected!", variableLabel));
			}
			
		}

		// Now we get each variable external to the survey
		// TODO
		
		//
		surveyRawData.addQuestionnaire(questionnaireData);
	}

}
