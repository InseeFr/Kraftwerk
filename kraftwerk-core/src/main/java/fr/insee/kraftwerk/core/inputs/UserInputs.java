package fr.insee.kraftwerk.core.inputs;

import fr.insee.kraftwerk.core.utils.files.FileUtilsInterface;
import lombok.Getter;
import lombok.Setter;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

@Setter
@Getter
public class UserInputs {

	protected Path inputDirectory;
	protected Path specsDirectory;
	protected Map<String, ModeInputs> modeInputsMap = new HashMap<>();
	protected Path vtlReconciliationFile;
	protected Path vtlTransformationsFile;
	protected Path vtlInformationLevelsFile;
	protected String multimodeDatasetName;
	protected FileUtilsInterface fileUtilsInterface;

	public UserInputs(Path inputDirectory, FileUtilsInterface fileUtilsInterface) {
		this.inputDirectory = inputDirectory;
		this.fileUtilsInterface = fileUtilsInterface;
	}
	public ModeInputs getModeInputs(String modeName) {
		return modeInputsMap.get(modeName);
	}


}
