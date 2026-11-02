package br.com.muxe.minigame.bedwars.game;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class Activity {

    public static final Duration AFK_AFTER = Duration.ofSeconds(45);

    private final Map<UUID, Instant> lastActive = new ConcurrentHashMap<>();

    public void active(UUID player, Instant now) {
        lastActive.put(player, now);
    }

    public boolean isAfk(UUID player, Instant now) {
        Instant last = lastActive.get(player);
        return last != null && !now.isBefore(last.plus(AFK_AFTER));
    }

    public void forget(UUID player) {
        lastActive.remove(player);
    }
}
