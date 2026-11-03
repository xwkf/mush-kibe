package br.com.muxe.minigame.bedwars.cosmetic.preview;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

final class BedBreakers {

    private final Set<UUID> breakers = new HashSet<>();

    boolean broke(UUID viewer) {
        boolean first = breakers.isEmpty();
        return breakers.add(viewer) && first;
    }

    boolean left(UUID viewer) {
        return breakers.remove(viewer) && breakers.isEmpty();
    }

    boolean broken() {
        return !breakers.isEmpty();
    }

    boolean breaker(UUID viewer) {
        return breakers.contains(viewer);
    }
}
