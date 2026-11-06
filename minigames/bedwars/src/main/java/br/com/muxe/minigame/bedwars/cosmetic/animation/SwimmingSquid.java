package br.com.muxe.minigame.bedwars.cosmetic.animation;

import br.com.muxe.core.bukkit.cosmetic.animation.Stage.Actor;
import br.com.muxe.core.bukkit.cosmetic.animation.Vec;
import java.util.random.RandomGenerator;

final class SwimmingSquid {

    static final double SWIM_SPEED = 0.2;
    static final double SWIM_LIFT = 0.1;
    static final double TURN_CHANCE = 1.0 / 50;
    static final double DRAG = 0.9;
    static final double FASTEST_STROKE = 0.2;
    static final double STROKE_START = 0.75 * Math.PI;
    static final int TENTACLES_RESET = 19;
    static final int TELEPORT_EVERY = 3;

    private final Actor body;
    private final RandomGenerator random;
    private final int cycle;
    private final int pushFrom;
    private final int pushUntil;
    private final int phase;
    private Vec position;
    private Vec heading;
    private double speed;
    private int age;

    SwimmingSquid(Actor body, Vec position, RandomGenerator random, int phase) {
        this.body = body;
        this.position = position;
        this.random = random;
        this.phase = phase;
        double turn = FASTEST_STROKE / (1 + random.nextDouble());
        this.cycle = (int) Math.ceil(2 * Math.PI / turn);
        this.pushFrom = (int) Math.ceil(STROKE_START / turn);
        this.pushUntil = (int) Math.ceil(Math.PI / turn);
        this.heading = heading(random);
    }

    static Vec heading(RandomGenerator random) {
        double angle = random.nextDouble() * Math.PI * 2;
        return new Vec(Math.cos(angle) * SWIM_SPEED, random.nextDouble() * 2 * SWIM_LIFT - SWIM_LIFT, Math.sin(angle) * SWIM_SPEED);
    }

    static double speed(double previous, int inCycle, int pushFrom, int pushUntil) {
        return inCycle >= pushFrom && inCycle < pushUntil ? 1 : previous * DRAG;
    }

    void swim(Flood flood) {
        int inCycle = age % cycle;
        if (inCycle == 0) {
            body.status(TENTACLES_RESET);
        }
        if (random.nextDouble() < TURN_CHANCE) {
            heading = heading(random);
        }
        speed = speed(speed, inCycle, pushFrom, pushUntil);
        if (inCycle == pushFrom) {
            body.velocity(heading);
        }
        Vec next = position.add(heading.multiply(speed));
        if (flood.water((int) Math.floor(next.x()), (int) Math.floor(next.y()), (int) Math.floor(next.z()))) {
            position = next;
        } else {
            heading = heading(random);
        }
        if ((age + phase) % TELEPORT_EVERY == 0) {
            body.teleport(position, Vec.ZERO.yawTowards(heading), 0);
        }
        age++;
    }

    Actor body() {
        return body;
    }

    Vec position() {
        return position;
    }
}
