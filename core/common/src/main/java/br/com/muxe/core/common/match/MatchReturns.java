package br.com.muxe.core.common.match;

import br.com.muxe.core.common.store.ExpiringStore;
import java.time.Duration;
import java.util.Optional;
import java.util.UUID;

public final class MatchReturns {

    private static final String PREFIX = "match:return:";

    private final ExpiringStore store;

    public MatchReturns(ExpiringStore store) {
        this.store = store;
    }

    public void remember(UUID player, String serverId, Duration ttl) {
        store.put(PREFIX + player, serverId, ttl);
    }

    public Optional<String> serverOf(UUID player) {
        return store.get(PREFIX + player);
    }

    public void forget(UUID player) {
        store.delete(PREFIX + player);
    }
}
