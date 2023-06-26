package fr.insee.kraftwerk.core.inputs;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.databind.JsonNode;

import fr.insee.kraftwerk.core.exceptions.KraftwerkException;
import fr.insee.kraftwerk.core.exceptions.MissingMandatoryFieldException;
import fr.insee.kraftwerk.core.exceptions.UnknownDataFormatException;
import fr.insee.kraftwerk.core.utils.JsonFileReader;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class UserInputs {

	@Getter
	@Setter
	private Path userInputFile;

	@Getter
	@Setter
	private Path inputDirectory;

	@Getter
	@Setter
	private Map<String, ModeInputs> modeInputsMap = new HashMap<>();
	@Getter
	@Setter
	private String multimodeDatasetName;
	@Getter
	@Setter
	private Path vtlReconciliationFile;
	@Getter
	@Setter
	private Path vtlTransformationsFile;
	@Getter
	@Setter
	private Path vtlInformationLevelsFile;

	private final Set<String> mandatoryFields = Set.of("survey_data", "data_mode", "data_file", "DDI_file",
			"data_format", "multimode_dataset_name");

	public UserInputs(){}

	public UserInputs(Path userConfigFile, Path inputDirectory) throws KraftwerkException {
		this.userInputFile = userConfigFile;
		this.inputDirectory = inputDirectory;
		readUserInputs();
	}

	public ModeInputs getModeInputs(String modeName) {
		return modeInputsMap.get(modeName);
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
				Path dataPath = (new File(dataFolder).exists()) ? convertToUserPath(dataFolder) : convertToPath(dataFolder);
				URL ddiFile = convertToUrl(readField(fileNode, "DDI_file"));
				Path lunaticFile = convertToPath(readField(fileNode, "lunatic_file"));
				String dataFormat = readField(fileNode, "data_format");
				Path paradataPath = (paradataFolder != null && new File(paradataFolder).exists()) ? convertToUserPath(paradataFolder) : convertToPath(paradataFolder);
				Path reportingDataFile = (reportingFolder != null && new File(reportingFolder).exists()) ? convertToUserPath(reportingFolder) : convertToPath(reportingFolder);
				Path vtlFile = convertToPath(readField(fileNode, "mode_specifications"));
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
			vtlReconciliationFile = convertToPath(readField(userInputs, "reconciliation_specifications"));
			vtlTransformationsFile = convertToPath(readField(userInputs, "transformation_specifications"));
			vtlInformationLevelsFile = convertToPath(readField(userInputs, "information_levels_specifications"));

		} catch (IOException e) {
			log.error("Unable to read user input file: {} , {}", userInputFile, e);
			throw new UnknownDataFormatException(e.getMessage());
		} catch (KraftwerkException e) {
			throw e;
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

	private Path convertToPath(String userField) throws KraftwerkException {
		if (userField != null && !"null".equals(userField) && !"".equals(userField)) {
			Path inputPath = inputDirectory.resolve(userField);
			if (!new File(inputPath.toUri()).exists()) {
				throw new KraftwerkException(400, String.format("The input folder \"%s\" does not exist.", userField));
			}
			return inputPath;
		} else {
			return null;
		}
	}

	private Path convertToUserPath(String userField) {
		if (userField != null && !"null".equals(userField) && !"".equals(userField)) {
			return Paths.get(userField);
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
