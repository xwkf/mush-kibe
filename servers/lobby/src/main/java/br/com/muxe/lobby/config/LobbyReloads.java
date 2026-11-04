package br.com.muxe.lobby.config;

import br.com.muxe.core.bukkit.config.edit.ConfigReloads;
import br.com.muxe.core.common.config.live.LiveValue;
import br.com.muxe.lobby.scoreboard.ScoreboardTemplate;

public final class LobbyReloads {

    public static final String SPAWN = "spawn";
    public static final String SCOREBOARD_TITLE = "scoreboard.title";
    public static final String SCOREBOARD_LINES = "scoreboard.lines";

    private LobbyReloads() {
    }

    public static ConfigReloads create(LiveValue<ScoreboardTemplate> scoreboard, Runnable spawn) {
        return new ConfigReloads()
            .register(SPAWN, SPAWN, config -> spawn.run())
            .register(SCOREBOARD_TITLE, SCOREBOARD_TITLE, config -> scoreboard.reload(() -> ScoreboardTemplate.from(config)))
            .register(SCOREBOARD_LINES, SCOREBOARD_LINES, config -> scoreboard.reload(() -> ScoreboardTemplate.from(config)));
    }
}
