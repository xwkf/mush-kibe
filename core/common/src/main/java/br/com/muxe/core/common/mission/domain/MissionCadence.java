package br.com.muxe.core.common.mission.domain;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjusters;

public enum MissionCadence {
    DAILY,
    WEEKLY,
    CREDIT_PATH;

    public LocalDate start(Instant at, ZoneId zone) {
        LocalDate date = LocalDate.ofInstant(at, zone);
        return this == DAILY ? date : date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
    }

    public Instant end(Instant at, ZoneId zone) {
        LocalDate start = start(at, zone);
        LocalDate next = this == DAILY ? start.plusDays(1) : start.plusWeeks(1);
        return next.atStartOfDay(zone).toInstant();
    }
}
