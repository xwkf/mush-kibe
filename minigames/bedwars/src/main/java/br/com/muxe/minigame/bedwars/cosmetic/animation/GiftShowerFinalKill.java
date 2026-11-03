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

public final class GiftShowerFinalKill implements SceneAnimation {

    static final List<String> GIFTS = List.of(
        "2b1ec7dc753061ca174424ea45cf9490b39cd5dcca477d138a603e6be755ec72",
        "69b564a6f73283112a70b9ce7e15753eb86bd12e7659ec4d0dc0855c6bea76e",
        "626b199c9cce33d96c6730de34c2ad9ec75eefad6fafe06b6c636ab53cdd2fb0",
        "ee3a8fd0852977444d9fd7797cac07b8d3948addc43f0bb5ce25ae72d95dc",
        "28893da6c340975c37c9b990d3dc8a8944c5a62cb1034ace3add66980a59344",
        "103416095268d93265c4cfc219415335fcaa72a51bb1ebca3cefc5cb302e1941",
        "143e0f73d8f447a573f33226fe4f9683b64dda42e7142c130b5b33c29f160183",
        "5b85e29f29ec9a90e482ea5b8391bcb4560bbae0dcd15d7ce1d86016b4356e98"
    );
    static final int[] FIREWORK_COLORS = {0xB5251A, 0x2C7F28, 0xFFFFFF};
    static final double HEAD_ABOVE_BASE = 1.44;
    static final int GIFT_EVERY = 2;
    static final double HORIZONTAL_SPEED = 0.28;
    static final double VERTICAL_SPEED = 1.19;
    static final float SPIN = 47;
    static final double SPIN_DECAY = 0.85;
    static final int LIFE = 74;
    static final float POP_PITCH = 0.49f;

    @Override
    public String id() {
        return "presentes";
    }

    @Override
    public void script(Scene scene, Stage stage, Timeline timeline, RandomGenerator random) {
        Vec origin = scene.origin();
        Actor rocket = stage.firework(origin, List.of(Kit.burst(Kit.STAR, false, true, FIREWORK_COLORS)));
        stage.sound("fireworks.launch", origin, 3, 1);
        rocket.velocity(new Vec(0, 0.09, 0));
        timeline.at(1, () -> rocket.status(17));
        timeline.at(2, rocket::remove);
        double start = random.nextDouble() * 360;
        for (int gift = 0; gift < GIFTS.size(); gift++) {
            gift(stage, timeline, origin, gift * GIFT_EVERY, start + gift * 45, GIFTS.get(gift));
        }
        timeline.lasts(LIFE + 1);
    }

    static Vec launch(double degrees) {
        return Kit.horizontal(degrees, HORIZONTAL_SPEED).add(0, VERTICAL_SPEED, 0);
    }

    private static void gift(Stage stage, Timeline timeline, Vec origin, int start, double degrees, String texture) {
        Body body = Body.living(origin, launch(degrees));
        Actor[] box = new Actor[1];
        float[] spin = {SPIN};
        float[] yaw = {0};
        timeline.at(start, () -> {
            box[0] = stage.armorStand(origin.add(0, -HEAD_ABOVE_BASE, 0), 0, new StandStyle(true, false, false, false, false));
            box[0].equipSkull(Slot.HELMET, texture);
            stage.sound("random.pop", origin, 1, POP_PITCH);
        });
        timeline.repeat(start + 1, LIFE, 1, offset -> {
            if (!body.landed()) {
                body.step(stage);
                yaw[0] -= spin[0];
                spin[0] *= (float) SPIN_DECAY;
            }
            box[0].teleport(body.position().add(0, -HEAD_ABOVE_BASE, 0), yaw[0], 0);
        });
        timeline.at(LIFE + 1, () -> box[0].remove());
    }
}
