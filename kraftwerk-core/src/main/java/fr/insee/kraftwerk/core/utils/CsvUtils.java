package fr.insee.kraftwerk.core.utils;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.CSVWriter;
import com.opencsv.ICSVWriter;

import fr.insee.kraftwerk.core.Constants;

/** Encapsulate org.opencsv features that we use in Kraftwerk. */
public class CsvUtils {

    private CsvUtils() {
        //Utility class
    }

    public static CSVReader getReader(Path filePath) throws IOException {
        CSVParser parser = new CSVParserBuilder()
                .withSeparator(Constants.CSV_OUTPUTS_SEPARATOR)
                //.withQuoteChar(Constants.CSV_OUTPUTS_QUOTE_CHAR)
                //.withEscapeChar(CSVWriter.DEFAULT_ESCAPE_CHARACTER)
                .build();
        return new CSVReaderBuilder(new FileReader(filePath.toFile(), StandardCharsets.UTF_8))
                //.withSkipLines(1) // (uncomment to ignore header)
                .withCSVParser(parser)
                .build();
    }

    public static CSVReader getReaderWithSeparator(String filePath, char separator) throws IOException {
        CSVParser csvParser= new CSVParserBuilder()
                .withSeparator(separator)
                .build();
        return new CSVReaderBuilder(new FileReader(filePath, StandardCharsets.UTF_8))
                .withCSVParser(csvParser)
                .build();
    }

    public static CSVWriter getWriter(String filePath) throws IOException {
        return new CSVWriter(new FileWriter(filePath, StandardCharsets.UTF_8),
                Constants.CSV_OUTPUTS_SEPARATOR,
                Constants.getCsvOutputQuoteChar(),
                ICSVWriter.DEFAULT_ESCAPE_CHARACTER,
                ICSVWriter.DEFAULT_LINE_END);
    }
}