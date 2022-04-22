package fr.insee.kraftwerk.core.inputs;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.databind.JsonNode;

import fr.insee.kraftwerk.core.utils.JsonFileReader;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class UserInputs {

	private final String userInputFile;

	@Getter
	private final Path inputDirectory;

	@Getter
	private final Map<String, ModeInputs> modeInputsMap = new HashMap<>();
	@Getter
	private String multimodeDatasetName;
	@Getter
	private Path vtlReconciliationFile;
	@Getter
	private Path vtlTransformationsFile;
	@Getter
	private Path vtlInformationLevelsFile;

	private final Set<String> mandatoryFields = Set.of("survey_data", "data_mode", "data_file", "DDI_file",
			"data_format", "multimode_dataset_name");

	public UserInputs(String userConfigFile, Path inputDirectory) {
		this.userInputFile = userConfigFile;
		this.inputDirectory = inputDirectory;
		readUserInputs();
	}

	public ModeInputs getModeInputs(String modeName) {
		return modeInputsMap.get(modeName);
	}

	private void readUserInputs() throws UnknownDataFormatException, MissingMandatoryFieldException {

		try {
			JsonNode userInputs = JsonFileReader.read(userInputFile);
			//
			JsonNode filesNode = userInputs.get("survey_data");
			for (JsonNode fileNode : filesNode) {
				//
				String lunaticFile = getText(fileNode, "lunatic_file");
				String dataMode = readField(fileNode, "data_mode");
				Path dataFile = convertToPath(readField(fileNode, "data_file"));
				URL ddiFile = convertToUrl(readField(fileNode, "DDI_file"));
				String dataFormat = readField(fileNode, "data_format");
				Path paradataFolder = convertToPath(readField(fileNode, "paradata_folder"));
				Path reportingDataFile = convertToPath(readField(fileNode, "reporting_data_file"));
				Path vtlFile = convertToPath(readField(fileNode, "mode_specifications"));
				//
				ModeInputs modeInputs = new ModeInputs();
				modeInputs.setDataFile(dataFile);
				modeInputs.setDDIURL(ddiFile);
				modeInputs.setLunaticFile(lunaticFile);
				modeInputs.setDataFormat(dataFormat);
				modeInputs.setParadataFolder(paradataFolder);
				modeInputs.setReportingDataFile(reportingDataFile);
				modeInputs.setModeVtlFile(vtlFile);
				modeInputsMap.put(dataMode, modeInputs);
			}
			//
			multimodeDatasetName = readField(userInputs, "multimode_dataset_name");
			vtlReconciliationFile = convertToPath(readField(userInputs, "reconciliation_specifications"));
			vtlTransformationsFile = convertToPath(readField(userInputs, "transformation_specifications"));
			vtlInformationLevelsFile = convertToPath(readField(userInputs, "information_levels_specifications"));

		} catch (IOException e) { // TODO: split read file and json parsing to throw IllegalArgumentException if
									// the json file is malformed
			log.error("Unable to read user input file: " + userInputFile, e);
		}
	}

	public List<String> getModes() {
		return new ArrayList<>(modeInputsMap.keySet());
	}

	private String readField(JsonNode node, String field) throws MissingMandatoryFieldException {
		JsonNode value = node.get(field);
		if (value != null) {
			String text = value.asText();
			if (!(text.equals("") || text.equals("null"))) {
				return text;
			} else {
				if (mandatoryFields.contains(field)) {
					throw new MissingMandatoryFieldException(String
							.format("Empty or null value in mandatory field \"%s\" in the input file given", field));
				} else {
					return null;
				}
			}
		} else {
			if (mandatoryFields.contains(field)) {
				throw new MissingMandatoryFieldException(
						String.format("Mandatory field \"%s\" missing in the input file given", field));
			} else {
				log.info(String.format("Optional field \"%s\" missing in the input file given", field));
				return null;
			}
		}
	}

	private Path convertToPath(String userField) {
		if (userField != null) {
			return inputDirectory.resolve(userField);
		} else {
			return null;
		}

	}

	private URL convertToUrl(String userField) {
		if (userField == null) {
			log.debug("null value out of method that reads DDI field (should not happen).");
			return null;
		}
		try {
			if (userField.startsWith("http")) {
				return new URL(userField);
			} else {
				return inputDirectory.resolve(userField).toFile().toURI().toURL();
			}
		} catch (MalformedURLException e) {
			log.error("Unable to convert URL from user input: " + userField);
			return null;
		}
	}

}
