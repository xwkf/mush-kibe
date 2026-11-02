package br.com.muxe.minigame.bedwars.stats.domain;

public enum TopCategory {
    FINAL_KILLS,
    WINS,
    WINSTREAK,
    LEVEL;

    public long valueOf(StatCounters counters) {
        return switch (this) {
            case FINAL_KILLS -> counters.finalKills();
            case WINS -> counters.wins();
            case WINSTREAK -> counters.winstreak();
            case LEVEL -> counters.xp();
        };
    }
}
