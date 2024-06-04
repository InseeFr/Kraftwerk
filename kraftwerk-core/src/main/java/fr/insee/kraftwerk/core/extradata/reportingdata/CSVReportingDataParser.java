package fr.insee.kraftwerk.core.extradata.reportingdata;

import java.nio.file.Path;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import fr.insee.kraftwerk.core.exceptions.KraftwerkException;
import fr.insee.kraftwerk.core.rawdata.SurveyRawData;
import fr.insee.kraftwerk.core.utils.SqlUtils;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class CSVReportingDataParser extends ReportingDataParser {

	public void parseReportingData(ReportingData reportingData, SurveyRawData data, boolean withAllReportingData, Statement database) throws KraftwerkException {
		try{
			Path filePath = reportingData.getFilepath();
			if(database == null){
				log.error("Failed connection to duckdb");
				return;
			}
			SqlUtils.readCsvFile(database,filePath);

			if (controlHeader(SqlUtils.getColumnNames(database,filePath.getFileName().toString().split("\\.")[0]))){
				//Connect to DuckDB and retrieve reportingData table
				ResultSet resultSet = SqlUtils.getAllData(database, filePath.getFileName().toString().split("\\.")[0]);


				while (resultSet.next()) {
					String rowIdentifier = resultSet.getString("idUe");
					String rowState = resultSet.getString("statut");
					String rowTimestamp = resultSet.getString("dateInfo");
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
				integrateReportingDataIntoUE(data, reportingData, withAllReportingData);
			} else {
				log.error("Following CSV file is malformed : {}", filePath);
			}
		} catch (SQLException e) {
			log.error(e.toString());
			throw new KraftwerkException(500,"Internal server error on CSV reporting data parser");
		}
	}

	public long convertToTimestamp(String rowTimestamp) {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S");
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

	/**
	 *
	 * @param header header to check
	 * @return true if header correct, false otherwise
	 */
	public boolean controlHeader(List<String> header) {
		if(header != null) {
			return (header.size() == 8 && header.contains("statut") && header.contains("dateInfo")
					&& header.contains("idUe") && header.contains("idContact")
					&& header.contains("nom") && header.contains("prenom")
					&& header.contains("adresse") && header.contains("numeroDeLot"));
		}
		return false;
	}
}
