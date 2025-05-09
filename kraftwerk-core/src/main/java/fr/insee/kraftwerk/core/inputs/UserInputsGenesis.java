package fr.insee.kraftwerk.core.inputs;

import com.fasterxml.jackson.databind.JsonNode;
import fr.insee.kraftwerk.core.Constants;
import fr.insee.kraftwerk.core.data.model.Mode;
import fr.insee.kraftwerk.core.exceptions.KraftwerkException;
import fr.insee.kraftwerk.core.exceptions.MissingMandatoryFieldException;
import fr.insee.kraftwerk.core.exceptions.UnknownDataFormatException;
import fr.insee.kraftwerk.core.utils.JsonReader;
import fr.insee.kraftwerk.core.utils.files.FileUtilsInterface;
import lombok.extern.log4j.Log4j2;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@Log4j2
public class UserInputsGenesis extends UserInputs{

	private final List<Mode> modes;

	public UserInputsGenesis(Path specsDirectory, List<Mode> modes, FileUtilsInterface fileUtilsInterface, boolean withDDI) throws KraftwerkException {
		super(specsDirectory, fileUtilsInterface);
		this.modes=modes;
		computeInputs(withDDI);
	}

	private void computeInputs(boolean withDDI) throws KraftwerkException {
		for (Mode mode : modes) {
			ModeInputs modeInputs = getModeInputs(mode, withDDI);
			modeInputsMap.put(mode.name(), modeInputs);
		}
		// Add user VTL script on final data (information levels step) if exists
		try {
			setVtlInformationLevelsFile(Path.of(fileUtilsInterface.findFile(String.valueOf(inputDirectory),Constants.SCRIPT_FINAL_REGEX)));
		} catch (KraftwerkException e) {
			// Script is optional, if not found the program should continue normally
			log.info("No final script found in specs directory");
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
		modeInputs.setModeVtlFile(getModeVtlFile(mode));
        return modeInputs;
    }

	/**
	 * Build the path to the unimodal user script for the requested mode if it exists
	 * @param mode mode requested
	 * @return Path to the script
	 */
    private Path getModeVtlFile(Mode mode) {
		try {
			return Path.of(fileUtilsInterface.findFile(String.valueOf(inputDirectory.resolve(mode.name())),Constants.SCRIPT_UNIMODAL_REGEX));
		} catch (KraftwerkException e) {
            return null;
        }
	}

	public List<String> getModes() {
		return new ArrayList<>(modeInputsMap.keySet());
	}

	/**
	 * Find the DDI file in the folder of a campaign
	 * @param specDirectory directory where the DDI file should be
	 * @return Path of the DDI file
	 * @throws KraftwerkException – if an error is thrown when accessing the starting file
	 */
	public Path findDDIFile(Path specDirectory) throws KraftwerkException {
		return Path.of(fileUtilsInterface.findFile(String.valueOf(specDirectory),Constants.DDI_FILE_REGEX));
	}

	/**
	 * Find the Lunatic file in the folder of a campaign
	 * @param specDirectory  directory where the Lunatic file should be
	 * @return Path of the Lunatic file
	 * @throws KraftwerkException  – if an error is thrown when accessing the starting file
	 */
	public Path findLunaticFile(Path specDirectory) throws KraftwerkException {
		return Path.of(fileUtilsInterface.findFile(String.valueOf(specDirectory),Constants.LUNATIC_FILE_REGEX));
	}
}
