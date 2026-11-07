package br.com.muxe.minigame.bedwars.game;

public record BedWarsEvent(Kind kind, int tier) {

    public enum Kind {
        DIAMOND_TIER,
        EMERALD_TIER,
        BEDS_DESTROYED,
        DRAGONS,
        GAME_END
    }
}
