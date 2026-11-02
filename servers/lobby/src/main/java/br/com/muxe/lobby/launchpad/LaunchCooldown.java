package br.com.muxe.lobby.launchpad;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class LaunchCooldown {

    private final Clock clock;
    private final Duration cooldown;
    private final Map<UUID, Instant> readyAt = new HashMap<>();

    public LaunchCooldown(Clock clock, Duration cooldown) {
        this.clock = clock;
        this.cooldown = cooldown;
    }

    public boolean tryUse(UUID playerId) {
        Instant now = clock.instant();
        Instant ready = readyAt.get(playerId);
        if (ready != null && now.isBefore(ready)) {
            return false;
        }
        readyAt.put(playerId, now.plus(cooldown));
        return true;
    }

    public void forget(UUID playerId) {
        readyAt.remove(playerId);
    }
}
