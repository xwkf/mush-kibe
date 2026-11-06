package br.com.muxe.minigame.bedwars.cosmetic.animation;

import br.com.muxe.core.bukkit.cosmetic.animation.Ground;
import br.com.muxe.core.bukkit.cosmetic.animation.Stage;
import br.com.muxe.core.bukkit.cosmetic.animation.Stage.Actor;
import br.com.muxe.core.bukkit.cosmetic.animation.Stage.BlockType;
import br.com.muxe.core.bukkit.cosmetic.animation.Vec;

final class Debris {

    static final double GRAVITY = 0.04;
    static final double DRAG = 0.98;
    static final int LONGEST = 100;

    private final Actor block;
    private Vec position;
    private Vec velocity;
    private int age;

    private Debris(Actor block, Vec position, Vec velocity) {
        this.block = block;
        this.position = position;
        this.velocity = velocity;
    }

    static Debris launch(Stage stage, Vec spot, BlockType type, Vec velocity) {
        Actor block = stage.fallingBlock(spot, type.material(), type.data());
        block.velocity(velocity);
        return new Debris(block, spot, velocity);
    }

    boolean step(Ground.Solid solid) {
        age++;
        velocity = velocity.add(0, -GRAVITY, 0);
        position = position.add(velocity);
        velocity = velocity.multiply(DRAG);
        if (age >= LONGEST || solid.at((int) Math.floor(position.x()), (int) Math.floor(position.y()), (int) Math.floor(position.z()))) {
            block.remove();
            return true;
        }
        return false;
    }

    Vec position() {
        return position;
    }

    void remove() {
        block.remove();
    }
}
