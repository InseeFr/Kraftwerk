package fr.insee.kraftwerk.core.extradata.reportingdata;

import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.exceptions.CsvValidationException;
import fr.insee.kraftwerk.core.Constants;
import fr.insee.kraftwerk.core.exceptions.NullException;
import fr.insee.kraftwerk.core.rawdata.SurveyRawData;
import fr.insee.kraftwerk.core.utils.files.FileUtilsInterface;
import lombok.extern.log4j.Log4j2;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

@Log4j2
public class CSVReportingDataParser extends ReportingDataParser {

	private CSVReader csvReader;

	public CSVReportingDataParser(FileUtilsInterface fileUtilsInterface) {
		super(fileUtilsInterface);
	}

	public void parseReportingData(ReportingData reportingData, SurveyRawData data, boolean withAllReportingData) throws NullException {
		Path filePath = reportingData.getFilepath();
		try(InputStream inputStream = fileUtilsInterface.readFile(filePath.toString())){
			readFile(inputStream);
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
				integrateReportingDataIntoUE(data, reportingData, withAllReportingData, fileUtilsInterface);
			} else {
				log.error("Following CSV file is malformed : {}", filePath);
			}
		} catch (CsvValidationException e) {
			log.error("Following CSV file is malformed : {}, CsvValidationException {} ", filePath, e.getMessage());
		} catch (IOException e) {
			log.error("Could not connect to data file {} because IOException {}", filePath, e.getMessage());
		}
	}

	public long convertToTimestamp(String dateString) {
		DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern(Constants.REPORTING_DATA_INPUT_DATE_FORMAT);
		LocalDateTime parsedDate = null;
		try {
			parsedDate = LocalDateTime.parse(dateString, dateFormat);
		} catch (DateTimeParseException e) {
			log.error("Parsing error : {}", e.getMessage());
		}
		if (parsedDate == null) {
			log.error("Parsing error : the parsed date is null");
			return 0L;
		}
		return parsedDate.atZone(ZoneId.of("CET")).toInstant().toEpochMilli();
	}

	/**
	 *
	 * @param header header to check
	 * @return true if header correct, false otherwise
	 */
	public boolean controlHeader(String[] header) {
		return (header.length == 8 && header[0].contentEquals("statut") && header[1].contentEquals("dateInfo")
				&& header[2].contentEquals("idUe") && header[3].contentEquals("idContact")
				&& header[4].contentEquals("nom") && header[5].contentEquals("prenom")
				&& header[6].contentEquals("adresse") && header[7].contentEquals("numeroDeLot"));
	}

	private void readFile(InputStream inputStream) {
		InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
		this.csvReader = new CSVReader(inputStreamReader);
		CSVParser parser = (new CSVParserBuilder()).withSeparator(',').build();
		this.csvReader = (new CSVReaderBuilder(inputStreamReader)).withCSVParser(parser).build();
	}
}