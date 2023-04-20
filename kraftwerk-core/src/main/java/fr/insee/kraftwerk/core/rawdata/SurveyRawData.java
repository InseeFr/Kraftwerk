package fr.insee.kraftwerk.core.rawdata;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import fr.insee.kraftwerk.core.metadata.VariablesMap;
import lombok.Getter;
import lombok.Setter;

/**
 * Object class to temporary store the data from a survey answer file.
 *
 */
@Getter@Setter
public class SurveyRawData {

	private String dataMode;
	private Path dataFilePath;
	private Path paraDataFolder;
	private VariablesMap variablesMap;
	private final List<QuestionnaireData> questionnaires = new ArrayList<>();
    private List<String> idSurveyUnits = new ArrayList<>();//Used for file by file operations


	public SurveyRawData() {}
	public SurveyRawData(String dataMode) {
		this.dataMode = dataMode;
	}

	public void addQuestionnaire(QuestionnaireData questionnaireData) {
		questionnaires.add(questionnaireData);
	}

	/** Return the number of questionnaires stored in the object. */
	public int getQuestionnairesCount() {
		return questionnaires.size();
	}

}
