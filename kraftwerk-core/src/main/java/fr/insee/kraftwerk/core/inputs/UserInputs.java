package fr.insee.kraftwerk.core.inputs;

import lombok.Getter;
import lombok.Setter;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

@Setter
@Getter
public class UserInputs {

	protected Path inputDirectory;
	protected Map<String, ModeInputs> modeInputsMap = new HashMap<>();
	protected Path vtlReconciliationFile;
	protected Path vtlTransformationsFile;
	protected Path vtlInformationLevelsFile;
	protected String multimodeDatasetName;

	public UserInputs(Path inputDirectory) {
		this.inputDirectory = inputDirectory;
	}
	public ModeInputs getModeInputs(String modeName) {
		return modeInputsMap.get(modeName);
	}


}
