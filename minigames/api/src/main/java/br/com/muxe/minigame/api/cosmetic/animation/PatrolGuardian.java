package br.com.muxe.minigame.api.cosmetic.animation;

import br.com.muxe.core.bukkit.cosmetic.animation.Stage.Actor;
import br.com.muxe.core.bukkit.cosmetic.animation.Vec;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.random.RandomGenerator;

final class PatrolGuardian {

    static final int FLAGS = 16;
    static final int MOVING = 2;
    static final int STILL = 0;
    static final int TARGET = 17;
    static final int NO_TARGET = 0;
    static final int ATTACK = 21;
    static final int FIRST_LOCK_MIN = 8;
    static final int FIRST_LOCK_MAX = 24;
    static final int BEAM = 80;
    static final int REST_MIN = 10;
    static final int REST_MAX = 20;
    static final double REACH = 16;
    static final int LEG_MIN = 40;
    static final int LEG_MAX = 80;
    static final double DRIFT_SPEED = 0.04;
    static final double SINK_MIN = -0.03;
    static final double SINK_MAX = 0.01;
    static final double STILL_CHANCE = 0.5;

    private final Actor body;
    private final RandomGenerator random;
    private final int phase;
    private Vec position;
    private Vec drift = Vec.ZERO;
    private float yaw;
    private int legLeft;
    private int lockIn;
    private int beamLeft;
    private boolean moving;
    private int age;

    PatrolGuardian(Actor body, Vec position, float yaw, RandomGenerator random, int phase) {
        this.body = body;
        this.position = position;
        this.yaw = yaw;
        this.random = random;
        this.phase = phase;
        this.lockIn = FIRST_LOCK_MIN + random.nextInt(FIRST_LOCK_MAX - FIRST_LOCK_MIN + 1);
    }

    static Optional<SwimmingSquid> prey(Vec from, List<SwimmingSquid> squids) {
        return squids.stream()
            .filter(squid -> squid.position().distance(from) <= REACH)
            .min(Comparator.comparingDouble(squid -> squid.position().distance(from)));
    }

    void patrol(Flood flood, List<SwimmingSquid> squids) {
        wander(flood);
        hunt(squids);
        if ((age + phase) % SwimmingSquid.TELEPORT_EVERY == 0) {
            body.teleport(position, yaw, 0);
        }
        age++;
    }

    boolean beaming() {
        return beamLeft > 0;
    }

    private void wander(Flood flood) {
        if (legLeft <= 0) {
            legLeft = LEG_MIN + random.nextInt(LEG_MAX - LEG_MIN + 1);
            drift = random.nextDouble() < STILL_CHANCE ? Vec.ZERO : course();
            if (drift.length() > 0) {
                yaw = Vec.ZERO.yawTowards(drift);
            }
            spikes(drift.length() > 0);
        }
        legLeft--;
        Vec next = position.add(drift);
        if (drift.length() > 0 && flood.water((int) Math.floor(next.x()), (int) Math.floor(next.y()), (int) Math.floor(next.z()))) {
            position = next;
        } else if (drift.length() > 0) {
            drift = Vec.ZERO;
            spikes(false);
        }
    }

    private Vec course() {
        double angle = random.nextDouble() * Math.PI * 2;
        return new Vec(Math.cos(angle) * DRIFT_SPEED, SINK_MIN + random.nextDouble() * (SINK_MAX - SINK_MIN), Math.sin(angle) * DRIFT_SPEED);
    }

    private void spikes(boolean retracted) {
        if (retracted != moving) {
            moving = retracted;
            body.watch(FLAGS, retracted ? MOVING : STILL);
        }
    }

    private void hunt(List<SwimmingSquid> squids) {
        if (beamLeft > 0) {
            beamLeft--;
            if (beamLeft == 0) {
                body.watch(TARGET, NO_TARGET);
                lockIn = REST_MIN + random.nextInt(REST_MAX - REST_MIN + 1);
            }
            return;
        }
        if (lockIn > 0) {
            lockIn--;
            return;
        }
        prey(position, squids).ifPresent(squid -> {
            body.status(ATTACK);
            body.target(squid.body());
            beamLeft = BEAM;
        });
    }
}
