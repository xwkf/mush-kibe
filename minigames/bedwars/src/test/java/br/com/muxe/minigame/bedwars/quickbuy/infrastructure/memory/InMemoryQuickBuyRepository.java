package br.com.muxe.minigame.bedwars.quickbuy.infrastructure.memory;

import br.com.muxe.minigame.bedwars.quickbuy.domain.QuickBuy;
import br.com.muxe.minigame.bedwars.quickbuy.domain.QuickBuyRepository;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public final class InMemoryQuickBuyRepository implements QuickBuyRepository {

    private final Map<UUID, QuickBuy> saved = new HashMap<>();
    private final Map<UUID, Instant> updatedAt = new HashMap<>();

    @Override
    public Optional<QuickBuy> find(UUID playerId) {
        return Optional.ofNullable(saved.get(playerId));
    }

    @Override
    public void save(UUID playerId, QuickBuy quickBuy, Instant at) {
        saved.put(playerId, quickBuy);
        updatedAt.put(playerId, at);
    }

    public Optional<Instant> updatedAt(UUID playerId) {
        return Optional.ofNullable(updatedAt.get(playerId));
    }
}
