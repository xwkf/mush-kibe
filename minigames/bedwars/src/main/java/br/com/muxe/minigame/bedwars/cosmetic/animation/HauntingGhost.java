package br.com.muxe.minigame.bedwars.cosmetic.animation;

import br.com.muxe.core.bukkit.cosmetic.animation.BedFrame;
import br.com.muxe.core.bukkit.cosmetic.animation.Stage.Actor;
import br.com.muxe.core.bukkit.cosmetic.animation.Stage.Slot;
import br.com.muxe.core.bukkit.cosmetic.animation.Stage.StandStyle;
import br.com.muxe.core.bukkit.cosmetic.animation.Stage;
import br.com.muxe.core.bukkit.cosmetic.animation.Vec;
import br.com.muxe.minigame.api.cosmetic.animation.Kit;
import br.com.muxe.minigame.api.cosmetic.animation.SoundPitch;
import java.util.random.RandomGenerator;

final class HauntingGhost {

    static final String SKIN = "c11de96c3e7b52fa5e4c794b4be14a05fc67694ca5eff9c9eb8baac672d21c09";
    static final double HEAD = 1.74;
    static final double MIN_HEIGHT = 0.72;
    static final double MAX_HEIGHT = 1.66;
    static final double MIN_REACH = 0.5;
    static final double MAX_REACH = 2.0;
    static final double SPEED = 0.12;
    static final double SLOWING = 0.2;
    static final double STEERING = 0.25;
    static final double TURNING = 0.6;
    static final int RETARGET = 10;
    static final int TRAIL = 5;

    private final Stage stage;
    private final BedFrame bed;
    private final RandomGenerator random;
    private final Actor stand;
    private Vec position;
    private Vec velocity = Vec.ZERO;
    private Vec target;
    private float yaw;

    private HauntingGhost(Stage stage, BedFrame bed, RandomGenerator random, Vec position) {
        this.stage = stage;
        this.bed = bed;
        this.random = random;
        this.position = position;
        this.target = pickTarget();
        this.yaw = position.yawTowards(target);
        this.stand = stage.armorStand(position, yaw, StandStyle.HIDDEN);
        stand.equipSkull(Slot.HELMET, SKIN);
    }

    static HauntingGhost rise(Stage stage, BedFrame bed, RandomGenerator random) {
        Vec spot = bed.at(Kit.between(random, 0, 1.4), Kit.between(random, 0.78, 1.06), Kit.between(random, -0.7, 0.7));
        HauntingGhost ghost = new HauntingGhost(stage, bed, random, spot);
        Vec head = ghost.head();
        stage.particle("CLOUD", head, 15, new Vec(0.01, 0.01, 0.01), 0.1);
        stage.sound("mob.bat.death", head, 1, SoundPitch.of(31));
        stage.sound("mob.bat.loop", head, 1, SoundPitch.of(47));
        return ghost;
    }

    void drift(int age) {
        if (age % RETARGET == 0) {
            target = pickTarget();
        }
        velocity = steer(velocity, target.subtract(position));
        position = position.add(velocity);
        yaw = turn(yaw, position.yawTowards(target));
        stand.teleport(position, yaw, 0);
        if (age % TRAIL == 0) {
            stage.particle("CLOUD", head(), 1, new Vec(0.1, 0.1, 0.1), 0.01);
        }
    }

    void vanish(float pitch) {
        Vec head = head();
        stand.remove();
        stage.particle("CLOUD", head, 15, new Vec(0.01, 0.01, 0.01), 0.1);
        stage.sound("random.fizz", head, 2, pitch);
    }

    Vec position() {
        return position;
    }

    static Vec steer(Vec velocity, Vec gap) {
        double distance = gap.length();
        Vec desired = distance == 0 ? Vec.ZERO : gap.multiply(Math.min(SPEED, distance * SLOWING) / distance);
        return velocity.add(desired.subtract(velocity).multiply(STEERING));
    }

    static float turn(float from, float to) {
        double difference = ((to - from) % 360 + 540) % 360 - 180;
        return (float) (from + difference * TURNING);
    }

    private Vec head() {
        return position.add(0, HEAD, 0);
    }

    private Vec pickTarget() {
        Vec around = Kit.horizontal(random.nextDouble() * 360, Kit.between(random, MIN_REACH, MAX_REACH));
        return bed.middle().add(around).add(0, Kit.between(random, MIN_HEIGHT, MAX_HEIGHT), 0);
    }
}
