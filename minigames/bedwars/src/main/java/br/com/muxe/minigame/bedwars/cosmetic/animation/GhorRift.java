package br.com.muxe.minigame.bedwars.cosmetic.animation;

import br.com.muxe.core.bukkit.cosmetic.animation.Stage;
import br.com.muxe.core.bukkit.cosmetic.animation.Vec;
import java.util.random.RandomGenerator;

final class GhorRift {

    static final double RADIUS = 1.5;
    static final double OPENING = 0.62;
    static final double CLOSING = 5.3;
    static final int RING = 30;
    static final int CORE = 10;
    static final double JITTER = 0.1;
    static final double CORE_SHARE = 0.9;
    static final double[][] EDGE = {{0.353, 0.063, 0.439}, {0.231, 0.039, 0.31}, {0.541, 0.063, 0.188}};
    static final double[] DEPTH = {0.078, 0, 0.102};

    private final Vec center;
    private final Vec across;
    private final Vec upward;

    GhorRift(Vec center, Vec facing, Vec right) {
        this.center = center;
        Vec normal = facing.normalize();
        this.across = right.normalize();
        Vec cross = new Vec(normal.y() * across.z() - normal.z() * across.y(), normal.z() * across.x() - normal.x() * across.z(), normal.x() * across.y() - normal.y() * across.x());
        this.upward = cross.y() < 0 ? cross.multiply(-1) : cross;
    }

    static double radius(int tick, int closesAt) {
        if (tick > closesAt) {
            return RADIUS * Math.cos(Math.PI / 2 * Math.min(1, (tick - closesAt) / CLOSING));
        }
        return RADIUS * (1 - Math.pow(OPENING, tick + 1));
    }

    Vec center() {
        return center;
    }

    Vec point(double degrees, double distance) {
        double radians = Math.toRadians(degrees);
        return center.add(across.multiply(Math.cos(radians) * distance)).add(upward.multiply(Math.sin(radians) * distance));
    }

    void draw(Stage stage, double radius, RandomGenerator random) {
        double phase = random.nextDouble() * 360;
        for (int index = 0; index < RING; index++) {
            double[] color = EDGE[random.nextInt(EDGE.length)];
            Vec at = point(phase + index * 360.0 / RING, radius + Kit.between(random, -JITTER, JITTER));
            Kit.dust(stage, at, color[0], color[1], color[2]);
        }
        for (int index = 0; index < CORE; index++) {
            Vec at = point(random.nextDouble() * 360, radius * CORE_SHARE * Math.sqrt(random.nextDouble()));
            Kit.dust(stage, at, DEPTH[0], DEPTH[1], DEPTH[2]);
        }
    }
}
