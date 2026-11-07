package br.com.muxe.minigame.api.timeline;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public final class Timeline<E> {

    private final List<Entry<E>> entries;

    public Timeline(List<Entry<E>> entries) {
        this.entries = entries.stream().sorted(Comparator.comparingInt(Entry::at)).toList();
    }

    public static <E> Timeline<E> empty() {
        return new Timeline<>(List.of());
    }

    public List<Entry<E>> entries() {
        return entries;
    }

    public Optional<Upcoming> next(int elapsedSeconds) {
        for (Entry<E> entry : entries) {
            if (entry.at() > elapsedSeconds) {
                return Optional.of(new Upcoming(entry.name(), entry.at() - elapsedSeconds));
            }
        }
        return Optional.empty();
    }

    public List<E> dueAt(int elapsedSeconds) {
        List<E> due = new ArrayList<>();
        for (Entry<E> entry : entries) {
            if (entry.at() == elapsedSeconds) {
                due.add(entry.event());
            }
        }
        return due;
    }

    public record Entry<E>(E event, int at, String name) {

        public Entry {
            if (at < 0) {
                throw new IllegalArgumentException("Event " + name + " cannot happen before the start");
            }
        }
    }

    public record Upcoming(String name, int seconds) {

        public String clock() {
            return seconds / 60 + ":" + String.format("%02d", seconds % 60);
        }
    }
}
