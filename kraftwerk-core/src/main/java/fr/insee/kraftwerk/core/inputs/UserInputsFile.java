package fr.insee.kraftwerk.core.inputs;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.databind.JsonNode;

import fr.insee.kraftwerk.core.exceptions.KraftwerkException;
import fr.insee.kraftwerk.core.exceptions.MissingMandatoryFieldException;
import fr.insee.kraftwerk.core.exceptions.UnknownDataFormatException;
import fr.insee.kraftwerk.core.utils.FileUtils;
import fr.insee.kraftwerk.core.utils.JsonFileReader;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class UserInputsFile extends UserInputs {

	@Getter
	@Setter
	private Path userInputFile;

	private final Set<String> mandatoryFields = Set.of("survey_data", "data_mode", "data_file", "data_format", "multimode_dataset_name");

	public UserInputsFile(Path userConfigFile, Path inputDirectory) throws KraftwerkException {
		super(inputDirectory);
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
				Path dataPath = (new File(dataFolder).exists()) ? convertToUserPath(dataFolder) : FileUtils.convertToPath(dataFolder,inputDirectory);
				URL ddiFile = FileUtils.convertToUrl(readField(fileNode, "DDI_file"),inputDirectory);
				Path lunaticFile = FileUtils.convertToPath(readField(fileNode, "lunatic_file"),inputDirectory);
				String dataFormat = readField(fileNode, "data_format");
				Path paradataPath = (paradataFolder != null && new File(paradataFolder).exists()) ? convertToUserPath(paradataFolder) : FileUtils.convertToPath(paradataFolder,inputDirectory);
				Path reportingDataFile = (reportingFolder != null && new File(reportingFolder).exists()) ? convertToUserPath(reportingFolder) : FileUtils.convertToPath(reportingFolder,inputDirectory);
				Path vtlFile = FileUtils.convertToPath(readField(fileNode, "mode_specifications"),inputDirectory);
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
			vtlReconciliationFile = FileUtils.convertToPath(readField(userInputs, "reconciliation_specifications"),inputDirectory);
			vtlTransformationsFile = FileUtils.convertToPath(readField(userInputs, "transformation_specifications"),inputDirectory);
			vtlInformationLevelsFile = FileUtils.convertToPath(readField(userInputs, "information_levels_specifications"),inputDirectory);

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
		if (userField != null && !"null".equals(userField) && !"".equals(userField)) {
			return Paths.get(userField);
		}
		return null;
	}

}