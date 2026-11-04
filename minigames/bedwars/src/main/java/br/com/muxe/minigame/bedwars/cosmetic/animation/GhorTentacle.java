package br.com.muxe.minigame.bedwars.cosmetic.animation;

import br.com.muxe.core.bukkit.cosmetic.animation.Stage;
import br.com.muxe.core.bukkit.cosmetic.animation.Stage.Actor;
import br.com.muxe.core.bukkit.cosmetic.animation.Stage.Slot;
import br.com.muxe.core.bukkit.cosmetic.animation.Vec;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

final class GhorTentacle {

    static final String SKIN = "cef119f08851a72a5f10fbc3247d95e1c006360d2b4f412b23ce054092756b0c";
    static final int SEGMENTS = 18;
    static final int THICK = 3;
    static final double SMALL_LIFT = 0.78;
    static final double BACK = 0.9;
    static final double HIGH = 0.53;
    static final double WIDE = 0.69;
    static final double SIDE_BOW = 1.25;
    static final double UP_BOW = 0.95;
    static final double RISE = 0.15;
    static final double FALL_SHAPE = 1.2;
    static final double FULL_BOW = 2;
    static final double WAVE = 0.2;
    static final double WAVE_LENGTH = 1.5;
    static final double WAVE_TICKS = 12;

    private final int up;
    private final int side;
    private final List<Actor> segments;
    private List<Vec> points;

    private GhorTentacle(int up, int side, List<Actor> segments, List<Vec> points) {
        this.up = up;
        this.side = side;
        this.segments = segments;
        this.points = points;
    }

    static GhorTentacle spawn(Stage stage, Vec root, int up, int side) {
        List<Actor> segments = new ArrayList<>();
        for (int segment = 0; segment < SEGMENTS; segment++) {
            Actor actor = stage.armorStand(stand(root, segment), 0, segment < THICK ? GhorEye.PIECE : GhorEye.SMALL_PIECE);
            actor.equipSkull(Slot.HELMET, SKIN);
            segments.add(actor);
        }
        return new GhorTentacle(up, side, segments, Collections.nCopies(SEGMENTS, root));
    }

    static Vec root(Vec center, Gaze gaze, int up, int side) {
        return center.add(gaze.ahead().multiply(-BACK)).add(gaze.up().multiply(up * HIGH)).add(gaze.side().multiply(side * WIDE));
    }

    static List<Vec> points(Vec root, Vec tip, int up, int side, int tick) {
        Vec span = tip.subtract(root);
        double length = span.length();
        if (length < 1.0E-3) {
            return Collections.nCopies(SEGMENTS, root);
        }
        Vec along = span.normalize();
        Vec across = new Vec(-along.z(), 0, along.x()).normalize();
        if (across.length() == 0) {
            across = new Vec(1, 0, 0);
        }
        Vec lift = cross(across, along);
        double grown = Math.min(1, length / FULL_BOW);
        List<Vec> points = new ArrayList<>();
        for (int segment = 0; segment < SEGMENTS; segment++) {
            double share = segment / (double) (SEGMENTS - 1);
            double bow = bow(share) * grown;
            double wave = WAVE * Math.sin(2 * Math.PI * (share * WAVE_LENGTH - tick / WAVE_TICKS) + up + 2 * side);
            Vec outward = across.multiply(side * (SIDE_BOW + wave) * bow).add(lift.multiply(up * (UP_BOW - wave) * bow));
            points.add(root.add(span.multiply(share)).add(outward));
        }
        return List.copyOf(points);
    }

    static double bow(double share) {
        if (share <= 0 || share >= 1) {
            return 0;
        }
        if (share < RISE) {
            return Math.sin(Math.PI / 2 * share / RISE);
        }
        return Math.pow(Math.cos(Math.PI / 2 * (share - RISE) / (1 - RISE)), FALL_SHAPE);
    }

    int up() {
        return up;
    }

    int side() {
        return side;
    }

    Vec point(int segment) {
        return points.get(segment);
    }

    void place(List<Vec> points) {
        this.points = points;
        float yaw = 0;
        for (int segment = 0; segment < SEGMENTS; segment++) {
            Vec at = points.get(segment);
            if (segment + 1 < SEGMENTS && at.distance(points.get(segment + 1)) > 1.0E-3) {
                yaw = at.yawTowards(points.get(segment + 1));
            }
            segments.get(segment).teleport(stand(at, segment), yaw, 0);
        }
    }

    void remove() {
        segments.forEach(Actor::remove);
    }

    private static Vec stand(Vec point, int segment) {
        return segment < THICK ? point : point.add(0, SMALL_LIFT, 0);
    }

    private static Vec cross(Vec first, Vec second) {
        return new Vec(first.y() * second.z() - first.z() * second.y(), first.z() * second.x() - first.x() * second.z(), first.x() * second.y() - first.y() * second.x());
    }
}
