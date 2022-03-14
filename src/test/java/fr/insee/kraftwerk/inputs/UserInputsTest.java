package fr.insee.kraftwerk.inputs;

import fr.insee.kraftwerk.TestConstants;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

public class UserInputsTest {

	private static final String inputSamplesDirectory = TestConstants.UNIT_TESTS_DIRECTORY +  "/user_inputs";

	@ParameterizedTest
	@ValueSource(strings = {
			// some files are disables since we don't have anonymized data yet
			"kraftwerk_inputs_simpsons1.json",
			"kraftwerk_inputs_simpsons2.json",
			"kraftwerk_inputs_vqs.json"
			//"kraftwerk_inputs_tic.json",
			//"kraftwerk_inputs_log-t01.json",
			//"kraftwerk_inputs_log-x01.json",
			//"kraftwerk_inputs_log-x12.json",
			//"kraftwerk_inputs_log-x21.json"
	})
	public void testReadUserInput(String jsonInputFileName) {
		String jsonInputFile = inputSamplesDirectory + "/" + jsonInputFileName;
		assertDoesNotThrow(() ->
				new UserInputs(jsonInputFile, Paths.get(inputSamplesDirectory)));
	}

/*	@Test
	public void testReadValidUserInput_singleMode() {
		UserInputs userInputs = new UserInputs(
				inputSamplesDirectory + "/inputs_valid.json",
				"/whatever/in");
		//
		assertEquals("sample_files_to_get", userInputs.getCampaignName());
		//
		ModeInputs modeInputs = userInputs.getModeInputs("CAPI");
		assertEquals("/whatever/in/sample_files_to_get/data.xml", modeInputs.getDataFile());
		assertEquals("/whatever/in/sample_files_to_get/ddi.xml", modeInputs.getDDIFile());
		assertEquals(DataFormat.LUNATIC_XML, modeInputs.getDataFormat());
		assertNull(modeInputs.getParadataFolder());
		assertNull(modeInputs.getReportingDataFile());
		assertNull(modeInputs.getModeVtlFile());
		//
		assertEquals(userInputs.getMultimodeDatasetName(), "MULTIMODE");
		assertNull(userInputs.getVtlReconciliationFile());
		assertNull(userInputs.getVtlTransformationsFile());
		assertNull(userInputs.getVtlInformationLevelsFile());
	}*/

	@Test
	public void testReadValidUserInput_missingOptionalFields() {
		assertDoesNotThrow(() -> new UserInputs(
				inputSamplesDirectory + "/inputs_valid_missing_fields.json",
				Paths.get("/whatever/in")));
	}

	/*@Test
	public void testReadValidUserInput_severalModes() {
		UserInputs userInputs = new UserInputs(
				inputSamplesDirectory + "/inputs_valid_several_modes.json",
				"/whatever/in");
		//
		assertEquals("TEST2", userInputs.getCampaignName());
		//
		assertTrue(userInputs.getModes().containsAll(Set.of("CAPI", "CAWI", "PAPI")));
		//
		ModeInputs modeInputs = userInputs.getModeInputs("CAWI");
		assertEquals("/whatever/in/sample_files_to_get/data.xml", modeInputs.getDataFile());
		assertEquals("/whatever/in/sample_files_to_get/ddi_web.xml", modeInputs.getDDIFile());
		assertEquals(DataFormat.LUNATIC_XML, modeInputs.getDataFormat());
		assertEquals("/whatever/in/sample_files_to_get/paradata", modeInputs.getParadataFolder());
		assertEquals("/whatever/in/sample_files_to_get/moog_data.csv", modeInputs.getReportingDataFile());
		assertEquals("/whatever/in/sample_files_to_get/web.vtl", modeInputs.getModeVtlFile());
		//
		assertEquals(userInputs.getMultimodeDatasetName(), "MULTIMODE");
		assertNull(userInputs.getVtlReconciliationFile());
		assertEquals(userInputs.getVtlTransformationsFile(), "test2.vtl");
		assertNull(userInputs.getVtlInformationLevelsFile());
	}*/

	@Test
	public void testReadInvalidUserInput_wrongDataFormat() {
		assertThrows(UnknownDataFormatException.class, () -> {
			new UserInputs(
					inputSamplesDirectory + "/inputs_invalid_data_format.json",
					Paths.get("/whatever/in/"));
		});
	}

	@Test
	public void testReadInvalidUserInput_wrongFieldNames() {
		assertThrows(MissingMandatoryFieldException.class, () -> {
			new UserInputs(
					inputSamplesDirectory + "/inputs_invalid_field_names.json",
					Paths.get("/whatever/in/"));
		});
	}

	/*@Test // TODO: see UserInputs
	public void testReadMalformedInput() {
		new UserInputs(
				inputSamplesDirectory + "/inputs_invalid_malformed.json",
				"/whatever/in");
	}*/

}
