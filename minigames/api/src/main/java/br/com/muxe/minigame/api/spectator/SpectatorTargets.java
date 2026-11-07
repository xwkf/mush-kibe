package br.com.muxe.minigame.api.spectator;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.random.RandomGenerator;

public final class SpectatorTargets {

    private SpectatorTargets() {
    }

    public static Optional<UUID> random(List<UUID> alive, UUID spectator, RandomGenerator random) {
        return pick(alive.stream().filter(id -> !id.equals(spectator)).toList(), random);
    }

    public static Optional<UUID> inCombat(List<UUID> alive, Set<UUID> fighting, UUID spectator, RandomGenerator random) {
        return pick(alive.stream().filter(id -> !id.equals(spectator) && fighting.contains(id)).toList(), random);
    }

    public static boolean hidden(boolean viewerIsSpectator, boolean targetIsSpectator) {
        return targetIsSpectator && !viewerIsSpectator;
    }

    private static Optional<UUID> pick(List<UUID> candidates, RandomGenerator random) {
        return candidates.isEmpty() ? Optional.empty() : Optional.of(candidates.get(random.nextInt(candidates.size())));
    }
}
