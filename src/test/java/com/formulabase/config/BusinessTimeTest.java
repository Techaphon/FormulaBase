package com.formulabase.config;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

class BusinessTimeTest {
    @Test void dateChangesAtMidnightInThailandRatherThanUtc() {
        assertEquals(LocalDate.of(2026, 11, 8), BusinessTime.today(Clock.fixed(Instant.parse("2026-11-08T16:59:59Z"), ZoneOffset.UTC)));
        assertEquals(LocalDate.of(2026, 11, 9), BusinessTime.today(Clock.fixed(Instant.parse("2026-11-08T17:00:00Z"), ZoneOffset.UTC)));
    }
}
