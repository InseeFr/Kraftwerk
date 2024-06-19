package fr.insee.kraftwerk.core.outputs;

import fr.insee.kraftwerk.core.Constants;
import fr.insee.kraftwerk.core.TestConstants;
import fr.insee.kraftwerk.core.inputs.UserInputsFile;
import fr.insee.kraftwerk.core.metadata.Group;
import fr.insee.kraftwerk.core.metadata.MetadataModel;
import fr.insee.kraftwerk.core.metadata.Variable;
import fr.insee.kraftwerk.core.metadata.VariableType;
import fr.insee.kraftwerk.core.outputs.parquet.ParquetOutputFiles;
import fr.insee.kraftwerk.core.utils.FileSystemImpl;
import fr.insee.kraftwerk.core.utils.FileUtilsInterface;
import fr.insee.kraftwerk.core.vtl.VtlBindings;
import fr.insee.vtl.model.Dataset;
import fr.insee.vtl.model.Dataset.Role;
import fr.insee.vtl.model.InMemoryDataset;
import fr.insee.vtl.model.Structured;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;

@TestMethodOrder(OrderAnnotation.class)
class ParquetOutputFilesTest {

	private static UserInputsFile testUserInputs;
	private static ParquetOutputFiles outputFiles;
	private static final FileUtilsInterface fileUtilsInterface = new FileSystemImpl();


	Dataset testDataset = new InMemoryDataset(
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

	@Test
	@Order(1)
	void createInstance() {
		assertDoesNotThrow(() -> {
			//
			testUserInputs = new UserInputsFile(
					Path.of(TestConstants.UNIT_TESTS_DIRECTORY, "user_inputs/inputs_valid_several_modes.json"),
					Path.of(TestConstants.UNIT_TESTS_DIRECTORY,"user_inputs"), fileUtilsInterface);
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
			outputFiles = new ParquetOutputFiles(Paths.get(TestConstants.UNIT_TESTS_DUMP), vtlBindings, testUserInputs.getModes());
		});
	}

	@Test
	@Order(2)
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
	@Order(3)
	void writeParquetFromDatasetTest(){

		// Clean the existing file
//		Files.deleteIfExists(outputFiles.getOutputFolder());
		FileSystemImpl.createDirectoryIfNotExist(outputFiles.getOutputFolder());

		Map<String, MetadataModel> metaModels = new HashMap<>();
		MetadataModel metMod = new MetadataModel();
		Group group = new Group("test","RACINE");
		metMod.getVariables().putVariable(new Variable("ID",group, VariableType.STRING));
		metMod.getVariables().putVariable(new Variable("ID2",group, VariableType.STRING));
		metMod.getVariables().putVariable(new Variable("FOO_STR",group, VariableType.STRING));
		metMod.getVariables().putVariable(new Variable("FOO_NUM",group, VariableType.NUMBER));
		metaModels.put("test",metMod);

		assertDoesNotThrow(() -> {outputFiles.writeOutputTables(metaModels);});
		Path racinePath = Path.of(outputFiles.getOutputFolder().toString(), outputFiles.getAllOutputFileNames("RACINE").getFirst());
		racinePath = racinePath.resolveSibling(racinePath.getFileName()+".parquet");
		File f = racinePath.toFile();
		Assertions.assertTrue(f.exists());
		Assertions.assertNotEquals(0, f.length());

	}
}
