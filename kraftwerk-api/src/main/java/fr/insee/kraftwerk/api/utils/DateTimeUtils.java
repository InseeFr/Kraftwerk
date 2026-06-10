package fr.insee.kraftwerk.api.utils;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

public final class DateTimeUtils {

    private static final ZoneId FRANCE_ZONE = ZoneId.of("Europe/Paris");

    private DateTimeUtils() {
    }

    public static Instant resolveInstant(Instant utcDate, LocalDateTime localDate) {
        if (utcDate != null && localDate != null) {
            throw new IllegalArgumentException("Use either UTC date or local date, not both");
        }

        if (localDate != null) {
            return localDate.atZone(FRANCE_ZONE).toInstant();
        }

        return utcDate;
    }

    public static Instant resolveRequiredInstant(
            Instant utcDate,
            LocalDateTime localDate,
            String utcParamName,
            String localParamName
    ) {
        Instant resolvedDate = resolveInstant(utcDate, localDate);

        if (resolvedDate == null) {
            throw new IllegalArgumentException(
                    "Either " + utcParamName + " or " + localParamName + " must be provided"
            );
        }

        return resolvedDate;
    }
}