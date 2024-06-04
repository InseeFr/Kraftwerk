package fr.insee.kraftwerk.core.outputs;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.List;
import java.util.Set;

import fr.insee.kraftwerk.core.utils.SqlUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import fr.insee.kraftwerk.core.Constants;
import fr.insee.kraftwerk.core.TestConstants;
import fr.insee.kraftwerk.core.inputs.UserInputsFile;
import fr.insee.kraftwerk.core.outputs.csv.CsvOutputFiles;
import fr.insee.kraftwerk.core.vtl.VtlBindings;
import fr.insee.vtl.model.Dataset;
import fr.insee.vtl.model.InMemoryDataset;
import fr.insee.vtl.model.Structured;

@TestMethodOrder(OrderAnnotation.class)
class CsvOutputFilesTest {

	private static UserInputsFile testUserInputsFile;
	private static OutputFiles outputFiles;

	Dataset fooDataset = new InMemoryDataset(List.of(),
			List.of(new Structured.Component("FOO", String.class, Dataset.Role.IDENTIFIER)));

	@Test
	@Order(1)
	void createInstance() {
		assertDoesNotThrow(() -> {
			//
			testUserInputsFile = new UserInputsFile(
					Path.of(TestConstants.UNIT_TESTS_DIRECTORY, "user_inputs/inputs_valid_several_modes.json"),
					Path.of(TestConstants.UNIT_TESTS_DIRECTORY,"user_inputs"));
			//
			VtlBindings vtlBindings = new VtlBindings();
			for (String mode : testUserInputsFile.getModes()) {
				vtlBindings.put(mode, fooDataset);
			}
			vtlBindings.put(testUserInputsFile.getMultimodeDatasetName(), fooDataset);
			vtlBindings.put(Constants.ROOT_GROUP_NAME, fooDataset);
			vtlBindings.put("LOOP", fooDataset);
			vtlBindings.put("FROM_USER", fooDataset);
			//
			outputFiles = new CsvOutputFiles(Paths.get(TestConstants.UNIT_TESTS_DUMP), vtlBindings, testUserInputsFile.getModes(), SqlUtils.openConnection().createStatement());
		});
	}

	@Test
	@Order(2)
	void testGetDatasetOutputNames() {
		//
		Set<String> outputDatasetNames = outputFiles.getDatasetToCreate();

		//
		for (String mode : testUserInputsFile.getModes()) {
			assertFalse(outputDatasetNames.contains(mode));
		}
		assertFalse(outputDatasetNames.contains(testUserInputsFile.getMultimodeDatasetName()));
		assertTrue(outputDatasetNames.containsAll(Set.of(Constants.ROOT_GROUP_NAME, "LOOP", "FROM_USER")));
	}

	@AfterAll
    static void closeConnection() throws SQLException {
		outputFiles.getDatabase().getConnection().close();
	}
}
