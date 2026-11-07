package br.com.muxe.minigame.api.cosmetic.animation;

import br.com.muxe.core.bukkit.cosmetic.animation.Stage.BlockChange;
import br.com.muxe.core.bukkit.cosmetic.animation.Stage;
import br.com.muxe.core.bukkit.cosmetic.animation.Vec;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.bukkit.Material;

final class WoolTunnel {

    static final int SIDE = 2;
    static final int BELOW = 1;
    static final int ABOVE = 5;

    private final Set<Block> dug = new HashSet<>();

    List<BlockChange> dig(Stage stage, Vec body) {
        int centerX = (int) Math.floor(body.x());
        int centerY = (int) Math.floor(body.y());
        int centerZ = (int) Math.floor(body.z());
        List<BlockChange> changes = new ArrayList<>();
        for (int x = centerX - SIDE; x <= centerX + SIDE; x++) {
            for (int y = centerY - BELOW; y <= centerY + ABOVE; y++) {
                for (int z = centerZ - SIDE; z <= centerZ + SIDE; z++) {
                    Block block = new Block(x, y, z);
                    if (!dug.contains(block) && stage.type(x, y, z).material() != Material.AIR) {
                        dug.add(block);
                        changes.add(new BlockChange(x, y, z, Material.AIR, 0));
                    }
                }
            }
        }
        return changes;
    }

    private record Block(int x, int y, int z) {
    }
}
