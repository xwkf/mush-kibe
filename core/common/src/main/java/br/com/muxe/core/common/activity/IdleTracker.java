package br.com.muxe.core.common.activity;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class IdleTracker {

    private final Clock clock;
    private final Duration idleAfter;
    private final Map<UUID, Instant> lastActive = new ConcurrentHashMap<>();

    public IdleTracker(Clock clock, Duration idleAfter) {
        this.clock = clock;
        this.idleAfter = idleAfter;
    }

    public void active(UUID player) {
        lastActive.put(player, clock.instant());
    }

    public boolean isIdle(UUID player) {
        Instant last = lastActive.get(player);
        return last != null && !clock.instant().isBefore(last.plus(idleAfter));
    }

    public void forget(UUID player) {
        lastActive.remove(player);
    }
}
