package br.com.muxe.minigame.api.cosmetic.animation;

import br.com.muxe.core.bukkit.cosmetic.animation.Stage;
import br.com.muxe.core.bukkit.cosmetic.animation.Vec;

public final class Body {

    private final double gravity;
    private final double verticalDrag;
    private final double horizontalDrag;
    private Vec position;
    private Vec velocity;
    private boolean landed;

    public Body(Vec position, Vec velocity, double gravity, double verticalDrag, double horizontalDrag) {
        this.position = position;
        this.velocity = velocity;
        this.gravity = gravity;
        this.verticalDrag = verticalDrag;
        this.horizontalDrag = horizontalDrag;
    }

    public static Body item(Vec position, Vec velocity) {
        return new Body(position, velocity, 0.04, 0.98, 0.98);
    }

    public static Body living(Vec position, Vec velocity) {
        return new Body(position, velocity, 0.08, 0.98, 0.91);
    }

    public boolean step(Stage stage) {
        if (landed) {
            return false;
        }
        Vec next = position.add(velocity);
        if (velocity.y() < 0 && stage.solid((int) Math.floor(next.x()), (int) Math.floor(next.y()), (int) Math.floor(next.z()))) {
            landed = true;
            position = new Vec(next.x(), Math.floor(next.y()) + 1, next.z());
            return true;
        }
        position = next;
        velocity = new Vec(velocity.x() * horizontalDrag, (velocity.y() - gravity) * verticalDrag, velocity.z() * horizontalDrag);
        return false;
    }

    public Vec position() {
        return position;
    }

    public Vec velocity() {
        return velocity;
    }

    public boolean landed() {
        return landed;
    }
}
