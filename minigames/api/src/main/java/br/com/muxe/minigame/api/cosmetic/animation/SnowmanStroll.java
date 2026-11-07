package br.com.muxe.minigame.api.cosmetic.animation;

import br.com.muxe.core.bukkit.cosmetic.animation.Ground;
import br.com.muxe.core.bukkit.cosmetic.animation.Stage.Actor;
import br.com.muxe.core.bukkit.cosmetic.animation.Stage.BlockType;
import br.com.muxe.core.bukkit.cosmetic.animation.Stage;
import br.com.muxe.core.bukkit.cosmetic.animation.Vec;
import java.util.random.RandomGenerator;
import org.bukkit.Material;

final class SnowmanStroll {

    static final double SPEED = 0.0625;
    static final int ROAM = 4;
    static final double MAX_CLIMB = 1;
    static final int MIN_REST = 20;
    static final int MAX_REST = 60;
    static final int STEP_EVERY = 26;
    static final float STEP_VOLUME = 0.15f;

    private final Actor body;
    private final Floor floor;
    private Vec position;
    private Vec target;
    private float yaw;
    private int rest;
    private int walked;

    SnowmanStroll(Actor body, Vec position, float yaw, Floor floor) {
        this.body = body;
        this.floor = floor;
        this.position = position;
        this.yaw = yaw;
    }

    void step(Stage stage, RandomGenerator random) {
        if (rest > 0) {
            rest--;
            return;
        }
        if (target == null) {
            target = pick(stage, random);
            if (target == null) {
                return;
            }
        }
        Vec toward = new Vec(target.x() - position.x(), 0, target.z() - position.z());
        if (toward.length() <= SPEED) {
            position = target;
            target = null;
            rest = random.nextInt(MIN_REST, MAX_REST + 1);
        } else {
            Vec next = position.add(toward.normalize().multiply(SPEED));
            position = Ground.surface(stage::solid, next);
            yaw = Vec.ZERO.yawTowards(toward);
        }
        body.teleport(position, yaw, 0);
        walked++;
        if (walked % STEP_EVERY == 0) {
            stage.sound(step(floor.at((int) Math.floor(position.x()), (int) Math.floor(position.y()) - 1, (int) Math.floor(position.z()))), position, STEP_VOLUME, 1);
        }
    }

    static String step(BlockType under) {
        return under.material() == Material.SNOW_BLOCK ? "step.snow" : "step.stone";
    }

    Vec position() {
        return position;
    }

    void remove() {
        body.remove();
    }

    private Vec pick(Stage stage, RandomGenerator random) {
        Vec spot = position.add(random.nextInt(-ROAM, ROAM + 1), 0, random.nextInt(-ROAM, ROAM + 1));
        Vec landed = Ground.surface(stage::solid, spot);
        boolean standing = stage.solid((int) Math.floor(landed.x()), (int) Math.floor(landed.y()) - 1, (int) Math.floor(landed.z()));
        return standing && Math.abs(landed.y() - position.y()) <= MAX_CLIMB ? landed : null;
    }

    @FunctionalInterface
    interface Floor {

        BlockType at(int x, int y, int z);
    }
}
