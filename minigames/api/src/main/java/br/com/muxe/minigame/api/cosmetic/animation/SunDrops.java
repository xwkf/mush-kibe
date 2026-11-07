package br.com.muxe.minigame.api.cosmetic.animation;

import br.com.muxe.core.bukkit.cosmetic.animation.Stage;
import br.com.muxe.core.bukkit.cosmetic.animation.Vec;
import br.com.muxe.minigame.api.cosmetic.animation.DesertSun.Dot.Tint;
import java.util.ArrayList;
import java.util.List;
import java.util.random.RandomGenerator;

final class SunDrops {

    static final List<Tint> TINTS = List.of(
        new Tint(1, 1, 1),
        new Tint(1, 0.75, 0.88),
        new Tint(1, 0.7, 0.28),
        new Tint(1, 0.84, 0),
        new Tint(1, 0.95, 0.63)
    );
    static final double HALF_WIDTH = 0.75;
    static final double FALL = 0.1;
    static final double FLOOR = 0.05;
    static final int PER_TICK = 2;

    private final Vec center;
    private final Vec right;
    private final double floor;
    private final List<Drop> drops = new ArrayList<>();

    SunDrops(Vec center, Vec right, double ground) {
        this.center = center;
        this.right = right;
        this.floor = ground + FLOOR;
    }

    static int fallTicks(double height) {
        return (int) Math.ceil((height - FLOOR) / FALL);
    }

    void tick(Stage stage, RandomGenerator random, boolean pouring) {
        if (pouring) {
            for (int drop = 0; drop < PER_TICK; drop++) {
                drops.add(new Drop(Kit.between(random, -HALF_WIDTH, HALF_WIDTH), center.y(), TINTS.get(random.nextInt(TINTS.size()))));
            }
        }
        List<Drop> falling = new ArrayList<>();
        for (Drop drop : drops) {
            if (drop.height() > floor) {
                Vec at = new Vec(center.x(), drop.height(), center.z()).add(right.multiply(drop.across()));
                Kit.dust(stage, at, drop.tint().red(), drop.tint().green(), drop.tint().blue());
                falling.add(new Drop(drop.across(), drop.height() - FALL, drop.tint()));
            }
        }
        drops.clear();
        drops.addAll(falling);
    }

    int falling() {
        return drops.size();
    }

    private record Drop(double across, double height, Tint tint) {
    }
}
