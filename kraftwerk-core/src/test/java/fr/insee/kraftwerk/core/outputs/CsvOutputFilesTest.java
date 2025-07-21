package fr.insee.kraftwerk.core.outputs;

import fr.insee.bpm.metadata.model.Group;
import fr.insee.bpm.metadata.model.MetadataModel;
import fr.insee.bpm.metadata.model.Variable;
import fr.insee.bpm.metadata.model.VariableType;
import fr.insee.kraftwerk.core.Constants;
import fr.insee.kraftwerk.core.TestConstants;
import fr.insee.kraftwerk.core.encryption.EncryptionUtilsStub;
import fr.insee.kraftwerk.core.exceptions.KraftwerkException;
import fr.insee.kraftwerk.core.inputs.UserInputsFile;
import fr.insee.kraftwerk.core.outputs.csv.CsvOutputFiles;
import fr.insee.kraftwerk.core.utils.SqlUtils;
import fr.insee.kraftwerk.core.utils.files.FileSystemImpl;
import fr.insee.kraftwerk.core.utils.files.FileUtilsInterface;
import fr.insee.kraftwerk.core.vtl.VtlBindings;
import fr.insee.vtl.model.Dataset;
import fr.insee.vtl.model.InMemoryDataset;
import fr.insee.vtl.model.Structured;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;


@TestMethodOrder(OrderAnnotation.class)
class CsvOutputFilesTest {

	private static UserInputsFile testUserInputsFile;
	private static OutputFiles outputFiles;
	private static Connection database;

	private static final FileUtilsInterface fileUtilsInterface = new FileSystemImpl(TestConstants.TEST_RESOURCES_DIRECTORY);

	Dataset fooDataset = new InMemoryDataset(List.of(),
			List.of(new Structured.Component("FOO", String.class, Dataset.Role.IDENTIFIER)));

	@Test
	@Order(1)
	void createInstance() {
		assertDoesNotThrow(() -> {
			//
			testUserInputsFile = new UserInputsFile(
					Path.of(TestConstants.UNIT_TESTS_DIRECTORY, "user_inputs/inputs_valid_several_modes.json"),
					Path.of(TestConstants.UNIT_TESTS_DIRECTORY,"user_inputs"), fileUtilsInterface);
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
			database = SqlUtils.openConnection();
			SqlUtils.convertVtlBindingsIntoSqlDatabase(vtlBindings, database.createStatement());
			outputFiles = new CsvOutputFiles(Paths.get(TestConstants.UNIT_TESTS_DUMP),
					vtlBindings,
					testUserInputsFile.getModes(),
					database.createStatement(),
					fileUtilsInterface,
					TestConstants.getKraftwerkExecutionContext(),
					new EncryptionUtilsStub());
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

	/**
	 * @author Adrien Marchal
	 */
	@Test
	@Order(3)
	void testWriteCsvV2() throws KraftwerkException, IOException {
		Files.createDirectories(outputFiles.getOutputFolder());

		Map<String, MetadataModel> metaModels = new HashMap<>();
		MetadataModel metMod = new MetadataModel();
		Group group = new Group("test","RACINE");
		metMod.getVariables().putVariable(new Variable("ID",group, VariableType.STRING));
		metMod.getVariables().putVariable(new Variable("ID2",group, VariableType.STRING));
		metMod.getVariables().putVariable(new Variable("FOO_STR",group, VariableType.STRING));
		metMod.getVariables().putVariable(new Variable("FOO_NUM",group, VariableType.NUMBER));
		metaModels.put("test",metMod);

		outputFiles.writeOutputTablesV2();

		Path racinePath = Path.of(outputFiles.getOutputFolder().toString(),
				outputFiles.outputFileName("RACINE",
						outputFiles.getKraftwerkExecutionContext())
		);
		racinePath = racinePath.resolveSibling(racinePath.getFileName());
		File f = racinePath.toFile();
		assertTrue(f.exists());
		Assertions.assertNotEquals(0, f.length());
	}

	@AfterAll
    static void closeConnection() throws SQLException {
		database.close();
	}
}
