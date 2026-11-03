package br.com.muxe.minigame.bedwars.quickbuy.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

public record QuickBuy(List<String> slots) {

    public static final int SIZE = 21;
    public static final String EMPTY = "";
    public static final int MAX_ID_LENGTH = 64;

    public QuickBuy {
        if (slots.size() != SIZE) {
            throw new IllegalArgumentException("A quick buy has " + SIZE + " slots, got " + slots.size());
        }
        for (String id : slots) {
            if (id == null || id.length() > MAX_ID_LENGTH) {
                throw new IllegalArgumentException("Invalid quick buy item: " + id);
            }
        }
        slots = List.copyOf(slots);
    }

    public static QuickBuy of(List<String> ids) {
        List<String> slots = new ArrayList<>(SIZE);
        for (int position = 0; position < SIZE; position++) {
            String id = position < ids.size() ? ids.get(position) : null;
            slots.add(id == null ? EMPTY : id);
        }
        return new QuickBuy(slots);
    }

    public Optional<String> at(int position) {
        String id = slots.get(position);
        return id.isEmpty() ? Optional.empty() : Optional.of(id);
    }

    public boolean contains(String id) {
        return !id.isEmpty() && slots.contains(id);
    }

    public QuickBuy place(int position, String id) {
        if (position < 0 || position >= SIZE) {
            throw new IllegalArgumentException("Invalid quick buy position: " + position);
        }
        List<String> changed = new ArrayList<>(without(id).slots);
        changed.set(position, id);
        return new QuickBuy(changed);
    }

    public QuickBuy remove(String id) {
        return without(id);
    }

    public QuickBuy known(Predicate<String> exists) {
        return new QuickBuy(slots.stream().map(id -> id.isEmpty() || exists.test(id) ? id : EMPTY).toList());
    }

    private QuickBuy without(String id) {
        return new QuickBuy(slots.stream().map(current -> current.equals(id) ? EMPTY : current).toList());
    }
}
