package br.com.muxe.minigame.bedwars.cosmetic.animation;

import br.com.muxe.core.bukkit.cosmetic.animation.BedAnimation;
import br.com.muxe.core.bukkit.cosmetic.animation.BedFrame;
import br.com.muxe.core.bukkit.cosmetic.animation.Stage;
import br.com.muxe.core.bukkit.cosmetic.animation.Timeline;
import br.com.muxe.core.bukkit.cosmetic.animation.Vec;
import br.com.muxe.minigame.api.cosmetic.animation.Kit;
import br.com.muxe.minigame.bedwars.cosmetic.animation.GhorEye.Face;
import java.util.List;
import java.util.random.RandomGenerator;
import org.bukkit.Material;

public final class GhorAnimation implements BedAnimation {

    static final int APPEAR = 5;
    static final int EMERGE = 6;
    static final int DRIP_FROM = 8;
    static final int DRIP_EVERY = 3;
    static final int STARE = 21;
    static final int GROWL = 35;
    static final int SHUDDER = 44;
    static final int WINGS = 41;
    static final int[] LAUNCHES = {42, 44, 45, 46};
    static final int[][] LIMBS = {{-1, 1}, {-1, -1}, {1, 1}, {1, -1}};
    static final int GRAB = 50;
    static final int[] CREAKS = {51, 55, 59, 64};
    static final float[] CREAK_VOLUME = {0.6f, 0.6f, 0.6f, 1};
    static final float[] CREAK_PITCH = {0.85f, 0.7f, 0.85f, 0.6f};
    static final int SPLINTERS = 3;
    static final int GRUNT = 64;
    static final int PULL = 65;
    static final int EAT = 78;
    static final int CHEW = 86;
    static final int SETTLED = 96;
    static final int BURP = 92;
    static final int HISS = 98;
    static final int REOPEN = 101;
    static final int GROAN = 108;
    static final int LEAVE = 120;
    static final int END = 126;
    static final double RIFT_AHEAD = 5.57;
    static final double RIFT_HEIGHT = 3.9;
    static final double EYE_LEVEL = 1.62;
    static final int BLOOD = 25;
    static final double BLOOD_CHANCE = 0.35;
    static final double[] RED = {0.478, 0.039, 0.039};
    static final List<Expression> EXPRESSIONS = List.of(
        new Expression(STARE, Face.GLARE),
        new Expression(GROWL, Face.HUNGRY),
        new Expression(BURP, Face.GLARE),
        new Expression(HISS, Face.CALM),
        new Expression(REOPEN, Face.GLARE),
        new Expression(GROAN, Face.CALM));

    @Override
    public String id() {
        return "ghor";
    }

