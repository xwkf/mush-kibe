package br.com.muxe.core.common.mission.domain;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.UUID;

public record MissionCatalog(List<Mission> daily, List<Mission> weekly, List<Mission> creditPath, int dailyCount, int weeklyCount) {

    private static final long DAILY_SALT = 31;

    public MissionCatalog {
        daily = List.copyOf(daily);
        weekly = List.copyOf(weekly);
        creditPath = List.copyOf(creditPath);
    }

    public static MissionCatalog empty() {
        return new MissionCatalog(List.of(), List.of(), List.of(), 0, 0);
    }

    public List<Mission> active(UUID playerId, Instant at, ZoneId zone) {
        List<Mission> active = new ArrayList<>();
        LocalDate day = MissionCadence.DAILY.start(at, zone);
        LocalDate week = MissionCadence.WEEKLY.start(at, zone);
        active.addAll(draw(daily, dailyCount, playerId.getMostSignificantBits() ^ playerId.getLeastSignificantBits() ^ day.toEpochDay() * DAILY_SALT));
        active.addAll(draw(weekly, weeklyCount, week.toEpochDay()));
        active.addAll(creditPath);
        return List.copyOf(active);
    }

    static List<Mission> draw(List<Mission> pool, int count, long seed) {
        List<Mission> shuffled = new ArrayList<>(pool);
        Collections.shuffle(shuffled, new Random(seed));
        List<Mission> chosen = new ArrayList<>(shuffled.subList(0, Math.min(count, shuffled.size())));
        chosen.sort((first, second) -> Integer.compare(pool.indexOf(first), pool.indexOf(second)));
        return chosen;
    }
}
