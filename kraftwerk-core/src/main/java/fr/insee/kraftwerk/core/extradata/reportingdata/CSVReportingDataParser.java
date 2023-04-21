package fr.insee.kraftwerk.core.extradata.reportingdata;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.exceptions.CsvValidationException;

import fr.insee.kraftwerk.core.exceptions.NullException;
import fr.insee.kraftwerk.core.rawdata.SurveyRawData;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class CSVReportingDataParser extends ReportingDataParser {

	private CSVReader csvReader;

	public void parseReportingData(ReportingData reportingData, SurveyRawData data) throws NullException {
		Path filePath = reportingData.getFilepath();
	    try{
	    	readFile(filePath);
	    } catch (NullPointerException e) {
	    	throw new NullException();
	    }
	    
		try {
			String[] header = this.csvReader.readNext();
			if (controlHeader(header)) {
				String[] nextRecord;
				while ((nextRecord = this.csvReader.readNext()) != null) {
					String rowIdentifier = nextRecord[2];
					String rowState = nextRecord[0];
					String rowTimestamp = nextRecord[1];
					State state = new State(rowState, convertToTimestamp(rowTimestamp));
					if (reportingData.containsReportingDataUE(rowIdentifier)) {
						ReportingDataUE reportingDataUE1 = reportingData.getListReportingDataUE().stream().filter(
								reportingDataUEToSearch -> rowIdentifier.equals(reportingDataUEToSearch.getIdentifier()))
								.findAny().orElse(null);
						if (reportingDataUE1 != null) {
							reportingDataUE1.addState(state);
							reportingDataUE1.sortStates();
						}
						continue;
					}
					ReportingDataUE reportingDataUE = new ReportingDataUE(rowIdentifier);
					reportingDataUE.addState(state);
					reportingData.addReportingDataUE(reportingDataUE);
				}
				integrateReportingDataIntoUE(data, reportingData);
			} else {
				log.error("Following CSV file is malformed : {}", filePath);
			}
		} catch (CsvValidationException e) {
			log.error("Following CSV file is malformed : {}, CsvValidationException {} ", filePath, e.getMessage());
		} catch (IOException e) {
			log.error("Could not connect to data file {} because IOException {}", filePath, e.getMessage());
		}
	}

	public long convertToTimestamp(String rowTimestamp) {
		SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
		dateFormat.setTimeZone(TimeZone.getTimeZone("CET"));
		Date parsedDate = null;
		try {
			parsedDate = dateFormat.parse(rowTimestamp);
		} catch (ParseException e1) {
			log.error("Parsing error : {}", e1.getMessage());
		}
		if (parsedDate == null) {
			log.error("Parsing error : the parsed date is null");
			return 0L;
		}
		return TimeUnit.MILLISECONDS.toSeconds(parsedDate.getTime());
	}

	public boolean controlHeader(String[] header) {
		return (header.length == 8 && header[0].contentEquals("statut") && header[1].contentEquals("dateInfo")
				&& header[2].contentEquals("idUe") && header[3].contentEquals("idContact")
				&& header[4].contentEquals("nom") && header[5].contentEquals("prenom")
				&& header[6].contentEquals("adresse") && header[7].contentEquals("numeroDeLot"));
	}

	private void readFile(Path filePath) {
		try {
			FileReader filereader = new FileReader(filePath.toString());
			this.csvReader = new CSVReader(filereader);
			CSVParser parser = (new CSVParserBuilder()).withSeparator(',').build();
			this.csvReader = (new CSVReaderBuilder(filereader)).withCSVParser(parser).build();
		} catch (FileNotFoundException e) {
			log.error("Unable to find the file {}, FileNotFoundException {}", filePath, e);
		}
	}
}
