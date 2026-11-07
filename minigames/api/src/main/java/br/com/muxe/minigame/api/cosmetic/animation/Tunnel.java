package br.com.muxe.minigame.api.cosmetic.animation;

import br.com.muxe.core.bukkit.cosmetic.animation.Stage.BlockChange;
import br.com.muxe.core.bukkit.cosmetic.animation.Stage.BlockType;
import br.com.muxe.core.bukkit.cosmetic.animation.Stage;
import br.com.muxe.core.bukkit.cosmetic.animation.Vec;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.random.RandomGenerator;
import org.bukkit.Material;

final class Tunnel {

    static final double HALF_WIDTH = 3.5;
    static final int HEIGHT = 8;
    static final double DEBRIS_CHANCE = 1.0 / 6;
    static final double CRACK_CHANCE = 1.0 / 18;
    static final int DEBRIS_DELAY = 2;
    static final double THROW_SPEED = 2;
    static final double THROW_LIFT = 1;

    private final Stage stage;
    private final RandomGenerator random;
    private final Set<Long> dug = new HashSet<>();
    private final List<Throw> throwing = new ArrayList<>();
    private final List<Debris> flying = new ArrayList<>();

    Tunnel(Stage stage, RandomGenerator random) {
        this.stage = stage;
        this.random = random;
    }

    void dig(Vec center, int tick) {
        List<BlockChange> changes = new ArrayList<>();
        int baseY = (int) Math.floor(center.y());
        for (int x = first(center.x()); x <= last(center.x()); x++) {
            for (int z = first(center.z()); z <= last(center.z()); z++) {
                for (int y = baseY; y <= baseY + HEIGHT; y++) {
                    if (dug.contains(key(x, y, z))) {
                        continue;
                    }
                    BlockType type = stage.type(x, y, z);
                    if (type.material() == Material.AIR) {
                        continue;
                    }
                    dug.add(key(x, y, z));
                    changes.add(new BlockChange(x, y, z, Material.AIR, 0));
                    if (random.nextDouble() < CRACK_CHANCE) {
                        stage.breakEffect(x, y, z, type.material(), type.data());
                    }
                    if (random.nextDouble() < DEBRIS_CHANCE) {
                        Vec spot = new Vec(x + 0.5, y, z + 0.5);
                        throwing.add(new Throw(tick + DEBRIS_DELAY, spot, type, launch(center, spot)));
                    }
                }
            }
        }
        if (!changes.isEmpty()) {
            stage.blocks(changes);
        }
    }

    void update(int tick) {
        flying.removeIf(debris -> debris.step(this::solid));
        throwing.removeIf(due -> {
            if (due.tick() > tick) {
                return false;
            }
            flying.add(Debris.launch(stage, due.spot(), due.type(), due.velocity()));
            return true;
        });
    }

    void clear() {
        flying.forEach(Debris::remove);
        flying.clear();
        throwing.clear();
    }

    boolean dug(int x, int y, int z) {
        return dug.contains(key(x, y, z));
    }

    boolean solid(int x, int y, int z) {
        return !dug(x, y, z) && stage.solid(x, y, z);
    }

    static Vec launch(Vec center, Vec spot) {
        Vec outward = spot.subtract(center).normalize().multiply(THROW_SPEED);
        return new Vec(outward.x(), THROW_LIFT, outward.z());
    }

    static int first(double center) {
        return (int) Math.ceil(center - HALF_WIDTH - 0.5);
    }

    static int last(double center) {
        return (int) Math.floor(center + HALF_WIDTH - 0.5);
    }

    private static long key(int x, int y, int z) {
        return ((long) x & 0x3FFFFFF) << 38 | ((long) z & 0x3FFFFFF) << 12 | (y & 0xFFF);
    }

    private record Throw(int tick, Vec spot, BlockType type, Vec velocity) {
    }
}
