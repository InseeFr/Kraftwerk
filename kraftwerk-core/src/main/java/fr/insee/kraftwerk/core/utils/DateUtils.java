package fr.insee.kraftwerk.core.utils;

import fr.insee.kraftwerk.core.Constants;
import lombok.extern.log4j.Log4j2;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

@Log4j2
public class DateUtils {

	private DateUtils() {
		//Only static methods
	}

	public static String getCurrentTimeStamp() {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
		Timestamp timestamp = new Timestamp(System.currentTimeMillis());
		return sdf.format(timestamp);
	}
	
	public static long convertToTimestamp(String rowTimestamp, SimpleDateFormat dateFormat) {
		dateFormat.setTimeZone(TimeZone.getTimeZone("CET"));
		Date parsedDate;
		try {
			parsedDate = dateFormat.parse(rowTimestamp);
		} catch (ParseException e1) {
			log.error("Parsing error : {}", e1.getMessage());
			return 0L;
		}
		return parsedDate.getTime();
	}
}
