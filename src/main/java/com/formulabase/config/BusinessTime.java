package com.formulabase.config;

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;

/** Calendar dates used for production are always based on Thailand time. */
public final class BusinessTime {
    public static final ZoneId ZONE = ZoneId.of("Asia/Bangkok");

    private BusinessTime() { }

    public static java.time.OffsetDateTime inBangkok(java.time.OffsetDateTime value) {
        return value == null ? null : value.atZoneSameInstant(ZONE).toOffsetDateTime();
    }

    public static LocalDate today() {
        return today(Clock.systemUTC());
    }

    public static LocalDate today(Clock clock) {
        return LocalDate.now(clock.withZone(ZONE));
    }
}
