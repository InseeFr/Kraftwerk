package fr.insee.kraftwerk.outputs;

import fr.insee.kraftwerk.Constants;
import fr.insee.kraftwerk.TestConstants;
import fr.insee.kraftwerk.inputs.UserInputs;
import fr.insee.kraftwerk.vtl.VtlBindings;
import fr.insee.vtl.model.Dataset;
import fr.insee.vtl.model.InMemoryDataset;
import fr.insee.vtl.model.Structured;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import java.nio.file.Paths;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(OrderAnnotation.class)
public class OutputTablesTest {

	private static UserInputs testUserInputs;
	private static OutputTables outputTables;

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
			outputTables = new OutputTables(
					Paths.get(TestConstants.UNIT_TESTS_DUMP),
					vtlBindings, testUserInputs);
		});
	}

	@Test
	@Order(2)
	public void testGetDatasetOutputNames() {
		//
		Set<String> outputDatasetNames = outputTables.getOutputDatasetNames();

		//
		for (String mode : testUserInputs.getModes()) {
			assertFalse(outputDatasetNames.contains(mode));
		}
		assertFalse(outputDatasetNames.contains(testUserInputs.getMultimodeDatasetName()));
		assertTrue(outputDatasetNames.containsAll(Set.of(Constants.ROOT_GROUP_NAME, "LOOP", "FROM_USER")));
	}
}
