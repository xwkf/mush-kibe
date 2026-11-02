package br.com.muxe.minigame.bedwars.room;

import br.com.muxe.minigame.engine.team.TeamColor;
import java.time.Instant;
import java.util.UUID;

public final class RoomPlayer {

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
    private int beds;
    private int deaths;
    private int finalDeaths;
    private int assists;
    private int finalAssists;
    private int bedsLost;
    private long xp;
    private int respawnAt;
    private Instant protectedUntil = Instant.MIN;
    private Instant disconnectedUntil = Instant.MIN;
    private int armorTier;
    private int pickaxeTier;
    private int axeTier;
    private boolean shears;
    private int killStreak;
    private int bestKillStreak;
    private int invisibleBeds;
    private int endStones;
    private boolean usedExplosives;
    private int bedbugKills;
    private int emeraldsThisLife;
    private int bestEmeralds;
    private double fireballDamage;
    private int explosiveBlocks;
    private int clayBought;

    public RoomPlayer(UUID id, String name) {
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

    public int beds() {
        return beds;
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

    public int bestKillStreak() {
        return bestKillStreak;
    }

    public int invisibleBeds() {
        return invisibleBeds;
    }

    public void addInvisibleBed() {
        invisibleBeds++;
    }

    public int endStones() {
        return endStones;
    }

    public void addEndStone() {
        endStones++;
    }

    public boolean usedExplosives() {
        return usedExplosives;
    }

    public void useExplosive() {
        usedExplosives = true;
    }

    public int bedbugKills() {
        return bedbugKills;
    }

    public void addBedbugKill() {
        bedbugKills++;
    }

    public int bestEmeralds() {
        return bestEmeralds;
    }

    public void addEmeralds(int amount) {
        emeraldsThisLife += amount;
        bestEmeralds = Math.max(bestEmeralds, emeraldsThisLife);
    }

    public double fireballDamage() {
        return fireballDamage;
    }

    public void addFireballDamage(double amount) {
        fireballDamage += Math.max(0, amount);
    }

    public int explosiveBlocks() {
        return explosiveBlocks;
    }

    public void addExplosiveBlocks(int amount) {
        explosiveBlocks += amount;
    }

    public int clayBought() {
        return clayBought;
    }

    public void addClay(int amount) {
        clayBought += amount;
    }

    void addBed() {
        beds++;
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

    public int bedsLost() {
        return bedsLost;
    }

    public long xp() {
        return xp;
    }

    void addDeath(boolean finalDeath) {
        killStreak = 0;
        emeraldsThisLife = 0;
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

    void loseBed() {
        bedsLost = 1;
    }

    void addXp(long amount) {
        xp += Math.max(0, amount);
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

    public int armorTier() {
        return armorTier;
    }

    public void armorTier(int tier) {
        this.armorTier = Math.max(armorTier, tier);
    }

    public int pickaxeTier() {
        return pickaxeTier;
    }

    public void pickaxeTier(int tier) {
        this.pickaxeTier = tier;
    }

    public int axeTier() {
        return axeTier;
    }

    public void axeTier(int tier) {
        this.axeTier = tier;
    }

    public boolean hasShears() {
        return shears;
    }

    public void giveShears() {
        this.shears = true;
    }

    void downgradeTools() {
        pickaxeTier = Math.max(pickaxeTier > 0 ? 1 : 0, pickaxeTier - 1);
        axeTier = Math.max(axeTier > 0 ? 1 : 0, axeTier - 1);
    }
}
