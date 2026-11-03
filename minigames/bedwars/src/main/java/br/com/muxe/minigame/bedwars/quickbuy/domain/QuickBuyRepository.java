package br.com.muxe.minigame.bedwars.quickbuy.domain;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface QuickBuyRepository {

    Optional<QuickBuy> find(UUID playerId);

    void save(UUID playerId, QuickBuy quickBuy, Instant at);
}