    @Override
    public void script(BedFrame bed, Vec breaker, Stage stage, Timeline timeline, RandomGenerator random) {
        GhorRift rift = new GhorRift(bed.at(RIFT_AHEAD, RIFT_HEIGHT, 0), bed.middle().subtract(bed.at(RIFT_AHEAD, RIFT_HEIGHT, 0)), bed.right());
        Vec portal = rift.center();
        GhorHunt hunt = new GhorHunt(bed, breaker.add(0, EYE_LEVEL, 0), stage, random);
        timeline.at(0, () -> {
            stage.sound("mob.endermen.portal", portal, 2, 0.5f);
            stage.sound("portal.trigger", portal, 0.4f, 1.6f);
        });
        timeline.repeat(0, END - 1, 1, tick -> rift.draw(stage, GhorRift.radius(tick, LEAVE), random));
        timeline.at(APPEAR, hunt::appear);
        timeline.repeat(EMERGE, LEAVE - 1, 1, offset -> hunt.step(EMERGE + offset));
        timeline.at(EMERGE, () -> {
            stage.sound("mob.zombie.unfect", portal, 2, 0.5f);
            stage.particle("PORTAL", portal, 40, new Vec(0.5, 0.5, 0.5), 1);
            stage.particle("SPELL_WITCH", portal, 15, new Vec(0.6, 0.6, 0.6), 0.1);
        });
        timeline.repeat(DRIP_FROM, LEAVE - 1, DRIP_EVERY, tick -> drip(stage, hunt, random));
        for (Expression expression : EXPRESSIONS) {
            timeline.at(expression.tick(), () -> hunt.show(expression.face()));
        }
        timeline.at(STARE, () -> {
            stage.sound("mob.slime.attack", hunt.middle(), 2, 0.5f);
            stage.sound("mob.endermen.stare", hunt.middle(), 1, 1.2f);
        });
        timeline.at(GROWL, () -> {
            stage.sound("mob.enderdragon.growl", hunt.middle(), 2, 0.8f);
            Vec spray = hunt.middle().add(hunt.gaze().ahead().multiply(0.9)).add(0, 0.9, 0);
            for (int drop = 0; drop < BLOOD; drop++) {
                Vec at = spray.add(Kit.between(random, -1, 1), Kit.between(random, -1, 1), Kit.between(random, -1, 1));
                Kit.dust(stage, at, RED[0], RED[1], RED[2]);
            }
        });
        timeline.at(WINGS, () -> stage.sound("mob.enderdragon.wings", hunt.middle(), 2, 1.6f));
        timeline.repeat(LAUNCHES[0], EAT, 1, tick -> hunt.bleed(LAUNCHES[0] + tick, BLOOD_CHANCE, RED));
        timeline.at(GRAB, () -> {
            stage.sound("mob.slime.attack", hunt.bedMiddle().add(0, 0.25, 0), 1, 0.5f);
            stage.particle("BLOCK_CRACK", hunt.bedMiddle().add(0, 0.28, 0), 30, new Vec(0.5, 0.2, 0.5), 0.2, Kit.blockId(Material.REDSTONE_BLOCK, 0));
        });
        for (int creak = 0; creak < CREAKS.length; creak++) {
            int index = creak;
            timeline.at(CREAKS[index], () -> {
                stage.sound("mob.zombie.wood", hunt.bedMiddle(), CREAK_VOLUME[index], CREAK_PITCH[index]);
                if (index < SPLINTERS) {
                    stage.particle("BLOCK_CRACK", hunt.bedMiddle().add(0, 0.36, 0), 8, new Vec(0.4, 0.1, 0.4), 0.1, Kit.blockId(Material.WOOD, 0));
                }
            });
        }
        timeline.at(GRUNT, () -> stage.sound("mob.enderdragon.growl", hunt.middle(), 1, 1.2f));
        timeline.at(EAT, () -> {
            hunt.swallow();
            Vec mouth = hunt.mouth();
            stage.sound("mob.zombie.woodbreak", mouth, 2, 0.8f);
            stage.sound("random.eat", mouth, 2, 0.6f);
            stage.particle("BLOCK_CRACK", mouth, 30, new Vec(0.4, 0.4, 0.4), 0.25, Kit.blockId(Material.WOOD, 0));
            stage.particle("BLOCK_CRACK", mouth, 40, new Vec(0.5, 0.5, 0.5), 0.3, Kit.blockId(Material.REDSTONE_BLOCK, 0));
            stage.particle("SMOKE_LARGE", mouth, 6, new Vec(0.4, 0.4, 0.4), 0.05);
        });
        timeline.at(BURP, () -> {
            stage.sound("random.burp", hunt.crown(), 2, 0.6f);
            stage.particle("SMOKE_NORMAL", hunt.crown(), 15, new Vec(0.3, 0.3, 0.3), 0.05);
        });
        timeline.at(HISS, () -> stage.sound("mob.slime.attack", hunt.crown(), 0.6f, 1.5f));
        timeline.at(GROAN, () -> stage.sound("mob.zombie.unfect", hunt.middle(), 2, 0.7f));
        timeline.at(LEAVE, () -> {
            hunt.leave();
            stage.sound("mob.endermen.portal", portal, 2, 0.6f);
            stage.particle("PORTAL", portal, 50, new Vec(0.3, 0.3, 0.3), 1.5);
        });
        timeline.at(END, () -> {
            stage.sound("random.fizz", portal, 2, 0.5f);
            stage.particle("FIREWORKS_SPARK", portal, 20, new Vec(0.1, 0.1, 0.1), 0.15);
            stage.particle("SMOKE_LARGE", portal, 8, new Vec(0.4, 0.4, 0.4), 0.02);
        });
        timeline.lasts(END);
    }

    record Expression(int tick, Face face) {
    }

    private static void drip(Stage stage, GhorHunt hunt, RandomGenerator random) {
        Gaze gaze = hunt.gaze();
        Vec flat = new Vec(gaze.ahead().x(), 0, gaze.ahead().z()).normalize();
        Vec at = GhorEye.neck(hunt.center())
            .add(flat.multiply(Kit.between(random, 0, 0.9)))
            .add(gaze.side().multiply(Kit.between(random, -0.6, 0.6)))
            .add(0, -Kit.between(random, 0.6, 0.85), 0);
        stage.particle("DRIP_LAVA", at, 1, Vec.ZERO, 0);
    }
}
