package br.com.muxe.minigame.api.cosmetic.trail;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;

public final class TrailFlights {

    private final List<Tracked> flights = new ArrayList<>();
    private final BiConsumer<String, RuntimeException> failures;

    public TrailFlights(BiConsumer<String, RuntimeException> failures) {
        this.failures = failures;
    }

    public void add(UUID owner, String group, TrailFlight flight) {
        flights.add(new Tracked(owner, group, flight));
    }

    public void tick(BiPredicate<UUID, String> active) {
        Iterator<Tracked> iterator = flights.iterator();
        while (iterator.hasNext()) {
            Tracked tracked = iterator.next();
            if (!active.test(tracked.owner(), tracked.group())) {
                iterator.remove();
                close(tracked.flight());
                continue;
            }
            try {
                if (!tracked.flight().advance()) {
                    iterator.remove();
                }
            } catch (RuntimeException exception) {
                iterator.remove();
                close(tracked.flight());
                failures.accept(tracked.flight().trailId(), exception);
            }
        }
    }

    public void stop(UUID owner) {
        flights.removeIf(tracked -> {
            if (!tracked.owner().equals(owner)) {
                return false;
            }
            close(tracked.flight());
            return true;
        });
    }

    public void stopAll() {
        List<Tracked> all = List.copyOf(flights);
        flights.clear();
        all.forEach(tracked -> close(tracked.flight()));
    }

    public boolean isEmpty() {
        return flights.isEmpty();
    }

    public int size() {
        return flights.size();
    }

    private void close(TrailFlight flight) {
        try {
            flight.close();
        } catch (RuntimeException exception) {
            failures.accept(flight.trailId(), exception);
        }
    }

    private record Tracked(UUID owner, String group, TrailFlight flight) {
    }
}
