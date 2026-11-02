package br.com.muxe.core.common.mission.domain;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public final class MissionGames {

    private final Map<String, Game> games = new ConcurrentHashMap<>();

    public void register(String game, MissionCatalog catalog, MissionRewards rewards) {
        games.put(game, new Game(catalog, rewards));
    }

    public Optional<Game> find(String game) {
        return Optional.ofNullable(games.get(game));
    }

    public record Game(MissionCatalog catalog, MissionRewards rewards) {
    }
}
