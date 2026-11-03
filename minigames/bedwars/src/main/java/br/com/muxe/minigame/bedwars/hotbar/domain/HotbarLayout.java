package br.com.muxe.minigame.bedwars.hotbar.domain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.OptionalInt;
import java.util.Set;
import java.util.function.Predicate;

public record HotbarLayout(List<List<HotbarEntry>> slots) {

    public static final int SLOTS = 9;
    public static final int MAX_ENTRIES = 14;
    private static final String ENTRY_SEPARATOR = ",";

    public HotbarLayout {
        if (slots.size() != SLOTS) {
            throw new IllegalArgumentException("A hotbar layout has " + SLOTS + " slots, got " + slots.size());
        }
        Set<HotbarEntry> seen = new HashSet<>();
        List<List<HotbarEntry>> copy = new ArrayList<>(SLOTS);
        for (List<HotbarEntry> entries : slots) {
            if (entries.size() > MAX_ENTRIES) {
                throw new IllegalArgumentException("A hotbar slot holds at most " + MAX_ENTRIES + " entries, got " + entries.size());
            }
            for (HotbarEntry entry : entries) {
                if (!seen.add(entry)) {
                    throw new IllegalArgumentException("A hotbar entry can only be in one slot: " + entry);
                }
            }
            copy.add(List.copyOf(entries));
        }
        slots = List.copyOf(copy);
    }

    public static HotbarLayout empty() {
        return new HotbarLayout(Collections.nCopies(SLOTS, List.of()));
    }

    public static HotbarLayout decode(List<String> stored) {
        Set<HotbarEntry> seen = new HashSet<>();
        List<List<HotbarEntry>> slots = new ArrayList<>(SLOTS);
        for (int slot = 0; slot < SLOTS; slot++) {
            List<HotbarEntry> entries = new ArrayList<>();
            String text = slot < stored.size() && stored.get(slot) != null ? stored.get(slot) : "";
            for (String part : text.split(ENTRY_SEPARATOR)) {
                HotbarEntry.decode(part).filter(entry -> entries.size() < MAX_ENTRIES && seen.add(entry)).ifPresent(entries::add);
            }
            slots.add(entries);
        }
        return new HotbarLayout(slots);
    }

    public List<String> encode() {
        return slots.stream().map(entries -> String.join(ENTRY_SEPARATOR, entries.stream().map(HotbarEntry::encoded).toList())).toList();
    }

    public List<HotbarEntry> entries(int slot) {
        requireSlot(slot);
        return slots.get(slot);
    }

    public boolean isEmpty() {
        return slots.stream().allMatch(List::isEmpty);
    }

    public boolean isFull(int slot) {
        return entries(slot).size() >= MAX_ENTRIES;
    }

    public boolean contains(HotbarEntry entry) {
        return slotOf(entry).isPresent();
    }

    public OptionalInt slotOf(HotbarEntry entry) {
        for (int slot = 0; slot < SLOTS; slot++) {
            if (slots.get(slot).contains(entry)) {
                return OptionalInt.of(slot);
            }
        }
        return OptionalInt.empty();
    }

    public boolean accepts(int slot, String itemId, Set<String> categoryIds) {
        return entries(slot).stream().anyMatch(entry -> entry.matches(itemId, categoryIds));
    }

    public HotbarLayout add(int slot, HotbarEntry entry) {
        requireSlot(slot);
        if (slots.get(slot).contains(entry)) {
            return this;
        }
        if (isFull(slot)) {
            throw new IllegalStateException("Hotbar slot " + slot + " is full");
        }
        List<List<HotbarEntry>> changed = new ArrayList<>(SLOTS);
        for (int position = 0; position < SLOTS; position++) {
            List<HotbarEntry> entries = new ArrayList<>(slots.get(position));
            if (position == slot) {
                entries.add(entry);
            } else {
                entries.remove(entry);
            }
            changed.add(entries);
        }
        return new HotbarLayout(changed);
    }

    public HotbarLayout remove(int slot, HotbarEntry entry) {
        requireSlot(slot);
        List<HotbarEntry> entries = new ArrayList<>(slots.get(slot));
        if (!entries.remove(entry)) {
            return this;
        }
        List<List<HotbarEntry>> changed = new ArrayList<>(slots);
        changed.set(slot, entries);
        return new HotbarLayout(changed);
    }

    public HotbarLayout known(Predicate<HotbarEntry> exists) {
        return new HotbarLayout(slots.stream().map(entries -> entries.stream().filter(exists).toList()).toList());
    }

    private static void requireSlot(int slot) {
        if (slot < 0 || slot >= SLOTS) {
            throw new IllegalArgumentException("Invalid hotbar slot: " + slot);
        }
    }
}
