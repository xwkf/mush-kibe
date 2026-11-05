package br.com.muxe.minigame.bedwars.cosmetic.animation;

import br.com.muxe.core.bukkit.cosmetic.animation.BedAnimation;
import br.com.muxe.core.bukkit.cosmetic.animation.BedFrame;
import br.com.muxe.core.bukkit.cosmetic.animation.Stage;
import br.com.muxe.core.bukkit.cosmetic.animation.Stage.Actor;
import br.com.muxe.core.bukkit.cosmetic.animation.Timeline;
import br.com.muxe.core.bukkit.cosmetic.animation.Vec;
import java.util.ArrayList;
import java.util.List;
import java.util.random.RandomGenerator;
import org.bukkit.Material;

public final class HauntedAnimation implements BedAnimation {

    static final int BONES = 36;
    static final double BONE_LIFT = 0.25;
    static final double BONE_JITTER = 20;
    static final double BONE_MIN_SPEED = 0.12;
    static final double BONE_MAX_SPEED = 0.29;
    static final double BONE_MIN_UP = 0.32;
    static final double BONE_MAX_UP = 0.5;
    static final Material[] GRAVE = {Material.QUARTZ_BLOCK, Material.WOOL, Material.STONE};
    static final int[] GRAVE_DATA = {0, 0, 3};
    static final int[] GHOSTS_AT = {0, 4, 9};
    static final int[] VANISH_AT = {49, 55, 58};
    static final float[] VANISH_PITCH = {SoundPitch.of(94), SoundPitch.of(126), SoundPitch.of(94)};
    static final double ITEM_GRAVITY = 0.04;
    static final double ITEM_DRAG = 0.98;
    static final int END = 99;

    @Override
    public String id() {
        return "assombrado";
    }

    @Override
    public void script(BedFrame bed, Vec breaker, Stage stage, Timeline timeline, RandomGenerator random) {
        Vec start = bed.middle().add(0, BONE_LIFT, 0);
        List<Actor> bones = new ArrayList<>();
        List<Bone> flights = new ArrayList<>();
        timeline.at(0, () -> {
            for (int index = 0; index < BONES; index++) {
                Vec velocity = boneVelocity(index, random);
                Actor bone = stage.item(start, Material.BONE, 0);
                bone.velocity(velocity);
                bones.add(bone);
                flights.add(new Bone(start, velocity));
            }
        });
        timeline.repeat(1, END - 1, 1, tick -> {
            for (Bone flight : flights) {
                if (flight.fall(stage)) {
                    grave(stage, flight, random);
                }
            }
        });
        timeline.at(END, () -> bones.forEach(Actor::remove));
        for (int index = 0; index < GHOSTS_AT.length; index++) {
            int ghost = index;
            HauntingGhost[] spirit = new HauntingGhost[1];
            timeline.at(GHOSTS_AT[ghost], () -> spirit[0] = HauntingGhost.rise(stage, bed, random));
            timeline.repeat(GHOSTS_AT[ghost] + 1, VANISH_AT[ghost] - 1, 1, tick -> spirit[0].drift(tick + 1));
            timeline.at(VANISH_AT[ghost], () -> spirit[0].vanish(VANISH_PITCH[ghost]));
        }
    }

    static Vec boneVelocity(int index, RandomGenerator random) {
        double degrees = index * 360.0 / BONES + Kit.between(random, -BONE_JITTER, BONE_JITTER);
        return Kit.horizontal(degrees, Kit.between(random, BONE_MIN_SPEED, BONE_MAX_SPEED))
            .add(0, Kit.between(random, BONE_MIN_UP, BONE_MAX_UP), 0);
    }

    private static void grave(Stage stage, Bone bone, RandomGenerator random) {
        int kind = random.nextInt(GRAVE.length);
        int[] block = bone.grave();
        stage.block(block[0], block[1], block[2], GRAVE[kind], GRAVE_DATA[kind]);
        stage.sound("mob.chicken.plop", new Vec(bone.position().x(), block[1] + 1, bone.position().z()), 1, 1);
    }

    static final class Bone {

        private Vec position;
        private Vec velocity;
        private boolean landed;

        Bone(Vec position, Vec velocity) {
            this.position = position;
            this.velocity = velocity;
        }

        boolean fall(Stage stage) {
            if (landed) {
                return false;
            }
            position = position.add(velocity);
            int[] below = grave();
            if (velocity.y() < 0 && stage.solid(below[0], below[1], below[2])) {
                landed = true;
                return true;
            }
            velocity = new Vec(velocity.x() * ITEM_DRAG, (velocity.y() - ITEM_GRAVITY) * ITEM_DRAG, velocity.z() * ITEM_DRAG);
            return false;
        }

        int[] grave() {
            return new int[] {(int) Math.floor(position.x()), (int) Math.floor(position.y() - 1), (int) Math.floor(position.z())};
        }

        Vec position() {
            return position;
        }
    }
}
