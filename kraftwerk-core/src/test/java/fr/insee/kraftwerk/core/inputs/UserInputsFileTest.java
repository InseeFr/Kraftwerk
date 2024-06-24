package fr.insee.kraftwerk.core.inputs;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.util.Set;

import fr.insee.kraftwerk.core.utils.files.FileSystemImpl;
import fr.insee.kraftwerk.core.utils.files.FileUtilsInterface;
import org.junit.jupiter.api.Test;

import fr.insee.kraftwerk.core.TestConstants;
import fr.insee.kraftwerk.core.exceptions.KraftwerkException;
import fr.insee.kraftwerk.core.exceptions.MissingMandatoryFieldException;
import fr.insee.kraftwerk.core.exceptions.UnknownDataFormatException;
import fr.insee.kraftwerk.core.parsers.DataFormat;

class UserInputsFileTest {

	private static final Path inputSamplesDirectory = Path.of(TestConstants.UNIT_TESTS_DIRECTORY, "user_inputs");
	private static final FileUtilsInterface fileUtilsInterface = new FileSystemImpl();


	@Test
	void testReadValidUserInput_singleMode() throws KraftwerkException {
		UserInputsFile userInputsFile = new UserInputsFile(
				inputSamplesDirectory.resolve("kraftwerk.json"),
				inputSamplesDirectory, fileUtilsInterface);
		//
		ModeInputs modeInputs = userInputsFile.getModeInputs("CAPI");
		assertNotNull(modeInputs.getDataFile());
		assertNotNull(modeInputs.getDdiUrl());
		assertEquals(DataFormat.LUNATIC_XML, modeInputs.getDataFormat());
		assertNull(modeInputs.getParadataFolder());
		assertNotNull(modeInputs.getReportingDataFile());
		assertNull(modeInputs.getModeVtlFile());
		//
		assertEquals( "MULTIMODE", userInputsFile.getMultimodeDatasetName());
		assertNull(userInputsFile.getVtlReconciliationFile());
		assertNull(userInputsFile.getVtlTransformationsFile());
		assertNull(userInputsFile.getVtlInformationLevelsFile());
	}

	@Test
	void testReadValidUserInput_missingOptionalFields() {
		assertDoesNotThrow(() -> new UserInputsFile(
				inputSamplesDirectory.resolve("inputs_valid_missing_fields.json"),
				inputSamplesDirectory, fileUtilsInterface));
	}

	@Test
	void testReadValidUserInput_severalModes() throws KraftwerkException {
		UserInputsFile userInputsFile = new UserInputsFile(
				inputSamplesDirectory.resolve("inputs_valid_several_modes.json"),
				inputSamplesDirectory, fileUtilsInterface);
		//
		assertTrue(userInputsFile.getModes().containsAll(Set.of("CAPI", "CAWI", "PAPI")));
		//
		ModeInputs modeInputs = userInputsFile.getModeInputs("CAWI");
		assertNotNull(modeInputs.getDataFile());
		assertNotNull(modeInputs.getDdiUrl());
		assertEquals(DataFormat.LUNATIC_XML, modeInputs.getDataFormat());
		assertNotNull(modeInputs.getParadataFolder());
		assertNotNull(modeInputs.getReportingDataFile());
		assertNotNull(modeInputs.getModeVtlFile());
		//
		assertEquals("MULTIMODE", userInputsFile.getMultimodeDatasetName());
		assertNull(userInputsFile.getVtlReconciliationFile());
		assertEquals("test2.vtl", userInputsFile.getVtlTransformationsFile().getFileName().toString());
		assertNull(userInputsFile.getVtlInformationLevelsFile());
	}

	@Test
	void testReadInvalidUserInput_wrongDataFormat() {
		Path path = inputSamplesDirectory.resolve("inputs_invalid_data_format.json");
		assertThrows(UnknownDataFormatException.class, () -> {
			new UserInputsFile(path,inputSamplesDirectory, fileUtilsInterface);
		});
	}

	@Test
	void testReadInvalidUserInput_wrongFieldNames() {
		Path path = inputSamplesDirectory.resolve("inputs_invalid_field_names.json");
		assertThrows(MissingMandatoryFieldException.class, () -> {
			new UserInputsFile(	path,inputSamplesDirectory, fileUtilsInterface);
		});
	}

	@Test
	void testReadMalformedInput() throws KraftwerkException {
		Path path = inputSamplesDirectory.resolve("inputs_invalid_malformed.json");
		assertThrows(UnknownDataFormatException.class, () -> {
			new UserInputsFile(path,inputSamplesDirectory, fileUtilsInterface);
		});
	}
	
	@Test
	void testReadInputMissingFile() throws KraftwerkException {
		Path path = inputSamplesDirectory.resolve("inputs_invalid_several_modes_fileNotExist.json");
		assertThrows(KraftwerkException.class, () -> {
			new UserInputsFile(path,inputSamplesDirectory, fileUtilsInterface);
		});
	}
	
	@Test
	void testReadInputCompletePath() throws KraftwerkException {
		Path path = inputSamplesDirectory.resolve("inputs_valid_several_modes_completePath.json");
		assertDoesNotThrow(() -> {
			new UserInputsFile(path,inputSamplesDirectory, fileUtilsInterface);
		});
	}
	

}
