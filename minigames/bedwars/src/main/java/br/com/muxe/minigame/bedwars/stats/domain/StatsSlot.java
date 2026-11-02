package br.com.muxe.minigame.bedwars.stats.domain;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public record StatsSlot(String mode, StatsPeriod period, LocalDate start) {

    public StatsSlot {
        Objects.requireNonNull(mode, "mode");
        Objects.requireNonNull(period, "period");
        Objects.requireNonNull(start, "start");
    }

    public static List<StatsSlot> touchedBy(MatchOutcome outcome, Instant at, ZoneId zone) {
        List<StatsSlot> slots = new ArrayList<>();
        for (String mode : List.of(outcome.mode(), PlayerStats.GENERAL)) {
            for (StatsPeriod period : StatsPeriod.values()) {
                slots.add(new StatsSlot(mode, period, period.start(at, zone)));
            }
        }
        return List.copyOf(slots);
    }
}
