package fr.insee.kraftwerk.core.utils;

import com.opencsv.*;

import fr.insee.kraftwerk.core.Constants;

import java.io.*;
import java.nio.charset.StandardCharsets;

/** Encapsulate org.opencsv features that we use in Kraftwerk. */
public class CsvUtils {

    public static CSVReader getReader(String filePath) throws IOException {
        CSVParser parser = new CSVParserBuilder()
                .withSeparator(Constants.CSV_OUTPUTS_SEPARATOR)
                //.withQuoteChar(Constants.CSV_OUTPUTS_QUOTE_CHAR)
                //.withEscapeChar(CSVWriter.DEFAULT_ESCAPE_CHARACTER)
                .build();
        return new CSVReaderBuilder(new FileReader(filePath, StandardCharsets.UTF_8))
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
                Constants.CSV_OUTPUTS_QUOTE_CHAR,
                CSVWriter.DEFAULT_ESCAPE_CHARACTER,
                CSVWriter.DEFAULT_LINE_END);
    }
}
