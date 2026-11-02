package br.com.muxe.minigame.bedwars.stats.domain;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjusters;
import java.util.Arrays;
import java.util.Optional;

public enum StatsPeriod {
    DAILY("diario"),
    WEEKLY("semanal"),
    MONTHLY("mensal"),
    TOTAL("total");

    public static final LocalDate TOTAL_START = LocalDate.of(1970, 1, 1);

    private final String id;

    StatsPeriod(String id) {
        this.id = id;
    }

    public String id() {
        return id;
    }

    public LocalDate start(Instant at, ZoneId zone) {
        LocalDate date = LocalDate.ofInstant(at, zone);
        return switch (this) {
            case DAILY -> date;
            case WEEKLY -> date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
            case MONTHLY -> date.withDayOfMonth(1);
            case TOTAL -> TOTAL_START;
        };
    }

    public static Optional<StatsPeriod> fromId(String id) {
        return Arrays.stream(values()).filter(period -> period.id.equals(id)).findFirst();
    }
}
