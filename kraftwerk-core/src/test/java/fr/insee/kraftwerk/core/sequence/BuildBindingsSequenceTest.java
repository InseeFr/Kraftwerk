package fr.insee.kraftwerk.core.sequence;

import fr.insee.kraftwerk.core.TestConstants;
import fr.insee.kraftwerk.core.exceptions.KraftwerkException;
import fr.insee.kraftwerk.core.inputs.UserInputsFile;
import fr.insee.kraftwerk.core.metadata.MetadataModel;
import fr.insee.kraftwerk.core.metadata.UcqVariable;
import fr.insee.kraftwerk.core.metadata.Variable;
import fr.insee.kraftwerk.core.metadata.VariableType;
import fr.insee.kraftwerk.core.vtl.VtlBindings;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.nio.file.Path;

import static org.junit.Assert.assertThrows;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class BuildBindingsSequenceTest {
	
	private static final Path inputSamplesDirectory = Path.of(TestConstants.UNIT_TESTS_DIRECTORY, "user_inputs");


	@Test
	void buildVtlBindings_errorWithoutMetadata() throws KraftwerkException {
		//GIVEN
		UserInputsFile userInputsFile = new UserInputsFile(
				inputSamplesDirectory.resolve("inputs_valid.json"),
				inputSamplesDirectory);
		String dataMode = "CAPI";
		VtlBindings vtlBindings = new VtlBindings();
		boolean withAllReportingData = false;
		boolean withDdi = true;	
		BuildBindingsSequence bbs = new BuildBindingsSequence(withAllReportingData);
		//WHEN 
		MetadataModel metadata = null;
		
		//THEN
		assertThrows(NullPointerException.class, () -> bbs.buildVtlBindings(userInputsFile, dataMode, vtlBindings, metadata, withDdi));
		
	}
	
	@ParameterizedTest
	@CsvSource({"true,true", "true,false", "false,false", "false,true"})
	void buildVtlBindings_success_changingDdi_and_reportingData(boolean withDdi, boolean withAllReportingData ) throws KraftwerkException {
		//GIVEN
		UserInputsFile userInputsFile = new UserInputsFile(
				inputSamplesDirectory.resolve("inputs_valid.json"),
				inputSamplesDirectory);
		String dataMode = "CAPI";
		VtlBindings vtlBindings = new VtlBindings();
		BuildBindingsSequence bbs = new BuildBindingsSequence(withAllReportingData);
        MetadataModel capiMetadata = new MetadataModel();
		capiMetadata.getVariables().putVariable(new Variable("VAR1", capiMetadata.getRootGroup(), VariableType.STRING));
		capiMetadata.getVariables().putVariable(new UcqVariable("PAYSNAIS", capiMetadata.getRootGroup(), VariableType.STRING));

		//WHEN
		//THEN
		assertDoesNotThrow(() -> bbs.buildVtlBindings(userInputsFile, dataMode, vtlBindings, capiMetadata, withDdi));
	}
	
	
}
