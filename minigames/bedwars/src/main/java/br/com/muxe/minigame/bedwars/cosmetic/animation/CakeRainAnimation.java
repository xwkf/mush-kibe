package br.com.muxe.minigame.bedwars.cosmetic.animation;

import br.com.muxe.core.bukkit.cosmetic.animation.BedAnimation;
import br.com.muxe.core.bukkit.cosmetic.animation.BedFrame;
import br.com.muxe.core.bukkit.cosmetic.animation.Stage;
import br.com.muxe.core.bukkit.cosmetic.animation.Stage.Actor;
import br.com.muxe.core.bukkit.cosmetic.animation.Stage.Slot;
import br.com.muxe.core.bukkit.cosmetic.animation.Stage.StandStyle;
import br.com.muxe.core.bukkit.cosmetic.animation.Timeline;
import br.com.muxe.core.bukkit.cosmetic.animation.Vec;
import java.util.ArrayList;
import java.util.List;
import java.util.random.RandomGenerator;
import org.bukkit.Material;

public final class CakeRainAnimation implements BedAnimation {

    static final String[] CAKES = {
        "23d811c06254ac99e89d024a4fd85a8dc67ab6a83c91a3b7456b50a29ae66604",
        "8fd03a8db015531e6ee5e8284f49be415d547332b70b7f3fb19929b4d3f97e73"
    };
    static final StandStyle CAKE_STAND = new StandStyle(true, true, false, false, false);
    static final Material[] TREATS = {Material.SUGAR, Material.CAKE};
    static final int COUNT = 6;
    static final double CAKE_DROP = -0.781;
    static final double CAKE_TOP = 0.5625;
    static final int FLIGHT = 10;
    static final double CLIMB = 0.875;
    static final double GRAVITY = 0.2;
    static final double MIN_SPEED = 0.15;
    static final double MAX_SPEED = 0.25;
    static final double DRAG = 0.92;
    static final double TREAT_LIFT = 1;
    static final double TREAT_SPEED = 0.1;
    static final double TREAT_UP = 0.4;
    static final int BURST = 2 + FLIGHT;
    static final int SPARKS = 175;
    static final int CAKES_GONE = 52;
    static final int END = 53;

    @Override
    public String id() {
        return "chuva-de-bolos";
    }

    @Override
    public void script(BedFrame bed, Vec breaker, Stage stage, Timeline timeline, RandomGenerator random) {
        Vec middle = bed.middle();
        Vec ground = middle.add(0, CAKE_DROP, 0);
        double treatAngle = random.nextDouble() * 360;
        List<Actor> cakes = new ArrayList<>();
        List<Actor> treats = new ArrayList<>();
        timeline.at(0, () -> stage.sound("fireworks.blast", middle, 1, SoundPitch.of(94)));
        for (int index = 0; index < COUNT; index++) {
            int cake = index;
            int launch = launch(cake);
            Vec direction = bed.forward().multiply(Math.cos(Math.toRadians(60.0 * cake)))
                .add(bed.right().multiply(Math.sin(Math.toRadians(60.0 * cake))));
            double speed = Kit.between(random, MIN_SPEED, MAX_SPEED);
            Actor[] stand = new Actor[1];
            timeline.at(launch, () -> {
                stand[0] = stage.armorStand(ground, 0, CAKE_STAND);
                stand[0].equipSkull(Slot.HELMET, CAKES[cake % CAKES.length]);
                cakes.add(stand[0]);
                Vec start = middle.add(0, TREAT_LIFT, 0);
                Actor treat = stage.item(start, TREATS[cake % TREATS.length], 0);
                treat.velocity(Kit.horizontal(treatAngle + 60.0 * cake, TREAT_SPEED).add(0, TREAT_UP, 0));
                treats.add(treat);
                if (cake % 2 == 0) {
                    stage.sound("random.pop", middle, 1, 1);
                }
            });
            timeline.repeat(launch + 1, launch + FLIGHT, 1, offset -> {
                int step = offset + 1;
                Vec at = ground.add(direction.multiply(distance(speed, step))).add(0, height(step), 0);
                stand[0].teleport(at, 0, 0);
                double shade = (launch + step) % 2 == 1 ? 1 : 0;
                Kit.dust(stage, at.add(0, CAKE_TOP, 0), 1, shade, shade);
            });
        }
        timeline.at(BURST, () -> {
            for (int spark = 0; spark < SPARKS; spark++) {
                Vec push = Kit.horizontal(random.nextDouble() * 360, Kit.between(random, 0.25, 0.5)).add(0, Kit.between(random, 0, 0.05), 0);
                stage.particle("FIREWORKS_SPARK", middle, 0, push, 1);
            }
        });
        timeline.at(CAKES_GONE, () -> cakes.forEach(Actor::remove));
        timeline.at(END, () -> treats.forEach(Actor::remove));
    }

    static int launch(int cake) {
        return cake / 2;
    }

    static double height(int step) {
        if (step >= FLIGHT) {
            return 0;
        }
        return step * CLIMB - GRAVITY * step * (step - 1) / 2;
    }

    static double distance(double speed, int step) {
        return speed * (1 - Math.pow(DRAG, step)) / (1 - DRAG);
    }
}
