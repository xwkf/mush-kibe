package br.com.muxe.minigame.api.cosmetic.animation;

import br.com.muxe.core.bukkit.cosmetic.animation.Ground;
import br.com.muxe.core.bukkit.cosmetic.animation.Vec;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

final class Fireball {

    static final double SPEED = 2.0;
    static final double ACCELERATION = 0.1;
    static final double DRAG = 0.95;
    static final int SAMPLES = 8;
    static final double REACH = 0.6;
    static final double BELOW_FEET = 0.3;
    static final double ABOVE_FEET = 2.1;

    private Vec position;
    private Vec motion;
    private final Vec acceleration;

    Fireball(Vec position, Vec target, Vec heading) {
        this.position = position;
        this.motion = target.subtract(position).normalize().multiply(SPEED);
        this.acceleration = heading.normalize().multiply(ACCELERATION);
    }

    Vec position() {
        return position;
    }

    Vec motion() {
        return motion;
    }

    Optional<Impact> step(Ground.Solid solid, Map<UUID, Vec> feet) {
        for (int sample = 1; sample <= SAMPLES; sample++) {
            Vec point = position.add(motion.multiply((double) sample / SAMPLES));
            for (Map.Entry<UUID, Vec> rider : feet.entrySet()) {
                if (touches(point, rider.getValue())) {
                    return Optional.of(new Impact(position, Optional.of(rider.getKey())));
                }
            }
            if (solid.at((int) Math.floor(point.x()), (int) Math.floor(point.y()), (int) Math.floor(point.z()))) {
                return Optional.of(new Impact(position, Optional.empty()));
            }
        }
        position = position.add(motion);
        motion = motion.add(acceleration).multiply(DRAG);
        return Optional.empty();
    }

    static boolean touches(Vec point, Vec feet) {
        double horizontal = Math.hypot(point.x() - feet.x(), point.z() - feet.z());
        return horizontal <= REACH && point.y() >= feet.y() - BELOW_FEET && point.y() <= feet.y() + ABOVE_FEET;
    }

    record Impact(Vec at, Optional<UUID> rider) {
    }
}
