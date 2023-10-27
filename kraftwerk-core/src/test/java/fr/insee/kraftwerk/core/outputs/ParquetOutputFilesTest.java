package fr.insee.kraftwerk.core.outputs;

import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import com.opencsv.exceptions.CsvException;

import fr.insee.kraftwerk.core.Constants;
import fr.insee.kraftwerk.core.TestConstants;
import fr.insee.kraftwerk.core.inputs.UserInputs;
import fr.insee.kraftwerk.core.metadata.Group;
import fr.insee.kraftwerk.core.metadata.Variable;
import fr.insee.kraftwerk.core.metadata.VariableType;
import fr.insee.kraftwerk.core.metadata.VariablesMap;
import fr.insee.kraftwerk.core.outputs.parquet.ParquetOutputFiles;
import fr.insee.kraftwerk.core.utils.FileUtils;
import fr.insee.kraftwerk.core.vtl.VtlBindings;
import fr.insee.vtl.model.Dataset;
import fr.insee.vtl.model.Dataset.Role;
import fr.insee.vtl.model.InMemoryDataset;
import fr.insee.vtl.model.Structured;

@TestMethodOrder(OrderAnnotation.class)
class ParquetOutputFilesTest {

	private static UserInputs testUserInputs;
	private static OutputFiles outputFiles;


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
			testUserInputs = new UserInputs(
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
			outputFiles = new ParquetOutputFiles(Paths.get(TestConstants.UNIT_TESTS_DUMP), vtlBindings, testUserInputs.getModes(), testUserInputs.getMultimodeDatasetName());
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
		assertTrue(outputDatasetNames.containsAll(Set.of(Constants.ROOT_GROUP_NAME, "LOOP", "FROM_USER")));
	}

	
	@Test
	@Order(3)
	void writeParquetFromDatasetTest() throws IOException, CsvException {

		// Clean the existing file
//		Files.deleteIfExists(outputFiles.getOutputFolder());
		FileUtils.createDirectoryIfNotExist(outputFiles.getOutputFolder());

		Map<String, VariablesMap> metaVariables = new HashMap<>();
		VariablesMap varMap = new VariablesMap();
		Group group = new Group("test","RACINE");
		varMap.putVariable(new Variable("ID",group, VariableType.STRING));
		varMap.putVariable(new Variable("ID2",group, VariableType.STRING));
		varMap.putVariable(new Variable("FOO_STR",group, VariableType.STRING));
		varMap.putVariable(new Variable("FOO_NUM",group, VariableType.NUMBER));
		metaVariables.put("test",varMap);

		assertDoesNotThrow(() -> {outputFiles.writeOutputTables(metaVariables);});
		Path racinePath = Path.of(outputFiles.getOutputFolder().toString(), outputFiles.outputFileName("RACINE"));
		File f = racinePath.toFile();
		assertTrue(f.exists());
		assertNotEquals(0, f.length());
		
//		//
//		CSVReader reader = CsvUtils.getReader(outTestFilePath);
//		List<String[]> rows = reader.readAll();
//		//
//		assertEquals(4, rows.size());
//		String[] header = rows.get(0);
//		for (String columnName : List.of("ID", "ID2", "FOO_STR", "FOO_NUM")) {
//			assertTrue(arrayContains(header, columnName));
//		}
//		String[] row1 = rows.get(1);
//		for (String columnName : List.of("T01", "01", "foo11", "11")) {
//			assertTrue(arrayContains(row1, columnName));
//		}

	}
	
	
	
	
	

	
	boolean deleteDirectory(File directoryToBeDeleted) {
	    File[] allContents = directoryToBeDeleted.listFiles();
	    if (allContents != null) {
	        for (File file : allContents) {
	            deleteDirectory(file);
	        }
	    }
	    return directoryToBeDeleted.delete();
	}
}
