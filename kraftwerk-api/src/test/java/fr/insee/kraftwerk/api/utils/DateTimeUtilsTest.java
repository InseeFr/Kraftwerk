package fr.insee.kraftwerk.api.utils;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.*;

class DateTimeUtilsTest {

    @Test
    void shouldReturnUtcDateWhenUtcDateIsProvided() {
        Instant utcDate = Instant.parse("2026-06-10T12:00:00Z");

        Instant result = DateTimeUtils.resolveInstant(utcDate, null);

        assertEquals(utcDate, result);
    }

    @Test
    void shouldConvertLocalDateToInstantWhenLocalDateIsProvided() {
        LocalDateTime localDate = LocalDateTime.of(2026, 6, 10, 14, 0);

        Instant result = DateTimeUtils.resolveInstant(null, localDate);

        assertEquals(Instant.parse("2026-06-10T12:00:00Z"), result);
    }

    @Test
    void shouldReturnNullWhenNoDateIsProvided() {
        Instant result = DateTimeUtils.resolveInstant(null, null);

        assertNull(result);
    }

    @Test
    void shouldThrowExceptionWhenBothUtcAndLocalDatesAreProvided() {
        Instant utcDate = Instant.parse("2026-06-10T12:00:00Z");
        LocalDateTime localDate = LocalDateTime.of(2026, 6, 10, 14, 0);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> DateTimeUtils.resolveInstant(utcDate, localDate)
        );

        assertEquals("Use either UTC date or local date, not both", exception.getMessage());
    }

    @Test
    void shouldReturnResolvedDateWhenRequiredDateIsProvided() {
        Instant utcDate = Instant.parse("2026-06-10T12:00:00Z");

        Instant result = DateTimeUtils.resolveRequiredInstant(
                utcDate,
                null,
                "sinceDate",
                "localSinceDate"
        );

        assertEquals(utcDate, result);
    }

    @Test
    void shouldThrowExceptionWhenRequiredDateIsMissing() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> DateTimeUtils.resolveRequiredInstant(
                        null,
                        null,
                        "sinceDate",
                        "localSinceDate"
                )
        );

        assertEquals(
                "Either sinceDate or localSinceDate must be provided",
                exception.getMessage()
        );
    }

    @Test
    void shouldConvertInstantToFranceDateTime() {
        Instant instant = Instant.parse("2026-06-10T12:00:00Z");

        ZonedDateTime result = DateTimeUtils.toFranceDateTime(instant);

        assertEquals(
                ZonedDateTime.of(
                        2026,
                        6,
                        10,
                        14,
                        0,
                        0,
                        0,
                        ZoneId.of("Europe/Paris")
                ),
                result
        );
    }

    @Test
    void shouldReturnNullWhenConvertingNullInstantToFranceDateTime() {
        assertNull(DateTimeUtils.toFranceDateTime(null));
    }
}