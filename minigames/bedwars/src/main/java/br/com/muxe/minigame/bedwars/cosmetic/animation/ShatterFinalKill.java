package br.com.muxe.minigame.bedwars.cosmetic.animation;

import br.com.muxe.core.bukkit.cosmetic.animation.Scene;
import br.com.muxe.core.bukkit.cosmetic.animation.SceneAnimation;
import br.com.muxe.core.bukkit.cosmetic.animation.Stage;
import br.com.muxe.core.bukkit.cosmetic.animation.Timeline;
import br.com.muxe.core.bukkit.cosmetic.animation.Vec;
import java.util.random.RandomGenerator;
import org.bukkit.Material;

public record ShatterFinalKill(String id, Material material, int data, String sound, int sounds) implements SceneAnimation {

    static final double[] HEIGHTS = {0.25, 0.5, 0.75, 1, 1.25, 1.5};
    static final double DENSEST = 0.5;
    static final int SHARDS = 25;
    static final int BLUE = 11;

    public static ShatterFinalKill glass() {
        return new ShatterFinalKill("quebrado", Material.STAINED_GLASS, BLUE, "dig.glass", 1);
    }

    public static ShatterFinalKill redstone() {
        return new ShatterFinalKill("redstone", Material.REDSTONE_BLOCK, 0, "dig.stone", 5);
    }

    @Override
    public void script(Scene scene, Stage stage, Timeline timeline, RandomGenerator random) {
        Vec origin = scene.origin();
        int block = Kit.blockId(material, data);
        for (double height : HEIGHTS) {
            stage.particle("BLOCK_CRACK", origin.add(0, height, 0), shards(height), Vec.ZERO, 1, block);
        }
        for (int index = 0; index < sounds; index++) {
            stage.sound(sound, origin, 1, 1);
        }
        timeline.lasts(1);
    }

    static int shards(double height) {
        return height == DENSEST ? SHARDS * 2 : SHARDS;
    }
}
