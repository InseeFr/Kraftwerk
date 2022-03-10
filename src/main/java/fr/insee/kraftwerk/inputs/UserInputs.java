package fr.insee.kraftwerk.inputs;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.databind.JsonNode;

import fr.insee.kraftwerk.Constants;
import fr.insee.kraftwerk.Launcher;
import fr.insee.kraftwerk.utils.JsonFileReader;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class UserInputs {

	private final String userInputFile;
	private final Path inputDirectory;

	@Getter
	private String campaignName;
	@Getter
	private final Map<String, ModeInputs> modeInputsMap = new HashMap<>();
	@Getter
	private String multimodeDatasetName;
	@Getter
	private String vtlReconciliationFile;
	@Getter
	private String vtlTransformationsFile;
	@Getter
	private String vtlInformationLevelsFile;

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
			campaignName = Launcher.readCampaignName(inputDirectory);
			//
			JsonNode filesNode = userInputs.get("survey_data");
			for (JsonNode fileNode : filesNode) {
				//
				String dataMode = getText(fileNode, "data_mode");
				String dataFile = Constants.getInputPath(inputDirectory, getText(fileNode, "data_file"));
				String DDIFilePath = getText(fileNode, "DDI_file");
				String DDIFile = "";
				if (DDIFilePath.contains("http")) {
					DDIFile = DDIFilePath;
				} else {
					DDIFile = Constants.getInputPath(inputDirectory, getText(fileNode, "DDI_file"));
				}
				String dataFormat = getText(fileNode, "data_format");
				String paradataFolder = Constants.getInputPath(inputDirectory, getText(fileNode, "paradata_folder"));
				String reportingDataFile = Constants.getInputPath(inputDirectory,
						getText(fileNode, "reporting_data_file"));
				String vtlFile = Constants.getInputPath(inputDirectory, getText(fileNode, "mode_specifications"));
				//
				ModeInputs modeInputs = new ModeInputs();
				modeInputs.setDataFile(dataFile);
				modeInputs.setDDIFile(DDIFile);
				modeInputs.setDataFormat(dataFormat);
				modeInputs.setParadataFolder(paradataFolder);
				modeInputs.setReportingDataFile(reportingDataFile);
				modeInputs.setModeVtlFile(vtlFile);
				modeInputsMap.put(dataMode, modeInputs);
			}
			//
			multimodeDatasetName = getText(userInputs, "multimode_dataset_name");
			vtlReconciliationFile = getText(userInputs, "reconciliation_specifications");
			vtlTransformationsFile = getText(userInputs, "transformation_specifications");
			vtlInformationLevelsFile = getText(userInputs, "information_levels_specifications");

		} catch (IOException e) { // TODO: split read file and json parsing to throw IllegalArgumentException if
									// the json file is malformed
			log.error("Unable to read user input file: " + userInputFile, e);
		}
	}

	public List<String> getModes() {
		return new ArrayList<>(modeInputsMap.keySet());
	}

	private String getText(JsonNode node, String field) throws MissingMandatoryFieldException {
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
}
