package br.com.muxe.minigame.bedwars.cosmetic.trail;

import br.com.muxe.core.bukkit.cosmetic.animation.Vec;

final class FakeSource implements FlightSource {

    Vec position = Vec.ZERO;
    Vec velocity = new Vec(0, 0, 1);
    boolean valid = true;
    boolean onGround;

    void moveOn() {
        position = position.add(velocity);
    }

    @Override
    public Vec position() {
        return position;
    }

    @Override
    public Vec velocity() {
        return velocity;
    }

    @Override
    public boolean valid() {
        return valid;
    }

    @Override
    public boolean onGround() {
        return onGround;
    }
}
