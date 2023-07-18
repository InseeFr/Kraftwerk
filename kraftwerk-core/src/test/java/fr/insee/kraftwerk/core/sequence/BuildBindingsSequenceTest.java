package fr.insee.kraftwerk.core.sequence;

import static org.junit.Assert.assertThrows;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import fr.insee.kraftwerk.core.TestConstants;
import fr.insee.kraftwerk.core.exceptions.KraftwerkException;
import fr.insee.kraftwerk.core.inputs.UserInputs;
import fr.insee.kraftwerk.core.metadata.UcqVariable;
import fr.insee.kraftwerk.core.metadata.Variable;
import fr.insee.kraftwerk.core.metadata.VariableType;
import fr.insee.kraftwerk.core.metadata.VariablesMap;
import fr.insee.kraftwerk.core.vtl.VtlBindings;

class BuildBindingsSequenceTest {
	
	private static final Path inputSamplesDirectory = Path.of(TestConstants.UNIT_TESTS_DIRECTORY, "user_inputs");


	@Test
	void buildVtlBindings_errorWithoutMetadata() throws KraftwerkException {
		//GIVEN
		UserInputs userInputs = new UserInputs(
				inputSamplesDirectory.resolve("inputs_valid.json"),
				inputSamplesDirectory);
		String dataMode = "CAPI";
		VtlBindings vtlBindings = new VtlBindings();
		boolean withAllReportingData = false;
		boolean withDdi = true;	
		BuildBindingsSequence bbs = new BuildBindingsSequence(withAllReportingData);
		//WHEN 
		Map<String,VariablesMap> metadataVariables = new HashMap<>();
		
		//THEN
		assertThrows(NullPointerException.class, () -> bbs.buildVtlBindings(userInputs, dataMode, vtlBindings, metadataVariables, withDdi));
		
	}
	
	@ParameterizedTest
	@CsvSource({"true,true", "true,false", "false,false", "false,true"})
	void buildVtlBindings_success_changingDdi_and_reportingData(boolean withDdi, boolean withAllReportingData ) throws KraftwerkException {
		//GIVEN
		UserInputs userInputs = new UserInputs(
				inputSamplesDirectory.resolve("inputs_valid.json"),
				inputSamplesDirectory);
		String dataMode = "CAPI";
		VtlBindings vtlBindings = new VtlBindings();
		BuildBindingsSequence bbs = new BuildBindingsSequence(withAllReportingData);
		Map<String,VariablesMap> metadataVariables = new HashMap<>();
        VariablesMap capiVariables = new VariablesMap();
        capiVariables.putVariable(new Variable("VAR1", capiVariables.getRootGroup(), VariableType.STRING));
        capiVariables.putVariable(new UcqVariable("PAYSNAIS", capiVariables.getRootGroup(), VariableType.STRING));
		metadataVariables.put(dataMode, capiVariables);
		
		//WHEN
		//THEN
		assertDoesNotThrow(() -> bbs.buildVtlBindings(userInputs, dataMode, vtlBindings, metadataVariables, withDdi));
	}
	
	
}
