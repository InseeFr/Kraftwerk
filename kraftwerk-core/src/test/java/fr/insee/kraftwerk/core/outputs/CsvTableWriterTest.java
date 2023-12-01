package fr.insee.kraftwerk.core.outputs;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;

import fr.insee.kraftwerk.core.TestConstants;
import fr.insee.kraftwerk.core.metadata.Group;
import fr.insee.kraftwerk.core.metadata.Variable;
import fr.insee.kraftwerk.core.metadata.VariableType;
import fr.insee.kraftwerk.core.metadata.VariablesMap;
import fr.insee.kraftwerk.core.outputs.csv.CsvTableWriter;
import fr.insee.kraftwerk.core.utils.CsvUtils;
import fr.insee.kraftwerk.core.utils.FileUtils;
import fr.insee.vtl.model.Dataset;
import fr.insee.vtl.model.Dataset.Role;
import fr.insee.vtl.model.InMemoryDataset;
import fr.insee.vtl.model.Structured;

class CsvTableWriterTest {

	Path outTestFilePath = Paths.get(TestConstants.UNIT_TESTS_DUMP, "test.csv");

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

	Dataset testCompleteVariablesDataset = new InMemoryDataset(
			List.of(
					List.of("T01", "foostring1", 1, 123456789, true, new Date(1)),
					List.of("T01", "foostring2", 2, 123456790, false, new Date(2)),
					List.of("T02", "foostring3", 3, 123456791, true, new Date(3))
			),
			List.of(
					new Structured.Component("ID", String.class, Role.IDENTIFIER),
					new Structured.Component("FOO_STR", String.class, Role.MEASURE),
					new Structured.Component("FOO_INT", Integer.class, Role.MEASURE),
					new Structured.Component("FOO_NUM", Double.class, Role.MEASURE),
					new Structured.Component("FOO_BOO", Boolean.class, Role.MEASURE),
					new Structured.Component("FOO_DAT", Date.class, Role.MEASURE)
			)
	);

	@Test
	void writeCsvFromDatasetTest() throws IOException, CsvException {
		// Clean the existing file
		Files.deleteIfExists(outTestFilePath);
		FileUtils.createDirectoryIfNotExist(outTestFilePath.getParent());

		Map<String, VariablesMap> metaVariables = new HashMap<>();
		VariablesMap varMap = new VariablesMap();
		Group group = new Group("test","RACINE");
		varMap.putVariable(new Variable("ID",group, VariableType.STRING));
		varMap.putVariable(new Variable("ID2",group, VariableType.STRING));
		varMap.putVariable(new Variable("FOO_STR",group, VariableType.STRING));
		varMap.putVariable(new Variable("FOO_NUM",group, VariableType.NUMBER));
		metaVariables.put("test",varMap);

		CsvTableWriter.writeCsvTable(testDataset, outTestFilePath, metaVariables, "test");
		//
		CSVReader reader = CsvUtils.getReader(outTestFilePath);
		List<String[]> rows = reader.readAll();
		//
		assertEquals(4, rows.size());
		String[] header = rows.get(0);
		for (String columnName : List.of("ID", "ID2", "FOO_STR", "FOO_NUM")) {
			assertTrue(arrayContains(header, columnName));
		}
		String[] row1 = rows.get(1);
		for (String columnName : List.of("T01", "01", "foo11", "11")) {
			assertTrue(arrayContains(row1, columnName));
		}

	}

	@Test
	void getDataPointValueTest() {
		/*
		 * 
					List.of("T01", "foostring1", 1, 11L, true, new Date(100000)),
					List.of("T01", "foostring2", 2, 12L, false, new Date(200000)),
					List.of("T02", "foostring3", 3, 21L, true, new Date(300000))
		 */
		// String variable
		assertEquals("foostring1", CsvTableWriter.getDataPointValue(testCompleteVariablesDataset.getDataPoints().get(0),
				testCompleteVariablesDataset.getDataStructure().get("FOO_STR")));
		// Integer variable
		assertEquals("1",CsvTableWriter.getDataPointValue(testCompleteVariablesDataset.getDataPoints().get(0),
				testCompleteVariablesDataset.getDataStructure().get("FOO_INT")));
		// Numeric variable
		assertEquals("123456789", CsvTableWriter.getDataPointValue(testCompleteVariablesDataset.getDataPoints().get(0),
				testCompleteVariablesDataset.getDataStructure().get("FOO_NUM")));
		// Boolean variable
		assertEquals("1", CsvTableWriter.getDataPointValue(testCompleteVariablesDataset.getDataPoints().get(0),
				testCompleteVariablesDataset.getDataStructure().get("FOO_BOO")));
		// Date variable
		assertEquals("1970-01-01", CsvTableWriter.getDataPointValue(testCompleteVariablesDataset.getDataPoints().get(1),
				testCompleteVariablesDataset.getDataStructure().get("FOO_DAT")));
		
	}

	private boolean arrayContains(String[] array, String s) {
		boolean res = false;
		int i = 0;
		while (i<array.length && ! res) {
			if (array[i].equals(s)) {
				res = true;
			} else {
				i++;
			}
		}
		return res;
	}

}
