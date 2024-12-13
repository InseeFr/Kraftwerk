package fr.insee.kraftwerk.core.inputs;

import com.fasterxml.jackson.databind.JsonNode;
import fr.insee.kraftwerk.core.Constants;
import fr.insee.kraftwerk.core.data.model.Mode;
import fr.insee.kraftwerk.core.exceptions.KraftwerkException;
import fr.insee.kraftwerk.core.exceptions.MissingMandatoryFieldException;
import fr.insee.kraftwerk.core.exceptions.UnknownDataFormatException;
import fr.insee.kraftwerk.core.utils.files.FileUtilsInterface;
import fr.insee.kraftwerk.core.utils.JsonReader;
import lombok.extern.log4j.Log4j2;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@Log4j2
public class UserInputsGenesis extends UserInputs{

	private final boolean hasConfigFile;

	private final List<Mode> modes;

	public UserInputsGenesis(boolean hasConfigFile, Path inputDirectory, List<Mode> modes, FileUtilsInterface fileUtilsInterface, boolean withDDI) throws KraftwerkException {
		super(inputDirectory, fileUtilsInterface);
		this.hasConfigFile = hasConfigFile;
		this.modes=modes;
		computeInputs(withDDI);
	}

	private void computeInputs(boolean withDDI) throws KraftwerkException {
        UserInputsFile userInputsFile;
		if(hasConfigFile){
            userInputsFile = new UserInputsFile(inputDirectory.resolve(Constants.USER_INPUT_FILE), inputDirectory, fileUtilsInterface);
            modeInputsMap = userInputsFile.getModeInputsMap();
		}else{
            for (Mode mode : modes) {
                ModeInputs modeInputs = getModeInputs(mode, withDDI);
                modeInputsMap.put(mode.name(), modeInputs);
            }
        }
	}

    /**
     * Look for mode inputs without a configuration file
     * @param mode mode to get inputs from
     * @return a ModeInputs object
     */
    private ModeInputs getModeInputs(Mode mode, boolean withDDI) throws KraftwerkException {
        ModeInputs modeInputs = new ModeInputs();
        if (withDDI) {
            modeInputs.setDdiUrl(findDDIFile(inputDirectory.resolve(mode.name())).toString());
        }
		modeInputs.setLunaticFile(findLunaticFile(inputDirectory.resolve(mode.name())));

        modeInputs.setDataMode(mode.name());
        if (mode == Mode.WEB || mode == Mode.TEL || mode == Mode.F2F) {
            modeInputs.setDataFormat("LUNATIC_XML");
        } else {
            // Not implemented yet
            modeInputs.setDataFormat("OTHER");
        }
        if (hasConfigFile) {
            modeInputs.setModeVtlFile(getModeVtlFile(mode));
        }
        return modeInputs;
    }

    private Path getModeVtlFile(Mode mode) throws UnknownDataFormatException, MissingMandatoryFieldException, KraftwerkException {
		Path userInputFile = inputDirectory.resolve(Constants.USER_INPUT_FILE);
		try {
			JsonNode userInputs = JsonReader.read(userInputFile, fileUtilsInterface);
			JsonNode filesNode = userInputs.get("survey_data");
			for (JsonNode fileNode : filesNode) {
				String dataMode = readField(fileNode, "data_mode");
				if (dataMode == null) {break;}
				if (dataMode.equals(mode.name())) {
					return fileUtilsInterface.convertToPath(readField(fileNode, "mode_specifications"),inputDirectory);
				}
			}
		} catch (IOException e) {
			log.error("Unable to read user input file: {} , {}", userInputFile, e);
			throw new UnknownDataFormatException(e.getMessage());
		}
		return null;
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
	 * @param specDirectory directory where the DDI file should be
	 * @return Path of the DDI file
	 * @throws IOException – if an I/O error is thrown when accessing the starting file
	 */
	public Path findDDIFile(Path specDirectory) throws KraftwerkException {
		return Path.of(fileUtilsInterface.findFile(String.valueOf(specDirectory),Constants.DDI_FILE_REGEX));
	}

	/**
	 * Find the Lunatic file in the folder of a campaign
	 * @param specDirectory  directory where the Lunatic file should be
	 * @return Path of the Lunatic file
	 * @throws IOException  – if an I/O error is thrown when accessing the starting file
	 */
	public Path findLunaticFile(Path specDirectory) throws KraftwerkException {
		return Path.of(fileUtilsInterface.findFile(String.valueOf(specDirectory),Constants.LUNATIC_FILE_REGEX));
	}
}
