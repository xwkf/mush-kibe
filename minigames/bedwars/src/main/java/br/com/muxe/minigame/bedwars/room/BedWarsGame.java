package br.com.muxe.minigame.bedwars.room;

import br.com.muxe.minigame.api.runtime.MatchRules;
import br.com.muxe.minigame.api.runtime.MatchRuntime;
import br.com.muxe.minigame.api.runtime.Minigame;
import br.com.muxe.minigame.api.runtime.TeamPreferences;
import br.com.muxe.minigame.bedwars.arena.BedWarsArena;
import br.com.muxe.minigame.engine.arena.LoadedArena;
import org.bukkit.Material;

public final class BedWarsGame implements Minigame<BedWarsArena, BedWarsMatch> {

    public static final String NAME_TAG_PREFIX = "mxbw";

    private final BedWarsServices services;
    private final TeamPreferences preferences;

    public BedWarsGame(BedWarsServices services, TeamPreferences preferences) {
        this.services = services;
        this.preferences = preferences;
    }

    public static MatchRules matchRules() {
        return MatchRules.defaults(NAME_TAG_PREFIX).respawning().keeping(BedWarsGame::keptTool);
    }

    static boolean keptTool(Material type) {
        return type == Material.WOOD_SWORD || type.name().endsWith("_PICKAXE") || type.name().endsWith("_AXE") || type == Material.SHEARS;
    }

    @Override
    public String id() {
        return BedWarsMatch.GAME;
    }

    @Override
    public MatchRules rules() {
        return matchRules();
    }

    @Override
    public BedWarsArena arena(LoadedArena loaded) {
        return BedWarsArena.from(loaded);
    }

    @Override
    public BedWarsMatch open(MatchRuntime<BedWarsMatch> runtime, BedWarsArena arena) {
        return new BedWarsMatch(runtime, services, arena);
    }

    @Override
    public TeamPreferences teamPreferences() {
        return preferences;
    }
}
