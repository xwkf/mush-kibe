package br.com.muxe.minigame.api.cosmetic.animation;

import br.com.muxe.core.bukkit.cosmetic.animation.Stage.Actor;
import br.com.muxe.core.bukkit.cosmetic.animation.Stage.Pose;
import br.com.muxe.core.bukkit.cosmetic.animation.Stage.Slot;
import br.com.muxe.core.bukkit.cosmetic.animation.Stage.StandStyle;
import br.com.muxe.core.bukkit.cosmetic.animation.Stage;
import br.com.muxe.core.bukkit.cosmetic.animation.Vec;
import java.util.Optional;

final class WitherSkull {

    static final String TEXTURE = "b95d875140b99235ec5e4486242a70da881a7c183b650ceafae0084bceb66182";
    static final StandStyle STYLE = new StandStyle(true, false, false, false, false);
    static final double LIFT = 1.3125;
    static final double HEAD = 1.45;
    static final double FIRST_STEP = 10;
    static final double DRAG = 0.95;
    static final int STEPS = 8;
    static final double TRACE = 0.25;

    private final Actor stand;
    private final Vec direction;
    private final float yaw;
    private Vec head;
    private int steps;

    private WitherSkull(Actor stand, Vec head, Vec direction, float yaw) {
        this.stand = stand;
        this.head = head;
        this.direction = direction;
        this.yaw = yaw;
    }

    static WitherSkull launch(Stage stage, Vec wither, float yaw, float pitch) {
        Vec standAt = wither.add(0, LIFT, 0);
        Actor stand = stage.armorStand(standAt, yaw, STYLE);
        stand.equipSkull(Slot.HELMET, TEXTURE);
        stand.pose(Pose.STANDING.withHead(new Vec(pitch, 0, 0)));
        return new WitherSkull(stand, standAt.add(0, HEAD, 0), Flight.direction(yaw, pitch), yaw);
    }

    static double stride(int step) {
        return FIRST_STEP * Math.pow(DRAG, step);
    }

    Optional<Vec> fly(Stage stage) {
        if (steps >= STEPS) {
            return Optional.of(head);
        }
        double length = stride(steps);
        steps++;
        int probes = (int) Math.ceil(length / TRACE);
        for (int probe = 1; probe <= probes; probe++) {
            double travelled = Math.min(length, probe * TRACE);
            Vec point = head.add(direction.multiply(travelled));
            if (stage.solid((int) Math.floor(point.x()), (int) Math.floor(point.y()), (int) Math.floor(point.z()))) {
                head = head.add(direction.multiply((probe - 1) * TRACE));
                stand.teleport(head.add(0, -HEAD, 0), yaw, 0);
                return Optional.of(head);
            }
        }
        head = head.add(direction.multiply(length));
        stand.teleport(head.add(0, -HEAD, 0), yaw, 0);
        return Optional.empty();
    }

    void remove() {
        stand.remove();
    }
}
