package fr.insee.kraftwerk.core.inputs;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.util.Set;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import fr.insee.kraftwerk.core.TestConstants;
import fr.insee.kraftwerk.core.parsers.DataFormat;

class UserInputsTest {

	private static final Path inputSamplesDirectory = Path.of(TestConstants.UNIT_TESTS_DIRECTORY, "user_inputs");

	@Test
	void testReadValidUserInput_singleMode() {
		UserInputs userInputs = new UserInputs(
				inputSamplesDirectory.resolve("inputs_valid.json"),
				inputSamplesDirectory);
		//
		ModeInputs modeInputs = userInputs.getModeInputs("CAPI");
		assertNotNull(modeInputs.getDataFile());
		assertNotNull(modeInputs.getDdiUrl());
		assertEquals(DataFormat.LUNATIC_XML, modeInputs.getDataFormat());
		assertNull(modeInputs.getParadataFolder());
		assertNull(modeInputs.getReportingDataFile());
		assertNull(modeInputs.getModeVtlFile());
		//
		assertEquals(userInputs.getMultimodeDatasetName(), "MULTIMODE");
		assertNull(userInputs.getVtlReconciliationFile());
		assertNull(userInputs.getVtlTransformationsFile());
		assertNull(userInputs.getVtlInformationLevelsFile());
	}

	@Test
	void testReadValidUserInput_missingOptionalFields() {
		assertDoesNotThrow(() -> new UserInputs(
				inputSamplesDirectory.resolve("inputs_valid_missing_fields.json"),
				inputSamplesDirectory));
	}

	@Test
	void testReadValidUserInput_severalModes() {
		UserInputs userInputs = new UserInputs(
				inputSamplesDirectory.resolve("inputs_valid_several_modes.json"),
				inputSamplesDirectory);
		//
		assertTrue(userInputs.getModes().containsAll(Set.of("CAPI", "CAWI", "PAPI")));
		//
		ModeInputs modeInputs = userInputs.getModeInputs("CAWI");
		assertNotNull(modeInputs.getDataFile());
		assertNotNull(modeInputs.getDdiUrl());
		assertEquals(DataFormat.LUNATIC_XML, modeInputs.getDataFormat());
		assertNotNull(modeInputs.getParadataFolder());
		assertNotNull(modeInputs.getReportingDataFile());
		assertNotNull(modeInputs.getModeVtlFile());
		//
		assertEquals("MULTIMODE", userInputs.getMultimodeDatasetName());
		assertNull(userInputs.getVtlReconciliationFile());
		assertEquals("test2.vtl", userInputs.getVtlTransformationsFile().getFileName().toString());
		assertNull(userInputs.getVtlInformationLevelsFile());
	}

	@Test
	void testReadInvalidUserInput_wrongDataFormat() {
		assertThrows(UnknownDataFormatException.class, () -> {
			new UserInputs(
					inputSamplesDirectory.resolve("inputs_invalid_data_format.json"),
					inputSamplesDirectory);
		});
	}

	@Test
	public void testReadInvalidUserInput_wrongFieldNames() {
		assertThrows(MissingMandatoryFieldException.class, () -> {
			new UserInputs(
					inputSamplesDirectory.resolve("inputs_invalid_field_names.json"),
					inputSamplesDirectory);
		});
	}

	@Test
	@Disabled("Management of malformed user input file not implemented.") // TODO: see UserInputs
	public void testReadMalformedInput() {
		new UserInputs(
				inputSamplesDirectory.resolve("inputs_invalid_malformed.json"),
				inputSamplesDirectory);
	}

}
