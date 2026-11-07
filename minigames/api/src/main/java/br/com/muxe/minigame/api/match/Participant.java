package br.com.muxe.minigame.api.match;

import br.com.muxe.minigame.engine.team.TeamColor;
import java.time.Instant;
import java.util.UUID;

public final class Participant {

    public enum State {
        WAITING,
        ALIVE,
        RESPAWNING,
        DISCONNECTED,
        ELIMINATED
    }

    private final UUID id;
    private final String name;
    private TeamColor team;
    private State state = State.WAITING;
    private int kills;
    private int finalKills;
    private int deaths;
    private int finalDeaths;
    private int assists;
    private int finalAssists;
    private int killStreak;
    private int bestKillStreak;
    private long xp;
    private int respawnAt;
    private Instant protectedUntil = Instant.MIN;
    private Instant disconnectedUntil = Instant.MIN;

    public Participant(UUID id, String name) {
        this.id = id;
        this.name = name;
    }

    public UUID id() {
        return id;
    }

    public String name() {
        return name;
    }

    public TeamColor team() {
        return team;
    }

    void team(TeamColor value) {
        this.team = value;
    }

    public State state() {
        return state;
    }

    void state(State value) {
        this.state = value;
    }

    public boolean isAlive() {
        return state == State.ALIVE;
    }

    public boolean isPlaying() {
        return state == State.ALIVE || state == State.RESPAWNING || state == State.DISCONNECTED;
    }

    public int kills() {
        return kills;
    }

    public int finalKills() {
        return finalKills;
    }

    public int deaths() {
        return deaths;
    }

    public int finalDeaths() {
        return finalDeaths;
    }

    public int assists() {
        return assists;
    }

    public int finalAssists() {
        return finalAssists;
    }

    public int bestKillStreak() {
        return bestKillStreak;
    }

    public long xp() {
        return xp;
    }

    public void addXp(long amount) {
        xp += Math.max(0, amount);
    }

    void addKill(boolean finalKill) {
        if (finalKill) {
            finalKills++;
        } else {
            kills++;
        }
        killStreak++;
        bestKillStreak = Math.max(bestKillStreak, killStreak);
    }

    void addDeath(boolean finalDeath) {
        killStreak = 0;
        if (finalDeath) {
            finalDeaths++;
        } else {
            deaths++;
        }
    }

    void addAssist(boolean finalAssist) {
        if (finalAssist) {
            finalAssists++;
        } else {
            assists++;
        }
    }

    public int respawnAt() {
        return respawnAt;
    }

    void respawnAt(int elapsedSeconds) {
        this.respawnAt = elapsedSeconds;
    }

    public boolean isProtected(Instant now) {
        return now.isBefore(protectedUntil);
    }

    void protectUntil(Instant until) {
        this.protectedUntil = until;
    }

    public Instant disconnectedUntil() {
        return disconnectedUntil;
    }

    void disconnectedUntil(Instant until) {
        this.disconnectedUntil = until;
    }
}
