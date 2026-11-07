package br.com.muxe.minigame.api.match;

public record TeamStatus(Kind kind, int alive) {

    public enum Kind {
        RESPAWNS,
        RESPAWNS_OFFLINE,
        LAST_LIFE,
        ELIMINATED
    }

    public static TeamStatus of(boolean respawns, int aliveMembers, int connectedMembers, boolean canReturn) {
        if (aliveMembers <= 0 && !(respawns && canReturn)) {
            return new TeamStatus(Kind.ELIMINATED, 0);
        }
        if (!respawns) {
            return new TeamStatus(Kind.LAST_LIFE, aliveMembers);
        }
        return new TeamStatus(connectedMembers > 0 ? Kind.RESPAWNS : Kind.RESPAWNS_OFFLINE, aliveMembers);
    }

    public boolean isEliminated() {
        return kind == Kind.ELIMINATED;
    }
}
