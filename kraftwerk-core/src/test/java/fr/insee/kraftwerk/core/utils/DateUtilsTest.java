package fr.insee.kraftwerk.core.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class DateUtilsTest {

	@Test
	void convertDateTest() {
		assertEquals(1645007098, DateUtils.convertToTimestamp("16/02/2022 11:24:58"));
		assertEquals(1566544132, DateUtils.convertToTimestamp("23/08/2019 09:08:52"));
		assertEquals(1111111111, DateUtils.convertToTimestamp("18/03/2005 02:58:31"));
		assertEquals(1, DateUtils.convertToTimestamp("01/01/1970 01:00:01"));
		
	}

}
