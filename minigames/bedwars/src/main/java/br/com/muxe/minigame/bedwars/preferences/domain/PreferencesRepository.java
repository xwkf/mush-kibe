package br.com.muxe.minigame.bedwars.preferences.domain;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface PreferencesRepository {

    Optional<Preferences> find(UUID playerId);

    void save(UUID playerId, Preferences preferences, Instant at);
}
