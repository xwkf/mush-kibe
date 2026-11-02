package br.com.muxe.core.common.mission.domain;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public record MissionBoard(List<Entry> entries) {

    public MissionBoard {
        entries = List.copyOf(entries);
    }

    public static Map<String, LocalDate> starts(List<Mission> active, Instant at, ZoneId zone) {
        Map<String, LocalDate> starts = new LinkedHashMap<>();
        active.forEach(mission -> starts.put(mission.id(), mission.cadence().start(at, zone)));
        return starts;
    }

    public static MissionBoard of(List<Mission> active, Map<String, MissionProgress> saved, Map<String, LocalDate> starts) {
        List<Entry> entries = new ArrayList<>();
        for (Mission mission : active) {
            MissionProgress progress = saved.get(mission.id());
            entries.add(new Entry(mission, progress != null ? progress : MissionProgress.fresh(mission.id(), starts.get(mission.id()))));
        }
        return new MissionBoard(entries);
    }

    public Update apply(Map<String, Long> amounts) {
        List<Entry> updated = new ArrayList<>();
        List<MissionProgress> changed = new ArrayList<>();
        List<Mission> completed = new ArrayList<>();
        for (Entry entry : entries) {
            if (entry.mission().goal().equals(MissionGoal.WEEKLY_MISSIONS)) {
                updated.add(entry);
                continue;
            }
            updated.add(advance(entry, amounts.getOrDefault(entry.mission().goal().id(), 0L), changed, completed));
        }
        long weeklyDone = updated.stream().filter(entry -> entry.mission().cadence() == MissionCadence.WEEKLY && entry.progress().completed()).count();
        List<Entry> result = new ArrayList<>();
        for (Entry entry : updated) {
            result.add(entry.mission().goal().equals(MissionGoal.WEEKLY_MISSIONS) ? advance(entry, weeklyDone, changed, completed) : entry);
        }
        return new Update(new MissionBoard(result), changed, completed);
    }

    public long completed(MissionCadence cadence) {
        return entries.stream().filter(entry -> entry.mission().cadence() == cadence && entry.progress().completed()).count();
    }

    public long count(MissionCadence cadence) {
        return entries.stream().filter(entry -> entry.mission().cadence() == cadence).count();
    }

    private static Entry advance(Entry entry, long amount, List<MissionProgress> changed, List<Mission> completed) {
        MissionProgress progress = entry.progress();
        if (progress.completed() || amount <= 0) {
            return entry;
        }
        Mission mission = entry.mission();
        long value = Math.min(mission.target(), mission.goal().apply(progress.progress(), amount));
        if (value == progress.progress()) {
            return entry;
        }
        boolean done = value >= mission.target();
        MissionProgress next = new MissionProgress(mission.id(), progress.start(), value, done);
        changed.add(next);
        if (done) {
            completed.add(mission);
        }
        return new Entry(mission, next);
    }

    public record Entry(Mission mission, MissionProgress progress) {
    }

    public record Update(MissionBoard board, List<MissionProgress> changed, List<Mission> completed) {

        public Update {
            changed = List.copyOf(changed);
            completed = List.copyOf(completed);
        }
    }
}
