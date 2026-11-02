package br.com.muxe.minigame.bedwars.game;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public final class KillCredit {

    public static final Duration WINDOW = Duration.ofSeconds(15);

    private final Duration window;
    private final Map<UUID, Hit> lastHits = new HashMap<>();
    private final Map<UUID, Map<UUID, Instant>> attackers = new HashMap<>();

    public KillCredit() {
        this(WINDOW);
    }

    public KillCredit(Duration window) {
        this.window = window;
    }

    public void hit(UUID victim, UUID attacker, Instant at) {
        if (!victim.equals(attacker)) {
            lastHits.put(victim, new Hit(attacker, at));
            attackers.computeIfAbsent(victim, id -> new LinkedHashMap<>()).put(attacker, at);
        }
    }

    public Optional<UUID> lastHitter(UUID victim, Instant now) {
        Hit hit = lastHits.get(victim);
        if (hit == null || hit.at().plus(window).isBefore(now)) {
            return Optional.empty();
        }
        return Optional.of(hit.attacker());
    }

    public Optional<UUID> killer(UUID victim, Instant now, boolean finalKill, Optional<UUID> bedDestroyer) {
        Optional<UUID> hitter = lastHitter(victim, now);
        if (hitter.isPresent() || !finalKill) {
            return hitter;
        }
        return bedDestroyer.filter(destroyer -> !destroyer.equals(victim));
    }

    public List<UUID> assisters(UUID victim, UUID killer, Instant now) {
        Map<UUID, Instant> hits = attackers.getOrDefault(victim, Map.of());
        List<UUID> assisters = new ArrayList<>();
        for (Map.Entry<UUID, Instant> hit : hits.entrySet()) {
            if (!hit.getKey().equals(killer) && !hit.getValue().plus(window).isBefore(now)) {
                assisters.add(hit.getKey());
            }
        }
        return assisters;
    }

    public void forget(UUID victim) {
        lastHits.remove(victim);
        attackers.remove(victim);
    }

    private record Hit(UUID attacker, Instant at) {
    }
}
