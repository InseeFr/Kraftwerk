package fr.insee.kraftwerk.core.parsers;

import java.nio.file.Path;

import org.json.simple.JSONObject;

import fr.insee.kraftwerk.core.Constants;
import fr.insee.kraftwerk.core.exceptions.NullException;
import fr.insee.kraftwerk.core.rawdata.GroupInstance;
import fr.insee.kraftwerk.core.rawdata.QuestionnaireData;
import fr.insee.kraftwerk.core.rawdata.SurveyRawData;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class LunaticJsonDataParser extends DataParser {

	/**
	 * Parser constructor.
	 * @param data The SurveyRawData to be filled by the parseSurveyData method.
	 *             The variables must have been previously set.
	 */
	public LunaticJsonDataParser(SurveyRawData data) {
		super(data);
	}

	@Override
	void parseDataFile(Path filePath) throws NullException {
		log.warn("Lunatic data parser being implemented!");

		//
		JSONObject jsonObject = null;
		try {
			jsonObject = (JSONObject) Constants.readJsonSimple(filePath);
		} catch (Exception e) {
			throw new NullException("Can't read JSON file - "+e.getClass()+" "+ e.getMessage());
		}
		//JSONObject stateData = (JSONObject) jsonObject.get("stateData");
		JSONObject jsonData = (JSONObject) jsonObject.get("data");
		String identifier = (String) jsonObject.get("id");

		QuestionnaireData questionnaireData = new QuestionnaireData();

		// Root identifier
		questionnaireData.setIdentifier(identifier);

		// Survey answers
		GroupInstance answers = questionnaireData.getAnswers();

		// Now we get each variable calculated during the survey
		// TODO

		// Now we get each variable collected during the survey
		JSONObject collected_variables = (JSONObject) jsonData.get("COLLECTED");

		for (Object variable : collected_variables.keySet()) {
			String variableName = (String) variable;
			JSONObject variable_data = (JSONObject) collected_variables.get(variableName);
			if (data.getVariablesMap().hasVariable(variableName)) {
				String value = "";
				if (variable_data.get("COLLECTED") != null){
					value = (String) variable_data.get("COLLECTED").toString();
				} 
				answers.putValue(variableName, value);
			} else {
				//TODO fix log, lots of useless lines
				log.warn(String.format("WARNING: Variable %s not expected!", variableName));
			}
			
		}

		// Now we get each variable external to the survey
		// TODO
		
		//
		data.addQuestionnaire(questionnaireData);
	}

}
