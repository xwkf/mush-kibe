package br.com.muxe.minigame.engine;

import br.com.muxe.core.common.metrics.MetricRegistry;
import br.com.muxe.core.common.metrics.Tags;
import br.com.muxe.minigame.engine.mode.GameMode;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.IntSupplier;
import java.util.stream.Collectors;

public final class MinigameEngine {

    private final String game;
    private final Map<String, GameMode> modes;
    private volatile IntSupplier activeMatches = () -> 0;
    private volatile IntSupplier readyArenas = () -> 0;

    public MinigameEngine(String game, List<GameMode> modes, MetricRegistry metrics) {
        this.game = game;
        this.modes = modes.stream().collect(Collectors.toUnmodifiableMap(GameMode::id, Function.identity()));
        Tags tags = Tags.of("game", game);
        metrics.gauge("muxe_matches_active", tags, () -> activeMatches.getAsInt());
        metrics.gauge("muxe_arenas_available", tags, () -> readyArenas.getAsInt());
    }

    public String game() {
        return game;
    }

    public Map<String, GameMode> modes() {
        return modes;
    }

    public void track(IntSupplier activeMatches, IntSupplier readyArenas) {
        this.activeMatches = activeMatches;
        this.readyArenas = readyArenas;
    }
}
