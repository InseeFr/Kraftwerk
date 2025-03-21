package fr.insee.kraftwerk.core.utils;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileReader;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

class CsvUtilsTest {

    @Test
    void testGetReader() throws Exception {
        String csvData = "\"col1\";\"col2\";\"col3\"\n\"val1\";\"val2\";\"val3\"";
        InputStream inputStream = new ByteArrayInputStream(csvData.getBytes());

        try (CSVReader reader = CsvUtils.getReader(inputStream)) {
            List<String[]> rows = reader.readAll();
            assertEquals(2, rows.size(), "Le fichier CSV doit contenir 2 lignes");
            assertArrayEquals(new String[]{"col1", "col2", "col3"}, rows.get(0), "La première ligne doit être l'en-tête");
            assertArrayEquals(new String[]{"val1", "val2", "val3"}, rows.get(1), "La deuxième ligne doit contenir les valeurs");
        }
    }

    @Test
    void testGetReaderWithSeparator() throws Exception {
        String csvData = "col1|col2|col3\nval1|val2|val3";
        InputStream inputStream = new ByteArrayInputStream(csvData.getBytes());

        try (CSVReader reader = CsvUtils.getReaderWithSeparator(inputStream, '|')) {
            List<String[]> rows = reader.readAll();
            assertEquals(2, rows.size(), "Le fichier CSV doit contenir 2 lignes");
            assertArrayEquals(new String[]{"col1", "col2", "col3"}, rows.get(0), "La première ligne doit être l'en-tête");
            assertArrayEquals(new String[]{"val1", "val2", "val3"}, rows.get(1), "La deuxième ligne doit contenir les valeurs");
        }
    }

    @Test
    void testGetWriter(@TempDir Path tempDir) throws Exception {
        File tempFile = tempDir.resolve("test.csv").toFile();

        try (CSVWriter writer = CsvUtils.getWriter(tempFile.getAbsolutePath())) {
            writer.writeNext(new String[]{"col1", "col2", "col3"});
            writer.writeNext(new String[]{"val1", "val2", "val3"});
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(tempFile))) {
            String header = reader.readLine();
            String row = reader.readLine();
            assertEquals("\"col1\";\"col2\";\"col3\"", header, "L'en-tête du fichier CSV doit être correct");
            assertEquals("\"val1\";\"val2\";\"val3\"", row, "La première ligne de données doit être correcte");
        }
    }
}
