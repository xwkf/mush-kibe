package br.com.muxe.minigame.bedwars.cosmetic.animation;

import br.com.muxe.core.bukkit.cosmetic.animation.Stage;
import br.com.muxe.core.bukkit.cosmetic.animation.Stage.BlockChange;
import br.com.muxe.core.bukkit.cosmetic.animation.Vec;
import java.util.ArrayList;
import java.util.List;
import java.util.random.RandomGenerator;
import org.bukkit.Material;

final class Quicksand {

    static final int CORE = 1;
    static final int EDGE = 2;
    static final int EDGE_PATCHES = 5;
    static final double SAND_CHANCE = 0.6;
    static final int SMOOTH = 2;
    static final int DUST = 3;
    static final Vec DUST_SPREAD = new Vec(0.3, 0.1, 0.3);

    private Quicksand() {
    }

    static int floorY(Vec origin) {
        return (int) Math.floor(origin.y() - 1e-6);
    }

    static List<BlockChange> plan(Vec origin, RandomGenerator random) {
        int x = (int) Math.floor(origin.x());
        int y = floorY(origin);
        int z = (int) Math.floor(origin.z());
        List<BlockChange> changes = new ArrayList<>();
        for (int dx = -CORE; dx <= CORE; dx++) {
            for (int dz = -CORE; dz <= CORE; dz++) {
                boolean sand = random.nextDouble() < SAND_CHANCE;
                changes.add(new BlockChange(x + dx, y, z + dz, sand ? Material.SAND : Material.SANDSTONE, 0));
            }
        }
        List<int[]> edge = new ArrayList<>();
        for (int dx = -EDGE; dx <= EDGE; dx++) {
            for (int dz = -EDGE; dz <= EDGE; dz++) {
                if (Math.max(Math.abs(dx), Math.abs(dz)) == EDGE) {
                    edge.add(new int[] {dx, dz});
                }
            }
        }
        for (int patch = 0; patch < EDGE_PATCHES; patch++) {
            int[] cell = edge.remove(random.nextInt(edge.size()));
            changes.add(new BlockChange(x + cell[0], y, z + cell[1], Material.SANDSTONE, random.nextBoolean() ? SMOOTH : 0));
        }
        return changes;
    }

    static void spread(Stage stage, Vec origin, RandomGenerator random) {
        for (BlockChange change : plan(origin, random)) {
            if (stage.solid(change.x(), change.y(), change.z())) {
                stage.block(change.x(), change.y(), change.z(), change.material(), change.data());
                Vec top = new Vec(change.x() + 0.5, change.y() + 1, change.z() + 0.5);
                stage.particle("BLOCK_DUST", top, DUST, DUST_SPREAD, 0.05, Kit.blockId(change.material(), change.data()));
            }
        }
        for (int sound = 0; sound < 3; sound++) {
            stage.sound("dig.sand", origin, 0.8f, 69 / 63f);
        }
    }
}
