package br.com.muxe.minigame.bedwars.hotbar.domain;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface HotbarRepository {

    Optional<HotbarLayout> find(UUID playerId);

    void save(UUID playerId, HotbarLayout layout, Instant at);
}
