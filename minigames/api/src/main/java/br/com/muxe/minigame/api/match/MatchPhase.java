package br.com.muxe.minigame.api.match;

public enum MatchPhase {
    WAITING,
    STARTING,
    PLAYING,
    ENDING;

    public boolean beforeStart() {
        return this == WAITING || this == STARTING;
    }

    public boolean showsTeams() {
        return this == PLAYING || this == ENDING;
    }
}
