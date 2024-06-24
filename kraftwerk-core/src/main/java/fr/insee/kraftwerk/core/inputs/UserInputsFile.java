package fr.insee.kraftwerk.core.inputs;

import com.fasterxml.jackson.databind.JsonNode;
import fr.insee.kraftwerk.core.exceptions.KraftwerkException;
import fr.insee.kraftwerk.core.exceptions.MissingMandatoryFieldException;
import fr.insee.kraftwerk.core.exceptions.UnknownDataFormatException;
import fr.insee.kraftwerk.core.utils.files.FileUtilsInterface;
import fr.insee.kraftwerk.core.utils.JsonFileReader;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Log4j2
public class UserInputsFile extends UserInputs {

	@Getter
	@Setter
	private Path userInputFile;

	private final Set<String> mandatoryFields = Set.of("survey_data", "data_mode", "data_file", "data_format", "multimode_dataset_name");

	public UserInputsFile(Path userConfigFile, Path inputDirectory, FileUtilsInterface fileUtilsInterface) throws KraftwerkException {
		super(inputDirectory,fileUtilsInterface);
		this.userInputFile = userConfigFile;
		readUserInputs();
	}

	private void readUserInputs() throws UnknownDataFormatException, MissingMandatoryFieldException, KraftwerkException {

		try {
			JsonNode userInputs = JsonFileReader.read(userInputFile);
			//
			JsonNode filesNode = userInputs.get("survey_data");
			for (JsonNode fileNode : filesNode) {
				//
				String dataMode = readField(fileNode, "data_mode");
				String dataFolder = readField(fileNode, "data_file");
				String paradataFolder = readField(fileNode, "paradata_folder");
				String reportingFolder = readField(fileNode, "reporting_data_file");
				Path dataPath = (fileUtilsInterface.isFileExists(dataFolder)) ? convertToUserPath(dataFolder) : fileUtilsInterface.convertToPath(dataFolder,inputDirectory);
				URL ddiFile = fileUtilsInterface.convertToUrl(readField(fileNode, "DDI_file"),inputDirectory);
				Path lunaticFile = fileUtilsInterface.convertToPath(readField(fileNode, "lunatic_file"),inputDirectory);
				String dataFormat = readField(fileNode, "data_format");
				Path paradataPath = (paradataFolder != null && fileUtilsInterface.isFileExists(paradataFolder)) ? convertToUserPath(paradataFolder) : fileUtilsInterface.convertToPath(paradataFolder,inputDirectory);
				Path reportingDataFile = (reportingFolder != null && fileUtilsInterface.isFileExists(reportingFolder)) ? convertToUserPath(reportingFolder) : fileUtilsInterface.convertToPath(reportingFolder,inputDirectory);
				Path vtlFile = fileUtilsInterface.convertToPath(readField(fileNode, "mode_specifications"),inputDirectory);
				ModeInputs modeInputs = new ModeInputs();
				modeInputs.setDataFile(dataPath);
				modeInputs.setDdiUrl(ddiFile);
				modeInputs.setLunaticFile(lunaticFile);
				modeInputs.setDataFormat(dataFormat);
				modeInputs.setParadataFolder(paradataPath);
				modeInputs.setReportingDataFile(reportingDataFile);
				modeInputs.setModeVtlFile(vtlFile);
				modeInputsMap.put(dataMode, modeInputs);
			}
			//
			multimodeDatasetName = readField(userInputs, "multimode_dataset_name");
			vtlReconciliationFile = fileUtilsInterface.convertToPath(readField(userInputs, "reconciliation_specifications"),inputDirectory);
			vtlTransformationsFile = fileUtilsInterface.convertToPath(readField(userInputs, "transformation_specifications"),inputDirectory);
			vtlInformationLevelsFile = fileUtilsInterface.convertToPath(readField(userInputs, "information_levels_specifications"),inputDirectory);

		} catch (IOException e) {
			log.error("Unable to read user input file: {} , {}", userInputFile, e);
			throw new UnknownDataFormatException(e.getMessage());
		}
	}

	public List<String> getModes() {
		return new ArrayList<>(modeInputsMap.keySet());
	}

	private String readField(JsonNode node, String field) throws MissingMandatoryFieldException {
		JsonNode value = node.get(field);
		if (value != null) {
			String text = value.asText();
			if (!(text.isEmpty() || text.equals("null"))) {
				return text;
			}
			if (mandatoryFields.contains(field)) {
				throw new MissingMandatoryFieldException(String
						.format("Empty or null value in mandatory field \"%s\" in the input file given", field));
			}
			return null;
		}
		if (mandatoryFields.contains(field)) {
			throw new MissingMandatoryFieldException(
					String.format("Mandatory field \"%s\" missing in the input file given", field));
		}
		log.info(String.format("Optional field \"%s\" missing in the input file given", field));
		return null;
	}

	private Path convertToUserPath(String userField) {
		if (userField != null && !"null".equals(userField) && !userField.isEmpty()) {
			return Paths.get(userField);
		}
		return null;
	}

}
