package br.com.muxe.minigame.engine.world.server;

import org.bukkit.World;

final class MatchWorldRules {

    private MatchWorldRules() {
    }

    static void apply(World world) {
        world.setPVP(true);
        world.setAutoSave(false);
        world.setKeepSpawnInMemory(false);
        world.setStorm(false);
        world.setThundering(false);
        world.setTime(6000);
        world.setGameRuleValue("doDaylightCycle", "false");
        world.setGameRuleValue("doMobSpawning", "false");
        world.setGameRuleValue("randomTickSpeed", "0");
        world.setGameRuleValue("doFireTick", "false");
        world.setGameRuleValue("mobGriefing", "false");
    }
}
