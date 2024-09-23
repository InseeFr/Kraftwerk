package fr.insee.kraftwerk.core.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import java.text.SimpleDateFormat;

class DateUtilsTest {

	@Test
	void convertDateTest() {
		assertEquals(1645007098000L, DateUtils.convertToTimestamp("16/02/2022 11:24:58", new SimpleDateFormat("dd/MM" +
				"/yyyy HH:mm:ss")));
		assertEquals(1566544132000L, DateUtils.convertToTimestamp("23/08/2019 09:08:52", new SimpleDateFormat("dd/MM" +
				"/yyyy HH:mm:ss")));
		assertEquals(1111111111000L, DateUtils.convertToTimestamp("18/03/2005 02:58:31", new SimpleDateFormat("dd/MM/yyyy HH:mm:ss")));
		assertEquals(1000L, DateUtils.convertToTimestamp("01/01/1970 01:00:01", new SimpleDateFormat("dd/MM/yyyy HH:mm:ss")));
		
	}

}

