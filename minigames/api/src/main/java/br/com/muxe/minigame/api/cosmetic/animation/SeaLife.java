package br.com.muxe.minigame.api.cosmetic.animation;

import br.com.muxe.core.bukkit.cosmetic.animation.Area;
import br.com.muxe.core.bukkit.cosmetic.animation.Stage;
import br.com.muxe.core.bukkit.cosmetic.animation.Vec;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.random.RandomGenerator;
import org.bukkit.Material;

final class SeaLife {

    static final double SQUARE_BLOCKS_PER_CREATURE = 125;
    static final int MAX_CREATURES = 240;
    static final double GUARDIAN_SHARE = 0.4;
    static final int SPAWN_ATTEMPTS = 8;
    static final String SQUID = "Squid";
    static final String GUARDIAN = "Guardian";

    private final Stage stage;
    private final Flood flood;
    private final RandomGenerator random;
    private final List<Hatchling> waiting = new ArrayList<>();
    private final List<SwimmingSquid> squids = new ArrayList<>();
    private final List<PatrolGuardian> guardians = new ArrayList<>();

    SeaLife(Stage stage, Flood flood, Area area, double originY, RandomGenerator random) {
        this.stage = stage;
        this.flood = flood;
        this.random = random;
        int floor = (int) Math.floor(originY);
        int creatures = count(area);
        for (int creature = 0; creature < creatures; creature++) {
            for (int attempt = 0; attempt < SPAWN_ATTEMPTS; attempt++) {
                Vec spot = area.pick(random, floor + random.nextInt(Math.max(1, flood.top() - floor + 1)));
                int x = (int) Math.floor(spot.x());
                int y = (int) spot.y();
                int z = (int) Math.floor(spot.z());
                if (stage.type(x, y, z).material() == Material.AIR) {
                    waiting.add(new Hatchling(new Vec(x + 0.5, y, z + 0.5), random.nextDouble() < GUARDIAN_SHARE, (float) (random.nextDouble() * 360 - 180)));
                    break;
                }
            }
        }
    }

    static int count(Area area) {
        return (int) Math.min(MAX_CREATURES, Math.max(1, Math.round(area.size() / SQUARE_BLOCKS_PER_CREATURE)));
    }

    void live() {
        Iterator<Hatchling> pending = waiting.iterator();
        while (pending.hasNext()) {
            Hatchling hatchling = pending.next();
            Vec at = hatchling.position();
            if (flood.flooded((int) Math.floor(at.x()), (int) at.y(), (int) Math.floor(at.z()))) {
                hatch(hatchling);
                pending.remove();
            }
        }
        squids.forEach(squid -> squid.swim(flood));
        guardians.forEach(guardian -> guardian.patrol(flood, squids));
    }

    private void hatch(Hatchling hatchling) {
        int phase = squids.size() + guardians.size();
        if (hatchling.guardian()) {
            guardians.add(new PatrolGuardian(stage.mob(GUARDIAN, hatchling.position(), hatchling.yaw()), hatchling.position(), hatchling.yaw(), random, phase));
        } else {
            squids.add(new SwimmingSquid(stage.mob(SQUID, hatchling.position(), hatchling.yaw()), hatchling.position(), random, phase));
        }
    }

    private record Hatchling(Vec position, boolean guardian, float yaw) {
    }
}
