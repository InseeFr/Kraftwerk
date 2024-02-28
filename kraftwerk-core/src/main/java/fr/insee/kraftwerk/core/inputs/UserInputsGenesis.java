package fr.insee.kraftwerk.core.inputs;

import com.fasterxml.jackson.databind.JsonNode;
import fr.insee.kraftwerk.core.Constants;
import fr.insee.kraftwerk.core.data.model.Mode;
import fr.insee.kraftwerk.core.exceptions.KraftwerkException;
import fr.insee.kraftwerk.core.exceptions.MissingMandatoryFieldException;
import fr.insee.kraftwerk.core.exceptions.UnknownDataFormatException;
import fr.insee.kraftwerk.core.utils.FileUtils;
import fr.insee.kraftwerk.core.utils.JsonFileReader;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

@Log4j2
public class UserInputsGenesis extends UserInputs{

	@Getter
	@Setter
	boolean hasConfigFile;

	private List<Mode> modes;



	public UserInputsGenesis(boolean hasConfigFile, Path inputDirectory, List<Mode> modes) throws KraftwerkException, IOException {
		super(inputDirectory);
		this.hasConfigFile = hasConfigFile;
		this.modes=modes;
		computeInputs();
	}

	private void computeInputs() throws KraftwerkException, IOException {
		for (Mode mode : modes) {
			ModeInputs modeInputs = new ModeInputs();
			modeInputs.setDdiUrl(findDDIFile(inputDirectory.resolve(mode.name())).toFile().toURI().toURL());
			modeInputs.setLunaticFile(findLunaticFile(inputDirectory.resolve(mode.name())));
			modeInputs.setDataMode(mode.name());
			if (mode == Mode.WEB || mode == Mode.TEL || mode == Mode.F2F) {
				modeInputs.setDataFormat("LUNATIC_XML");
			} else {
				// Not implemented yet
				modeInputs.setDataFormat("OTHER");
			}
			if (isHasConfigFile()) {
				modeInputs.setModeVtlFile(getModeVtlFile(mode));
			}
			modeInputsMap.put(mode.name(), modeInputs);
		}
		if (isHasConfigFile()) {
			readUserInputs();
		}
	}

	private Path getModeVtlFile(Mode mode) throws UnknownDataFormatException, MissingMandatoryFieldException, KraftwerkException {
		Path userInputFile = inputDirectory.resolve(Constants.USER_INPUT_FILE);
		try {
			JsonNode userInputs = JsonFileReader.read(userInputFile);
			JsonNode filesNode = userInputs.get("survey_data");
			for (JsonNode fileNode : filesNode) {
				String dataMode = readField(fileNode, "data_mode");
				if (dataMode == null) {break;}
				if (dataMode.equals(mode.name())) {
					return FileUtils.convertToPath(readField(fileNode, "mode_specifications"),inputDirectory);
				}
			}
		} catch (IOException e) {
			log.error("Unable to read user input file: {} , {}", userInputFile, e);
			throw new UnknownDataFormatException(e.getMessage());
		}
		return null;
	}

	private void readUserInputs() throws UnknownDataFormatException, MissingMandatoryFieldException, KraftwerkException {
		Path userInputFile = inputDirectory.resolve(Constants.USER_INPUT_FILE);
		try {
			JsonNode userInputs = JsonFileReader.read(userInputFile);
			vtlReconciliationFile = FileUtils.convertToPath(readField(userInputs, "reconciliation_specifications"),inputDirectory);
			vtlTransformationsFile = FileUtils.convertToPath(readField(userInputs, "transformation_specifications"),inputDirectory);
			vtlInformationLevelsFile = FileUtils.convertToPath(readField(userInputs, "information_levels_specifications"),inputDirectory);

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
		}
		return null;
	}

	/**
	 * Find the DDI file in the folder of a campaign
	 * @param specDirectory
	 * @return Path of the DDI file
	 * @throws IOException
	 */
	public Path findDDIFile(Path specDirectory) throws KraftwerkException, IOException {
		try (Stream<Path> files = Files.find(specDirectory, 1, (path, basicFileAttributes) -> path.toFile().getName().matches("ddi[\\w,\\s-]+\\.xml"))) {
			return files.findFirst()
					.orElseThrow(() -> new KraftwerkException(404, "No DDI file found in " + specDirectory.toString()));
		}
	}

	/**
	 * Find the Lunatic file in the folder of a campaign
	 * @param specDirectory
	 * @return Path of the DDI file
	 * @throws IOException
	 */
	public Path findLunaticFile(Path specDirectory) throws KraftwerkException, IOException {
		try (Stream<Path> files = Files.find(specDirectory, 1, (path, basicFileAttributes) -> path.toFile().getName().matches("lunatic[\\w,\\s-]+\\.json"))) {
			return files.findFirst()
					.orElseThrow(() -> new KraftwerkException(404, "No Lunatic file found in " + specDirectory.toString()));
		}
	}
}
