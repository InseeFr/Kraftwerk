package fr.insee.kraftwerk.core.outputs;

import fr.insee.kraftwerk.core.Constants;
import fr.insee.kraftwerk.core.TestConstants;
import fr.insee.kraftwerk.core.inputs.UserInputs;
import fr.insee.kraftwerk.core.vtl.VtlBindings;
import fr.insee.vtl.model.Dataset;
import fr.insee.vtl.model.InMemoryDataset;
import fr.insee.vtl.model.Structured;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import java.io.File;
import java.nio.file.Paths;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(OrderAnnotation.class)
public class OutputFilesTest {

	private static UserInputs testUserInputs;
	private static OutputFiles outputFiles;

	Dataset fooDataset = new InMemoryDataset(
			List.of(),
			List.of(new Structured.Component("FOO", String.class, Dataset.Role.IDENTIFIER))
	);

	@Test
	@Order(1)
	public void createInstance() {
		assertDoesNotThrow(() -> {
			//
			testUserInputs = new UserInputs(
					TestConstants.UNIT_TESTS_DIRECTORY + "/user_inputs/inputs_valid_several_modes.json",
					Paths.get("/whatever/in"));
			//
			VtlBindings vtlBindings = new VtlBindings();
			for (String mode : testUserInputs.getModes()) {
				vtlBindings.getBindings().put(mode, fooDataset);
			}
			vtlBindings.getBindings().put(testUserInputs.getMultimodeDatasetName(), fooDataset);
			vtlBindings.getBindings().put(Constants.ROOT_GROUP_NAME, fooDataset);
			vtlBindings.getBindings().put("LOOP", fooDataset);
			vtlBindings.getBindings().put("FROM_USER", fooDataset);
			//
			outputFiles = new OutputFiles(
					Paths.get(TestConstants.UNIT_TESTS_DUMP),
					vtlBindings, testUserInputs);
		});
	}

	@Test
	@Order(2)
	public void testGetDatasetOutputNames() {
		//
		Set<String> outputDatasetNames = outputFiles.getOutputDatasetNames();

		//
		for (String mode : testUserInputs.getModes()) {
			assertFalse(outputDatasetNames.contains(mode));
		}
		assertFalse(outputDatasetNames.contains(testUserInputs.getMultimodeDatasetName()));
		assertTrue(outputDatasetNames.containsAll(Set.of(Constants.ROOT_GROUP_NAME, "LOOP", "FROM_USER")));
	}

	@Test
	@Order(3)
	public void moveFiles() {
		//
		testUserInputs = new UserInputs(
				TestConstants.UNIT_TESTS_DIRECTORY + "/move_files/move_files.json",
				Paths.get(TestConstants.UNIT_TESTS_DIRECTORY + "/move_files"));
		outputFiles.moveInputFile(testUserInputs);
		
	assertTrue(new File(TestConstants.UNIT_TESTS_DIRECTORY + "/move_files/Archive/papier").exists());
	assertTrue(new File(TestConstants.UNIT_TESTS_DIRECTORY + "/move_files/Archive/web").exists());
	assertTrue(new File(TestConstants.UNIT_TESTS_DIRECTORY + "/move_files/Archive/paradata/L0000010.json").exists());
	assertTrue(new File(TestConstants.UNIT_TESTS_DIRECTORY + "/move_files/Archive/suivi/reportingdata.xml").exists());
	}
}
