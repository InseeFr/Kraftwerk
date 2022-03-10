package fr.insee.kraftwerk.rawdata;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fr.insee.kraftwerk.metadata.VariablesMap;

/**
 * Object class to temporary store the data from an survey answer file.
 *
 */
public class SurveyRawData {

	private String dataMode;
	private String dataFilePath;
	private String paraDataFolder;
	private VariablesMap variablesMap;
	private final List<QuestionnaireData> questionnaires = new ArrayList<>();

	public SurveyRawData() {}
	public SurveyRawData(String dataMode) {
		this.dataMode = dataMode;
	}

	public String getDataMode() {
		return dataMode;
	}
	public void setDataMode(String dataMode) {
		this.dataMode = dataMode;
	}

	public String getDataFilePath() {
		return dataFilePath;
	}
	public void setDataFilePath(String dataFilePath) {
		this.dataFilePath = dataFilePath;
	}

	public String getParaDataFolder() {
		return paraDataFolder;
	}
	public void setParaDataFolder(String paraDataFolder) {
		this.paraDataFolder = paraDataFolder;
	}

	public VariablesMap getVariablesMap() {
		return variablesMap;
	}
	public void setVariablesMap(VariablesMap variablesMap) {
		this.variablesMap = variablesMap;
	}
	public void addQuestionnaire(QuestionnaireData questionnaireData) {
		questionnaires.add(questionnaireData);
	}
	public List<QuestionnaireData> getQuestionnaires() {
		return questionnaires;
	}

	/** Return the number of variables registered in the variables attribute. */
	public int getVariablesCount() {
		return variablesMap.getVariables().size();
	}

	/** Return the number of questionnaires stored in the object. */
	public int getQuestionnairesCount() {
		return questionnaires.size();
	}

}
