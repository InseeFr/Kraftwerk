package fr.insee.kraftwerk.core.sequence;

import fr.insee.kraftwerk.core.TestConstants;
import fr.insee.kraftwerk.core.exceptions.KraftwerkException;
import fr.insee.kraftwerk.core.exceptions.NullException;
import fr.insee.kraftwerk.core.extradata.paradata.ParadataParser;
import fr.insee.kraftwerk.core.inputs.ModeInputs;
import fr.insee.kraftwerk.core.inputs.UserInputsFile;
import fr.insee.bpm.metadata.model.MetadataModel;
import fr.insee.bpm.metadata.model.UcqVariable;
import fr.insee.bpm.metadata.model.Variable;
import fr.insee.bpm.metadata.model.VariableType;
import fr.insee.kraftwerk.core.utils.files.FileSystemImpl;
import fr.insee.kraftwerk.core.utils.files.FileUtilsInterface;
import fr.insee.kraftwerk.core.vtl.VtlBindings;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.file.Path;

import static org.junit.Assert.assertThrows;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BuildBindingsSequenceTest {
	
	private static final Path inputSamplesDirectory = Path.of(TestConstants.UNIT_TESTS_DIRECTORY, "user_inputs");
	private static final FileUtilsInterface fileUtilsInterface = new FileSystemImpl(TestConstants.TEST_RESOURCES_DIRECTORY);

	@Spy
	@InjectMocks
	private BuildBindingsSequence spyBuildBindingsSequence = new BuildBindingsSequence(new FileSystemImpl(TestConstants.TEST_RESOURCES_DIRECTORY));

	@Test
	void buildVtlBindings_errorWithoutMetadata() throws KraftwerkException {
		//GIVEN
		UserInputsFile userInputsFile = new UserInputsFile(
				inputSamplesDirectory.resolve("inputs_valid.json"),
				inputSamplesDirectory, fileUtilsInterface);
		String dataMode = "CAPI";
		VtlBindings vtlBindings = new VtlBindings();
		boolean withDdi = true;	
		BuildBindingsSequence bbs = new BuildBindingsSequence(new FileSystemImpl(TestConstants.TEST_RESOURCES_DIRECTORY));
		//WHEN 
		MetadataModel metadata = null;
		
		//THEN
		assertThrows(NullPointerException.class, () -> bbs.buildVtlBindings(userInputsFile, dataMode, vtlBindings, metadata, withDdi, null));

	}
	
	@ParameterizedTest
	@CsvSource({"true,true", "true,false", "false,false", "false,true"})
	void buildVtlBindings_success_changingDdi_and_reportingData(boolean withDdi, boolean withAllReportingData ) throws KraftwerkException {
		//GIVEN
		UserInputsFile userInputsFile = new UserInputsFile(
				inputSamplesDirectory.resolve("inputs_valid.json"),
				inputSamplesDirectory, fileUtilsInterface);
		String dataMode = "CAPI";
		VtlBindings vtlBindings = new VtlBindings();
		BuildBindingsSequence bbs = new BuildBindingsSequence(new FileSystemImpl(TestConstants.TEST_RESOURCES_DIRECTORY));
        MetadataModel capiMetadata = new MetadataModel();
		capiMetadata.getVariables().putVariable(new Variable("VAR1", capiMetadata.getRootGroup(), VariableType.STRING));
		capiMetadata.getVariables().putVariable(new UcqVariable("PAYSNAIS", capiMetadata.getRootGroup(), VariableType.STRING));

		//WHEN + THEN
		assertDoesNotThrow(() -> bbs.buildVtlBindings(userInputsFile, dataMode, vtlBindings, capiMetadata, withDdi, null));
	}


	@Test
	void parseParadata_ModeInputsNotSet_Test() throws NullException {
		// 1. Mock the dependencies
		ModeInputs mockModeInputs = mock(ModeInputs.class); //"getParadataFolder()" not set here to test null value
		ParadataParser mockParadataParser = mock(ParadataParser.class);

		// 2. Launch test
		spyBuildBindingsSequence.parseParadata(mockModeInputs, null);

		// 3. checks
		verify(mockParadataParser, times(0)).parseParadata(any(), any());
	}

	@Test
	void parseParadata_Test() throws NullException {
		// 1. Mock the dependencies
		ModeInputs mockModeInputs = mock(ModeInputs.class);
		doReturn(Path.of("a")).when(mockModeInputs).getParadataFolder();
		ParadataParser mockParadataParser = mock(ParadataParser.class);
		doReturn(mockParadataParser).when(spyBuildBindingsSequence).getParadataParser(any());
		doNothing().when(mockParadataParser).parseParadata(any(), any());

		// 2. Launch test
		spyBuildBindingsSequence.parseParadata(mockModeInputs, null);

		// 3. checks
		verify(mockParadataParser, times(1)).parseParadata(any(), any());
	}

}
