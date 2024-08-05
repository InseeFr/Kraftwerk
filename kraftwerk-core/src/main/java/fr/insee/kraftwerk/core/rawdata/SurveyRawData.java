package fr.insee.kraftwerk.core.rawdata;

import fr.insee.bpm.metadata.model.MetadataModel;
import fr.insee.bpm.metadata.model.Variable;
import lombok.Getter;
import lombok.Setter;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Object class to temporary store the data from a survey answer file.
 *
 */
@Getter@Setter
public class SurveyRawData {

	private String dataMode;
	private Path dataFilePath;
	private Path paraDataFolder;
	private MetadataModel metadataModel;
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

	public void putVariable(Variable variable) {
		metadataModel.getVariables().putVariable(variable);
	}

}
