package fr.insee.kraftwerk.core.extradata.reportingdata;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.exceptions.CsvValidationException;

import fr.insee.kraftwerk.core.Constants;
import fr.insee.kraftwerk.core.rawdata.SurveyRawData;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CSVReportingDataParser extends ReportingDataParser {

	/** Reader */
	private CSVReader csvReader;

	public void parseReportingData(ReportingData reportingData, SurveyRawData data) {
		String filePath = reportingData.getFilepath();

		readFile(filePath);

		try {
			String[] header = csvReader.readNext();
			/*
			 * We make sure the csv header is in a good format
			 */
			if (controlHeader(header)) {

				/*
				 * Then we read each data line and carefully put values at the right place.
				 */

				// Survey answers
				String[] nextRecord;
				while ((nextRecord = csvReader.readNext()) != null) {

					// Get values from the row
					String rowIdentifier = nextRecord[2];
					String rowState = nextRecord[0];
					String rowTimestamp = nextRecord[1];
					State state = new State(rowState, convertToTimestamp(rowTimestamp));

					// Find if Reporting data already exists, then add the state
					if (reportingData.containsReportingDataUE(rowIdentifier)) {
						ReportingDataUE reportingDataUE = reportingData.getListReportingDataUE().stream()
								.filter(reportingDataUEToSearch -> rowIdentifier.equals(reportingDataUEToSearch.getIdentifier()))
								.findAny().orElse(null);
						reportingDataUE.addState(state);
						reportingDataUE.sortStates();
					} else {
						ReportingDataUE reportingDataUE = new ReportingDataUE(rowIdentifier);
						reportingDataUE.addState(state);
						reportingData.addReportingDataUE(reportingDataUE);
					}
				}
				integrateReportingDataIntoUE(data, reportingData);
			} else {
				log.error(String.format("Following CSV file is malformed : %s", filePath), "");
			}
		} catch (CsvValidationException e) {
			log.error(String.format("Following CSV file is malformed : %s", filePath), e);
		} catch (IOException e) {
			log.error(String.format("Could not connect to data file %s", filePath), e);
		}

	}

	public long convertToTimestamp(String rowTimestamp) {
		SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
	    Date parsedDate = null;
		try {
			parsedDate = dateFormat.parse(rowTimestamp);
		} catch (ParseException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		return TimeUnit.MILLISECONDS.toSeconds(parsedDate.getTime());
	}

	public boolean controlHeader(String[] header) {
		// In a standard reporting data, the list of columns should include in that
		// order : "statut",
		// "dateInfo", "idUe", "idContact", "nom", "prenom", "adresse, "numeroDeLot"
		return header.length == 8 && header[0].contentEquals("statut") && header[1].contentEquals("dateInfo")
				&& header[2].contentEquals("idUe") && header[3].contentEquals("idContact")
				&& header[4].contentEquals("nom") && header[5].contentEquals("prenom")
				&& header[6].contentEquals("adresse") && header[7].contentEquals("numeroDeLot");
	}

	/**
	 * Instantiate a CSVReader
	 *
	 * @param filePath Path to the CSV file.
	 */
	private void readFile(String filePath) {
		try {
			// Create an object of file reader
			// class with CSV file as a parameter.
			FileReader filereader = new FileReader(filePath);
			// create csvReader object passing
			// file reader as a parameter
			csvReader = new CSVReader(filereader);

			// create csvParser object with
			// custom separator semi-colon
			CSVParser parser = new CSVParserBuilder().withSeparator(Constants.CSV_REPORTING_DATA_SEPARATOR).build();

			// create csvReader object with parameter
			// file reader and parser
			csvReader = new CSVReaderBuilder(filereader)
					// .withSkipLines(1) // (uncomment to ignore header)
					.withCSVParser(parser).build();

		} catch (FileNotFoundException e) {
			log.error(String.format("Unable to find the file %s", filePath), e);
		}
	}

}
