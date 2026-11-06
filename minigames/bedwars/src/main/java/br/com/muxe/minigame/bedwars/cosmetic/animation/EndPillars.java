package br.com.muxe.minigame.bedwars.cosmetic.animation;

import br.com.muxe.core.bukkit.cosmetic.animation.Area;
import br.com.muxe.core.bukkit.cosmetic.animation.Stage;
import br.com.muxe.core.bukkit.cosmetic.animation.Stage.BlockChange;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.Material;

final class EndPillars {

    static final int[][] OFFSETS = {{33, 0}, {10, 32}, {-27, 19}, {-27, -19}, {10, -32}};
    static final double RADIUS = 4.5;
    static final int BOTTOM = 0;
    static final int TOP = 127;

    private final List<Pillar> pillars = new ArrayList<>();

    EndPillars(Area area) {
        int middleX = (int) Math.floor(area.middleX());
        int middleZ = (int) Math.floor(area.middleZ());
        for (int[] offset : OFFSETS) {
            pillars.add(new Pillar(middleX + offset[0], middleZ + offset[1]));
        }
    }

    List<Pillar> pillars() {
        return List.copyOf(pillars);
    }

    void holdIn(MapWave wave) {
        for (Pillar pillar : pillars) {
            for (int[] column : pillar.columns()) {
                for (int y = BOTTOM; y <= TOP; y++) {
                    wave.hold(column[0], y, column[1]);
                }
            }
        }
    }

    void raiseReached(Stage stage, MapWave wave) {
        for (Pillar pillar : pillars) {
            if (!pillar.raised && (wave.done() || wave.front() >= MapWave.distance(pillar.x(), pillar.z(), wave.originX(), wave.originZ()))) {
                pillar.raised = true;
                stage.blocks(pillar.blocks());
            }
        }
    }

    static final class Pillar {

        private final int x;
        private final int z;
        private boolean raised;

        Pillar(int x, int z) {
            this.x = x;
            this.z = z;
        }

        int x() {
            return x;
        }

        int z() {
            return z;
        }

        boolean raised() {
            return raised;
        }

        List<int[]> columns() {
            List<int[]> columns = new ArrayList<>();
            int reach = (int) Math.ceil(RADIUS);
            for (int dx = -reach; dx <= reach; dx++) {
                for (int dz = -reach; dz <= reach; dz++) {
                    if (Math.hypot(dx, dz) <= RADIUS) {
                        columns.add(new int[] {x + dx, z + dz});
                    }
                }
            }
            return columns;
        }

        List<BlockChange> blocks() {
            List<BlockChange> blocks = new ArrayList<>();
            for (int[] column : columns()) {
                for (int y = BOTTOM; y <= TOP; y++) {
                    blocks.add(new BlockChange(column[0], y, column[1], Material.OBSIDIAN, 0));
                }
            }
            return blocks;
        }
    }
}
