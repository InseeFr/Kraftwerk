package fr.insee.kraftwerk.core.parsers;

import fr.insee.kraftwerk.core.Constants;
import fr.insee.kraftwerk.core.exceptions.NullException;
import fr.insee.kraftwerk.core.rawdata.GroupInstance;
import fr.insee.kraftwerk.core.rawdata.QuestionnaireData;
import fr.insee.kraftwerk.core.rawdata.SurveyRawData;
import lombok.extern.log4j.Log4j2;
import org.json.simple.JSONObject;

import java.nio.file.Path;

@Log4j2
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
		JSONObject jsonData = (JSONObject) jsonObject.get("data");
		String identifier = (String) jsonObject.get("id");

		QuestionnaireData questionnaireData = new QuestionnaireData();

		// Root identifier

		questionnaireData.setIdentifier(identifier);
		data.getIdSurveyUnits().add(identifier);

		// Survey answers
		GroupInstance answers = questionnaireData.getAnswers();

		// Now we get each variable collected during the survey
		JSONObject collectedVariables = (JSONObject) jsonData.get(Constants.COLLECTED);

		for (Object variable : collectedVariables.keySet()) {
			String variableName = (String) variable;
			JSONObject variableData = (JSONObject) collectedVariables.get(variableName);
			if (data.getMetadataModel().getVariables().hasVariable(variableName)) {
				String value = "";
				if (variableData.get(Constants.COLLECTED) != null){
					value = variableData.get(Constants.COLLECTED).toString();
				} 
				answers.putValue(variableName, value);
			} else {
				log.warn(String.format("WARNING: Variable %s not expected!", variableName));
			}
			
		}


		// TODO Get each variable calculated and external 

		data.addQuestionnaire(questionnaireData);
	}

}
