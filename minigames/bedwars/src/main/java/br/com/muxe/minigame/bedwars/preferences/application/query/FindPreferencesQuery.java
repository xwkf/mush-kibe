package br.com.muxe.minigame.bedwars.preferences.application.query;

import br.com.muxe.core.common.cqrs.query.Query;
import br.com.muxe.minigame.bedwars.preferences.domain.Preferences;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public record FindPreferencesQuery(UUID playerId) implements Query<Optional<Preferences>> {

    public FindPreferencesQuery {
        Objects.requireNonNull(playerId, "playerId");
    }
}
