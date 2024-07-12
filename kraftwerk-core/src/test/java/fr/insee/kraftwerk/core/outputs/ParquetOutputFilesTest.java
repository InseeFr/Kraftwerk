package fr.insee.kraftwerk.core.outputs;

import fr.insee.kraftwerk.core.Constants;
import fr.insee.kraftwerk.core.TestConstants;
import fr.insee.kraftwerk.core.exceptions.KraftwerkException;
import fr.insee.kraftwerk.core.inputs.UserInputsFile;
import fr.insee.kraftwerk.core.metadata.Group;
import fr.insee.kraftwerk.core.metadata.MetadataModel;
import fr.insee.kraftwerk.core.metadata.Variable;
import fr.insee.kraftwerk.core.metadata.VariableType;
import fr.insee.kraftwerk.core.outputs.parquet.ParquetOutputFiles;
import fr.insee.kraftwerk.core.utils.FileUtils;
import fr.insee.kraftwerk.core.utils.SqlUtils;
import fr.insee.kraftwerk.core.vtl.VtlBindings;
import fr.insee.vtl.model.Dataset;
import fr.insee.vtl.model.Dataset.Role;
import fr.insee.vtl.model.InMemoryDataset;
import fr.insee.vtl.model.Structured;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;

class ParquetOutputFilesTest {

	private static UserInputsFile testUserInputs;
	private static ParquetOutputFiles outputFiles;
	private static Statement testDatabase;


	static Dataset testDataset = new InMemoryDataset(
			List.of(
					List.of("T01", "01", "foo11", 11L),
					List.of("T01", "02", "foo12", 12L),
					List.of("T02", "01", "foo21", 21L)
			),
			List.of(
					new Structured.Component("ID", String.class, Role.IDENTIFIER),
					new Structured.Component("ID2", String.class, Role.IDENTIFIER),
					new Structured.Component("FOO_STR", String.class, Role.MEASURE),
					new Structured.Component("FOO_NUM", Double.class, Role.MEASURE)
			)
	);

	@BeforeAll
    static void createInstance() throws SQLException {
		testDatabase = SqlUtils.openConnection().createStatement();
		assertDoesNotThrow(() -> {
			//
			testUserInputs = new UserInputsFile(
					Path.of(TestConstants.UNIT_TESTS_DIRECTORY, "user_inputs/inputs_valid_several_modes.json"),
					Path.of(TestConstants.UNIT_TESTS_DIRECTORY,"user_inputs"));
			//
			VtlBindings vtlBindings = new VtlBindings();
			for (String mode : testUserInputs.getModes()) {
				vtlBindings.put(mode, testDataset);
			}
			vtlBindings.put(testUserInputs.getMultimodeDatasetName(), testDataset);
			vtlBindings.put(Constants.ROOT_GROUP_NAME, testDataset);
			vtlBindings.put("LOOP", testDataset);
			vtlBindings.put("FROM_USER", testDataset);
			//
			SqlUtils.convertVtlBindingsIntoSqlDatabase(vtlBindings, testDatabase);
			outputFiles = new ParquetOutputFiles(Paths.get(TestConstants.UNIT_TESTS_DUMP), vtlBindings, testUserInputs.getModes(), testDatabase);
		});
	}

	@Test
	void testGetDatasetOutputNames() {
		//
		Set<String> outputDatasetNames = outputFiles.getDatasetToCreate();

		//
		for (String mode : testUserInputs.getModes()) {
			assertFalse(outputDatasetNames.contains(mode));
		}
		assertFalse(outputDatasetNames.contains(testUserInputs.getMultimodeDatasetName()));
		Assertions.assertTrue(outputDatasetNames.containsAll(Set.of(Constants.ROOT_GROUP_NAME, "LOOP", "FROM_USER")));
	}

	
	@Test
	void writeParquetFromDatasetTest() throws KraftwerkException {

		// Clean the existing file
//		Files.deleteIfExists(outputFiles.getOutputFolder());
		FileUtils.createDirectoryIfNotExist(outputFiles.getOutputFolder());

		Map<String, MetadataModel> metaModels = new HashMap<>();
		MetadataModel metMod = new MetadataModel();
		Group group = new Group("test","RACINE");
		metMod.getVariables().putVariable(new Variable("ID",group, VariableType.STRING));
		metMod.getVariables().putVariable(new Variable("ID2",group, VariableType.STRING));
		metMod.getVariables().putVariable(new Variable("FOO_STR",group, VariableType.STRING));
		metMod.getVariables().putVariable(new Variable("FOO_NUM",group, VariableType.NUMBER));
		metaModels.put("test",metMod);

		outputFiles.writeOutputTables();
		Path racinePath = Path.of(outputFiles.getOutputFolder().toString(), outputFiles.getAllOutputFileNames("RACINE").getFirst());
		racinePath = racinePath.resolveSibling(racinePath.getFileName()+".parquet");
		File f = racinePath.toFile();
		Assertions.assertTrue(f.exists());
		Assertions.assertNotEquals(0, f.length());
	}

	@AfterAll
    static void closeConnection() throws SQLException {
		testDatabase.getConnection().close();
	}
}
