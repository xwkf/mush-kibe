package br.com.muxe.minigame.bedwars.cosmetic.animation;

import br.com.muxe.core.bukkit.cosmetic.animation.Scene;
import br.com.muxe.core.bukkit.cosmetic.animation.SceneAnimation;
import br.com.muxe.core.bukkit.cosmetic.animation.Stage;
import br.com.muxe.core.bukkit.cosmetic.animation.Stage.Actor;
import br.com.muxe.core.bukkit.cosmetic.animation.Stage.Slot;
import br.com.muxe.core.bukkit.cosmetic.animation.Stage.StandStyle;
import br.com.muxe.core.bukkit.cosmetic.animation.Timeline;
import br.com.muxe.core.bukkit.cosmetic.animation.Vec;
import java.util.random.RandomGenerator;

public final class WrappedFinalKill implements SceneAnimation {

    static final String OPEN_GIFT = "3aa03f817341caa56fbfc6d3f2811adb989b611781162c12af4c58af18c53c72";
    static final String CLOSED_GIFT = "2b1ec7dc753061ca174424ea45cf9490b39cd5dcca477d138a603e6be755ec72";
    static final int[] FIREWORK_COLORS = {0x70CA55, 0xF7E872};
    static final double FIREWORK_ABOVE = 0.5;
    static final double GIFT_BELOW = 1.4375;
    static final double LOWER_GIFT_BELOW = 3.4375;
    static final int[] SINK = {16, 12, 10, 8, 6, 4, 3, 2, 2, 1};
    static final double STEP = 1 / 32.0;
    static final int CLOSE = 14;
    static final int END = 58;

    @Override
    public String id() {
        return "embrulhado";
    }

    @Override
    public void script(Scene scene, Stage stage, Timeline timeline, RandomGenerator random) {
        Vec origin = scene.origin();
        float yaw = VictimBody.towardKiller(scene);
        Kit.flash(stage, timeline, 0, origin.add(0, FIREWORK_ABOVE, 0), Kit.burst(Kit.BALL, false, true, FIREWORK_COLORS));
        Actor body = VictimBody.spawn(stage, scene, origin, yaw);
        Actor gift = gift(stage, origin.add(0, -GIFT_BELOW, 0), yaw);
        Actor lower = gift(stage, origin.add(0, -LOWER_GIFT_BELOW, 0), yaw);
        timeline.repeat(0, SINK.length - 1, 1, offset -> body.teleport(origin.add(0, -sunk(offset + 1), 0), yaw, 0));
        timeline.at(CLOSE, () -> {
            gift.equipSkull(Slot.HELMET, CLOSED_GIFT);
            stage.sound("mob.horse.armor", origin, 1, 1);
            body.remove();
        });
        timeline.at(END, () -> {
            stage.sound("random.pop", origin, 1, 1);
            gift.remove();
            lower.remove();
        });
        timeline.lasts(END + 1);
    }

    static double sunk(int ticks) {
        int total = 0;
        for (int tick = 0; tick < Math.min(ticks, SINK.length); tick++) {
            total += SINK[tick];
        }
        return total * STEP;
    }

    private static Actor gift(Stage stage, Vec position, float yaw) {
        Actor stand = stage.armorStand(position, yaw, new StandStyle(true, false, false, false, false));
        stand.equipSkull(Slot.HELMET, OPEN_GIFT);
        return stand;
    }
}
