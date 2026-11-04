package br.com.muxe.core.common.afk;

import br.com.muxe.core.common.store.ExpiringStore;
import java.time.Duration;
import java.util.Optional;
import java.util.UUID;

public final class AfkReturns {

    private static final String PREFIX = "afk:return:";

    private final ExpiringStore store;

    public AfkReturns(ExpiringStore store) {
        this.store = store;
    }

    public void remember(UUID player, String serverId, Duration ttl) {
        store.put(PREFIX + player, serverId, ttl);
    }

    public Optional<String> originOf(UUID player) {
        return store.get(PREFIX + player);
    }

    public void forget(UUID player) {
        store.delete(PREFIX + player);
    }
}
