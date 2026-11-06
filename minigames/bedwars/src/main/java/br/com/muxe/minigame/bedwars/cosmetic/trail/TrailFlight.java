package br.com.muxe.minigame.bedwars.cosmetic.trail;

import br.com.muxe.core.bukkit.cosmetic.animation.Stage;
import java.util.random.RandomGenerator;

public final class TrailFlight {

    public static final int MAX_AGE = 1200;

    private final ProjectileTrail trail;
    private final ProjectileKind kind;
    private final FlightSource source;
    private final Stage stage;
    private final Runnable closeStage;
    private final RandomGenerator random;
    private Phase phase = Phase.FLYING;
    private int age;
    private int lingering;

    public TrailFlight(ProjectileTrail trail, ProjectileKind kind, FlightSource source, Stage stage, Runnable closeStage, RandomGenerator random) {
        this.trail = trail;
        this.kind = kind;
        this.source = source;
        this.stage = stage;
        this.closeStage = closeStage;
        this.random = random;
    }

    public boolean advance() {
        switch (phase) {
            case FLYING -> fly();
            case LINGERING -> {
                lingering--;
                if (lingering <= 0) {
                    close();
                }
            }
            case CLOSED -> {
            }
        }
        return phase != Phase.CLOSED;
    }

    private void fly() {
        Flight now = new Flight(source.position(), source.velocity(), age, kind);
        if (age < MAX_AGE && TrailRules.flying(source.valid(), source.onGround())) {
            trail.tick(now, stage, random);
            age++;
            return;
        }
        phase = Phase.LINGERING;
        lingering = Math.max(0, trail.linger());
        trail.land(now, stage, random);
        if (lingering == 0) {
            close();
        }
    }

    public void close() {
        if (phase == Phase.CLOSED) {
            return;
        }
        phase = Phase.CLOSED;
        closeStage.run();
    }

    public boolean flying() {
        return phase == Phase.FLYING;
    }

    public boolean closed() {
        return phase == Phase.CLOSED;
    }

    public int age() {
        return age;
    }

    public String trailId() {
        return trail.id();
    }

    private enum Phase {
        FLYING,
        LINGERING,
        CLOSED
    }
}
