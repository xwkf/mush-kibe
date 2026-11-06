package br.com.muxe.minigame.bedwars.cosmetic.animation;

import br.com.muxe.core.bukkit.cosmetic.animation.Stage;
import br.com.muxe.core.bukkit.cosmetic.animation.Stage.BlockChange;
import br.com.muxe.core.bukkit.cosmetic.animation.Vec;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.random.RandomGenerator;
import org.bukkit.Material;

final class Blight {

    static final int RADIUS = 20;
    static final double SCATTER = 8;
    static final int PER_TICK = 45;
    static final double SAND_SHARE = 0.5;
    static final int MIN_HEIGHT = 0;
    static final int MAX_HEIGHT = 255;

    private final Stage stage;
    private final RandomGenerator random;
    private final Set<Long> taken = new HashSet<>();
    private final List<Deque<int[]>> fronts = new ArrayList<>();

    Blight(Stage stage, RandomGenerator random) {
        this.stage = stage;
        this.random = random;
    }

    static long key(int x, int y, int z) {
        return ((long) x & 0x3FFFFFF) << 38 | ((long) z & 0x3FFFFFF) << 12 | (y & 0xFFF);
    }

    void spread(Vec centre) {
        int centreX = (int) Math.floor(centre.x());
        int centreY = (int) Math.floor(centre.y());
        int centreZ = (int) Math.floor(centre.z());
        List<Candidate> candidates = new ArrayList<>();
        for (int x = centreX - RADIUS; x <= centreX + RADIUS; x++) {
            for (int y = Math.max(MIN_HEIGHT, centreY - RADIUS); y <= Math.min(MAX_HEIGHT, centreY + RADIUS); y++) {
                for (int z = centreZ - RADIUS; z <= centreZ + RADIUS; z++) {
                    double distance = centre.distance(new Vec(x + 0.5, y + 0.5, z + 0.5));
                    if (distance <= RADIUS && stage.solid(x, y, z)) {
                        candidates.add(new Candidate(new int[] {x, y, z}, distance + random.nextDouble() * SCATTER));
                    }
                }
            }
        }
        candidates.sort(Comparator.comparingDouble(Candidate::order));
        Deque<int[]> front = new ArrayDeque<>();
        candidates.forEach(candidate -> front.add(candidate.block()));
        fronts.add(front);
    }

    void step() {
        List<BlockChange> changes = new ArrayList<>();
        Iterator<Deque<int[]>> each = fronts.iterator();
        while (each.hasNext()) {
            Deque<int[]> front = each.next();
            int converted = 0;
            while (converted < PER_TICK && !front.isEmpty()) {
                int[] block = front.poll();
                if (taken.add(key(block[0], block[1], block[2]))) {
                    Material material = random.nextDouble() < SAND_SHARE ? Material.SAND : Material.CLAY;
                    changes.add(new BlockChange(block[0], block[1], block[2], material, 0));
                    converted++;
                }
            }
            if (front.isEmpty()) {
                each.remove();
            }
        }
        if (!changes.isEmpty()) {
            stage.blocks(changes);
        }
    }

    boolean spreading() {
        return !fronts.isEmpty();
    }

    private record Candidate(int[] block, double order) {
    }
}
