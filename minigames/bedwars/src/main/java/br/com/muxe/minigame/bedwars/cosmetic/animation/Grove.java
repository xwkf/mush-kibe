package br.com.muxe.minigame.bedwars.cosmetic.animation;

import br.com.muxe.core.bukkit.cosmetic.animation.Ground;
import br.com.muxe.core.bukkit.cosmetic.animation.Stage;
import br.com.muxe.core.bukkit.cosmetic.animation.Stage.BlockChange;
import br.com.muxe.core.bukkit.cosmetic.animation.Vec;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.random.RandomGenerator;
import org.bukkit.Material;

final class Grove {

    static final int SEARCH_ABOVE = 16;
    static final int SEARCH_DEPTH = 48;

    private final Stage stage;
    private final Vec anchor;
    private final Map<Long, Material> grown = new HashMap<>();

    Grove(Stage stage, Vec anchor) {
        this.stage = stage;
        this.anchor = anchor;
    }

    Optional<Vec> spot(RandomGenerator random, double radius) {
        Vec offset = Kit.horizontal(random.nextDouble() * 360, radius * Math.sqrt(random.nextDouble()));
        double x = Math.floor(anchor.x() + offset.x()) + 0.5;
        double z = Math.floor(anchor.z() + offset.z()) + 0.5;
        OptionalDouble floor = Ground.hit(this::solid, x, anchor.y() + SEARCH_ABOVE, z, SEARCH_DEPTH);
        return floor.isPresent() ? Optional.of(new Vec(x, floor.getAsDouble(), z)) : Optional.empty();
    }

    boolean solid(int x, int y, int z) {
        Material material = grown.get(key(x, y, z));
        return material == null ? stage.solid(x, y, z) : material == Material.HUGE_MUSHROOM_1 || material == Material.HUGE_MUSHROOM_2;
    }

    boolean free(int x, int y, int z) {
        return !grown.containsKey(key(x, y, z)) && stage.type(x, y, z).material() == Material.AIR;
    }

    void grow(List<BlockChange> changes) {
        changes.forEach(change -> grown.put(key(change.x(), change.y(), change.z()), change.material()));
        if (!changes.isEmpty()) {
            stage.blocks(changes);
        }
    }

    Material at(int x, int y, int z) {
        return grown.getOrDefault(key(x, y, z), Material.AIR);
    }

    private static long key(int x, int y, int z) {
        return ((long) x & 0x3FFFFFF) << 38 | ((long) z & 0x3FFFFFF) << 12 | (y & 0xFFF);
    }
}
