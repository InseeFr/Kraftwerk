package fr.insee.kraftwerk.core.inputs;

import fr.insee.kraftwerk.core.TestConstants;
import fr.insee.kraftwerk.core.data.model.Mode;
import fr.insee.kraftwerk.core.exceptions.KraftwerkException;
import fr.insee.kraftwerk.core.parsers.DataFormat;
import fr.insee.kraftwerk.core.utils.files.FileSystemImpl;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class UserInputsGenesisTest {

	private static final Path inputSamplesDirectory = Path.of(TestConstants.UNIT_TESTS_DIRECTORY, "user_inputs");

	@Test
	void testReadValidUserInput_singleMode() throws KraftwerkException {
		List<Mode> modes = new ArrayList<>();
		modes.add(Mode.F2F);

		UserInputsGenesis userInputsGenesis = new UserInputsGenesis(false, inputSamplesDirectory.resolve("Valid"), modes, new FileSystemImpl(TestConstants.TEST_RESOURCES_DIRECTORY), true);
		//
		ModeInputs modeInputs = userInputsGenesis.getModeInputs("F2F");
		assertNotNull(modeInputs.getDdiUrl());
		assertEquals(DataFormat.LUNATIC_XML, modeInputs.getDataFormat());
		assertNull(modeInputs.getParadataFolder());
		assertNull(modeInputs.getModeVtlFile());
		//
		assertNull(userInputsGenesis.getVtlReconciliationFile());
		assertNull(userInputsGenesis.getVtlTransformationsFile());
		assertNull(userInputsGenesis.getVtlInformationLevelsFile());
	}

	@Test
	void testReadValidUserInput_singleMode_lunatic_only() throws KraftwerkException {
		List<Mode> modes = new ArrayList<>();
		modes.add(Mode.F2F);

		UserInputsGenesis userInputsGenesis = new UserInputsGenesis(false, inputSamplesDirectory.resolve("Valid"), modes, new FileSystemImpl(TestConstants.TEST_RESOURCES_DIRECTORY), false);
		//
		ModeInputs modeInputs = userInputsGenesis.getModeInputs("F2F");
		assertNull(modeInputs.getDdiUrl());
		assertNotNull(modeInputs.getLunaticFile());
		assertEquals(DataFormat.LUNATIC_XML, modeInputs.getDataFormat());
		assertNull(modeInputs.getParadataFolder());
		assertNull(modeInputs.getModeVtlFile());
		//
		assertNull(userInputsGenesis.getVtlReconciliationFile());
		assertNull(userInputsGenesis.getVtlTransformationsFile());
		assertNull(userInputsGenesis.getVtlInformationLevelsFile());
	}
}
