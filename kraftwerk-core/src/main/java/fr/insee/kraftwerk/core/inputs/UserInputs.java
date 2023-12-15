package fr.insee.kraftwerk.core.inputs;

import lombok.Getter;
import lombok.Setter;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class UserInputs {

	@Getter
	@Setter
	protected Path inputDirectory;
	@Getter
	@Setter
	protected Map<String, ModeInputs> modeInputsMap = new HashMap<>();
	@Getter
	@Setter
	protected Path vtlReconciliationFile;
	@Getter
	@Setter
	protected Path vtlTransformationsFile;
	@Getter
	@Setter
	protected Path vtlInformationLevelsFile;
	@Getter
	@Setter
	protected String multimodeDatasetName;

	public UserInputs(Path inputDirectory) {
		this.inputDirectory = inputDirectory;
	}
	public ModeInputs getModeInputs(String modeName) {
		return modeInputsMap.get(modeName);
	}


}
