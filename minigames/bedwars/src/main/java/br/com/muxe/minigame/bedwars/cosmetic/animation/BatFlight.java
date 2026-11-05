package br.com.muxe.minigame.bedwars.cosmetic.animation;

import br.com.muxe.core.bukkit.cosmetic.animation.Vec;
import java.util.random.RandomGenerator;

final class BatFlight {

    static final double CRUISE = 0.3;
    static final double CLIMB = 0.3;
    static final double LAUNCH_SPEED = 0.2;
    static final double LAUNCH_RISE = 0.45;
    static final double STEER = 0.1;
    static final int WANDER = 6;
    static final int LOWEST_HOP = -1;
    static final int HIGHEST_HOP = 5;
    static final double REACHED = 2;
    static final int RETARGET_ODDS = 30;
    static final double MIN_ALTITUDE = 1.5;
    static final double MAX_ALTITUDE = 14;
    static final double MAX_DISTANCE = 8;

    private final Vec home;
    private Vec position;
    private Vec motion;
    private Vec target;

    BatFlight(Vec home, double launchDegrees) {
        this.home = home;
        this.position = home;
        this.motion = Kit.horizontal(launchDegrees, LAUNCH_SPEED).add(0, LAUNCH_RISE, 0);
    }

    Vec step(RandomGenerator random) {
        if (target == null || random.nextInt(RETARGET_ODDS) == 0 || target.distance(position) < REACHED) {
            target = retarget(random);
        }
        Vec toward = target.subtract(position);
        motion = new Vec(
            motion.x() + (Math.signum(toward.x()) * CRUISE - motion.x()) * STEER,
            motion.y() + (Math.signum(toward.y()) * CLIMB - motion.y()) * STEER,
            motion.z() + (Math.signum(toward.z()) * CRUISE - motion.z()) * STEER);
        position = position.add(motion);
        return position;
    }

    Vec position() {
        return position;
    }

    Vec motion() {
        return motion;
    }

    float yaw() {
        return Vec.ZERO.yawTowards(motion);
    }

    private Vec retarget(RandomGenerator random) {
        double x = position.x() + random.nextInt(-WANDER, WANDER + 1);
        double y = position.y() + random.nextInt(LOWEST_HOP, HIGHEST_HOP + 1);
        double z = position.z() + random.nextInt(-WANDER, WANDER + 1);
        double dx = x - home.x();
        double dz = z - home.z();
        double distance = Math.hypot(dx, dz);
        if (distance > MAX_DISTANCE) {
            x = home.x() + dx * MAX_DISTANCE / distance;
            z = home.z() + dz * MAX_DISTANCE / distance;
        }
        y = Math.clamp(y, home.y() + MIN_ALTITUDE, home.y() + MAX_ALTITUDE);
        return new Vec(x, y, z);
    }
}
