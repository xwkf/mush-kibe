package br.com.muxe.minigame.bedwars.cosmetic.animation;

import br.com.muxe.core.bukkit.cosmetic.animation.Vec;
import java.util.random.RandomGenerator;

final class OwlGlide {

    static final double CRUISE = 0.35;
    static final double RISE = 0.12;
    static final double STEER = 0.1;
    static final int WANDER = 6;
    static final int LOWEST_HOP = -1;
    static final int HIGHEST_HOP = 4;
    static final double CEILING = 8;
    static final double MAX_DISTANCE = 10;
    static final double REACHED = 2;
    static final int RETARGET_ODDS = 30;

    private final Vec home;
    private Vec position;
    private Vec motion = Vec.ZERO;
    private Vec target;

    OwlGlide(Vec home) {
        this.home = home;
        this.position = home;
    }

    Vec step(RandomGenerator random) {
        if (target == null || random.nextInt(RETARGET_ODDS) == 0 || target.distance(position) < REACHED) {
            target = retarget(random);
        }
        Vec toward = target.subtract(position);
        motion = new Vec(
            motion.x() + (Math.signum(toward.x()) * CRUISE - motion.x()) * STEER,
            motion.y() + (Math.signum(toward.y()) * RISE - motion.y()) * STEER,
            motion.z() + (Math.signum(toward.z()) * CRUISE - motion.z()) * STEER);
        position = position.add(motion);
        return position;
    }

    Vec position() {
        return position;
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
        return new Vec(x, Math.clamp(y, home.y(), home.y() + CEILING), z);
    }
}
