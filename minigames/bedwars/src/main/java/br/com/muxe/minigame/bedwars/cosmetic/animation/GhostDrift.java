package br.com.muxe.minigame.bedwars.cosmetic.animation;

import br.com.muxe.core.bukkit.cosmetic.animation.Vec;
import java.util.random.RandomGenerator;

final class GhostDrift {

    static final double TOP_SPEED = 0.17;
    static final double STEERING = 0.03;
    static final double REACH = 3;
    static final double MIN_RISE = 0.4;
    static final double MAX_RISE = 1.6;
    static final double ARRIVED = 0.4;
    static final int PATIENCE = 14;
    static final double TURN = 35;

    private final Vec home;
    private final RandomGenerator random;
    private Vec position;
    private Vec velocity = Vec.ZERO;
    private Vec target;
    private double yaw;
    private int heading;

    GhostDrift(Vec home, Vec position, double yaw, RandomGenerator random) {
        this.home = home;
        this.position = position;
        this.yaw = yaw;
        this.random = random;
        this.target = pick();
    }

    void step() {
        if (position.distance(target) < ARRIVED || heading >= PATIENCE) {
            target = pick();
            heading = 0;
        }
        Vec wanted = target.subtract(position).normalize().multiply(TOP_SPEED);
        Vec change = wanted.subtract(velocity);
        if (change.length() > STEERING) {
            change = change.normalize().multiply(STEERING);
        }
        velocity = velocity.add(change);
        position = position.add(velocity);
        if (Math.hypot(velocity.x(), velocity.z()) > 1e-6) {
            yaw += Math.clamp(Flight.wrap(Vec.ZERO.yawTowards(velocity) - yaw), -TURN, TURN);
        }
        heading++;
    }

    Vec position() {
        return position;
    }

    Vec velocity() {
        return velocity;
    }

    Vec target() {
        return target;
    }

    float yaw() {
        return (float) yaw;
    }

    private Vec pick() {
        return home.add(Kit.horizontal(random.nextDouble() * 360, random.nextDouble() * REACH)).add(0, Kit.between(random, MIN_RISE, MAX_RISE), 0);
    }
}
