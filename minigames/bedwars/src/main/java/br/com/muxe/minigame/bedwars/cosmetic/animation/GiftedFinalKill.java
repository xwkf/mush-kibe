package br.com.muxe.minigame.bedwars.cosmetic.animation;

import br.com.muxe.core.bukkit.cosmetic.animation.Scene;
import br.com.muxe.core.bukkit.cosmetic.animation.SceneAnimation;
import br.com.muxe.core.bukkit.cosmetic.animation.Stage;
import br.com.muxe.core.bukkit.cosmetic.animation.Stage.Actor;
import br.com.muxe.core.bukkit.cosmetic.animation.Stage.Slot;
import br.com.muxe.core.bukkit.cosmetic.animation.Stage.StandStyle;
import br.com.muxe.core.bukkit.cosmetic.animation.Timeline;
import br.com.muxe.core.bukkit.cosmetic.animation.Vec;
import java.util.List;
import java.util.random.RandomGenerator;

public final class GiftedFinalKill implements SceneAnimation {

    static final String BIG_GIFT = "143e0f73d8f447a573f33226fe4f9683b64dda42e7142c130b5b33c29f160183";
    static final List<String> GIFTS = List.of(
        "5b85e29f29ec9a90e482ea5b8391bcb4560bbae0dcd15d7ce1d86016b4356e98",
        "e00afa9898346fbaca43bbf93ddc13db64d23202b30de479457ca883871aa86c",
        "2b1ec7dc753061ca174424ea45cf9490b39cd5dcca477d138a603e6be755ec72",
        "69b564a6f73283112a70b9ce7e15753eb86bd12e7659ec4d0dc0855c6bea76e",
        "626b199c9cce33d96c6730de34c2ad9ec75eefad6fafe06b6c636ab53cdd2fb0",
        "ee3a8fd0852977444d9fd7797cac07b8d3948addc43f0bb5ce25ae72d95dc",
        "103416095268d93265c4cfc219415335fcaa72a51bb1ebca3cefc5cb302e1941",
        "28893da6c340975c37c9b990d3dc8a8944c5a62cb1034ace3add66980a59344"
    );
    static final int[] FIREWORK_COLORS = {0xFF0000, 0xFFFFFF};
    static final double HEAD_ABOVE_BASE = 1.44;
    static final double PARTICLE_ABOVE_BASE = 1.2;
    static final double START_BELOW = 1.25;
    static final double RISE = 0.19;
    static final int RISE_TICKS = 26;
    static final float FIRST_PITCH = 0.75f;
    static final float PITCH_STEP = 0.05f;
    static final double MIN_SPREAD = 0.3;
    static final double MAX_SPREAD = 0.5;
    static final double MIN_LIFT = 0.85;
    static final double MAX_LIFT = 1.35;
    static final int END = 87;

    @Override
    public String id() {
        return "presenteado";
    }

    @Override
    public void script(Scene scene, Stage stage, Timeline timeline, RandomGenerator random) {
        Vec origin = scene.origin();
        Vec start = origin.add(0, -START_BELOW, 0);
        Actor big = stage.armorStand(start, 0, new StandStyle(true, false, false, false, false));
        big.equipSkull(Slot.HELMET, BIG_GIFT);
        timeline.repeat(0, RISE_TICKS - 1, 1, tick -> {
            Vec at = rising(start, tick);
            big.teleport(at, 0, 0);
            if (tick % 2 == 0) {
                int beat = tick / 2;
                float pitch = FIRST_PITCH + beat * PITCH_STEP;
                Vec head = at.add(0, PARTICLE_ABOVE_BASE, 0);
                stage.sound("note.pling", head, 1, pitch);
                stage.sound("fireworks.blast", head, 1, pitch);
                stage.particle(beat % 2 == 0 ? "REDSTONE" : "FIREWORKS_SPARK", head, 20, Vec.ZERO, 0);
            }
        });
        timeline.at(RISE_TICKS, () -> {
            Vec top = rising(start, RISE_TICKS).add(0, PARTICLE_ABOVE_BASE, 0);
            stage.sound("random.explode", top, 1, 1);
            stage.particle("EXPLOSION_LARGE", top, 1, Vec.ZERO, 1);
            big.remove();
        });
        Kit.flash(stage, timeline, RISE_TICKS, origin, Kit.burst(Kit.BURST, false, true, FIREWORK_COLORS));
        double first = random.nextDouble() * 360;
        for (int gift = 0; gift < GIFTS.size(); gift++) {
            Vec launch = Kit.horizontal(first + gift * 45, Kit.between(random, MIN_SPREAD, MAX_SPREAD)).add(0, Kit.between(random, MIN_LIFT, MAX_LIFT), 0);
            gift(stage, timeline, origin, launch, GIFTS.get(gift));
        }
        timeline.lasts(END + 1);
    }

    static Vec rising(Vec start, int tick) {
        return start.add(0, RISE * tick, 0);
    }

    private static void gift(Stage stage, Timeline timeline, Vec origin, Vec launch, String texture) {
        Body body = Body.living(origin, launch);
        Actor[] box = new Actor[1];
        timeline.at(RISE_TICKS, () -> {
            box[0] = stage.armorStand(origin.add(0, -HEAD_ABOVE_BASE, 0), 0, new StandStyle(true, false, false, false, false));
            box[0].equipSkull(Slot.HELMET, texture);
        });
        timeline.repeat(RISE_TICKS + 1, END, 1, offset -> {
            if (!body.landed()) {
                body.step(stage);
            }
            box[0].teleport(body.position().add(0, -HEAD_ABOVE_BASE, 0), 0, 0);
        });
        timeline.at(END + 1, () -> box[0].remove());
    }
}
