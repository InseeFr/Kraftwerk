package fr.insee.kraftwerk.core.outputs;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;
import fr.insee.kraftwerk.core.TestConstants;
import fr.insee.kraftwerk.core.utils.CsvUtils;
import fr.insee.vtl.model.Dataset;
import fr.insee.vtl.model.Dataset.Role;
import fr.insee.vtl.model.InMemoryDataset;
import fr.insee.vtl.model.Structured;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class CsvTableWriterTest {

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
	public void writeCsvFromDataset() throws IOException, CsvException {
		// Clean the existing file
		Path outTestFilePath = Paths.get(TestConstants.UNIT_TESTS_DUMP, "test.csv");
		Files.deleteIfExists(outTestFilePath);
		
		CsvTableWriter.writeCsvTable(testDataset, outTestFilePath);
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
