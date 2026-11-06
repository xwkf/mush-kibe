package br.com.muxe.minigame.bedwars.cosmetic.animation;

import br.com.muxe.core.bukkit.cosmetic.animation.Ground;
import br.com.muxe.core.bukkit.cosmetic.animation.Vec;

final class RocketFlight {

    static final double SPEED = 1.5;
    static final double LIFT = 0.8;
    static final double GRAVITY = 0.08;
    static final double VERTICAL_DRAG = 0.98;
    static final double HORIZONTAL_DRAG = 0.91;

    private final double floor;
    private final double aboveGround;
    private Vec position;
    private Vec velocity = Vec.ZERO;
    private boolean flying;

    RocketFlight(Vec position, double aboveGround) {
        this.position = position;
        this.floor = position.y();
        this.aboveGround = aboveGround;
    }

    void launch(float yaw) {
        velocity = Flight.direction(yaw, 0).multiply(SPEED).add(0, LIFT, 0);
        flying = true;
    }

    void step(Ground.Solid solid) {
        if (!flying) {
            return;
        }
        Vec next = position.add(velocity);
        if (velocity.y() < 0) {
            int below = (int) Math.floor(next.y() - aboveGround);
            if (solid.at((int) Math.floor(next.x()), below, (int) Math.floor(next.z()))) {
                rest(new Vec(next.x(), below + 1 + aboveGround, next.z()));
                return;
            }
            if (next.y() <= floor) {
                rest(new Vec(next.x(), floor, next.z()));
                return;
            }
        }
        position = next;
        velocity = new Vec(velocity.x() * HORIZONTAL_DRAG, (velocity.y() - GRAVITY) * VERTICAL_DRAG, velocity.z() * HORIZONTAL_DRAG);
    }

    Vec position() {
        return position;
    }

    Vec velocity() {
        return velocity;
    }

    boolean flying() {
        return flying;
    }

    private void rest(Vec at) {
        position = at;
        velocity = Vec.ZERO;
        flying = false;
    }
}
