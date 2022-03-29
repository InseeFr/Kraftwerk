package fr.insee.kraftwerk.core.rawdata;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import fr.insee.kraftwerk.core.metadata.VariablesMap;

/**
 * Object class to temporary store the data from an survey answer file.
 *
 */
public class SurveyRawData {

	private String dataMode;
	private Path dataFilePath;
	private Path paraDataFolder;
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

	public Path getDataFilePath() {
		return dataFilePath;
	}
	public void setDataFilePath(Path path) {
		this.dataFilePath = path;
	}

	public Path getParaDataFolder() {
		return paraDataFolder;
	}
	public void setParaDataFolder(Path paraDataFolder) {
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
