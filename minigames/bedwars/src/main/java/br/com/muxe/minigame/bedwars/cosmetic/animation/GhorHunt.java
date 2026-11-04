package br.com.muxe.minigame.bedwars.cosmetic.animation;

import br.com.muxe.core.bukkit.cosmetic.animation.BedFrame;
import br.com.muxe.core.bukkit.cosmetic.animation.Stage;
import br.com.muxe.core.bukkit.cosmetic.animation.Stage.Actor;
import br.com.muxe.core.bukkit.cosmetic.animation.Stage.Slot;
import br.com.muxe.core.bukkit.cosmetic.animation.Stage.StandStyle;
import br.com.muxe.core.bukkit.cosmetic.animation.Vec;
import java.util.ArrayList;
import java.util.List;
import java.util.random.RandomGenerator;
import org.bukkit.Material;

final class GhorHunt {

    static final double REST_AHEAD = 5.0;
    static final double REST_HEIGHT = 1.8;
    static final double EMERGE_DISTANCE = 2.45;
    static final double EMERGE_KEEP = 0.73;
    static final double BOB = 0.15;
    static final double BOB_TICKS = 52;
    static final double FOLLOW = 0.3;
    static final double SHAKE = 0.06;
    static final double LIFT_STEP = 0.04;
    static final double PULL_START = 0.225;
    static final double PULL_GROWTH = 1.25;
    static final double PULL_RISE = 0.555;
    static final double RECOIL = 0.35;
    static final double LUNGE = 0.75;
    static final double LUNGE_DROP = 0.4;
    static final double SHUDDER = 4;
    static final double CHEW_YAW = 5;
    static final double CHEW_PITCH = 6;
    static final double RETREAT_START = 0.11;
    static final double RETREAT_GROWTH = 1.18;
    static final double REACH_LEFT = 0.4;
    static final double NEAR_GRIP = 0.91;
    static final double FAR_GRIP = 0.12;
    static final double GRIP_DEPTH = -1.37;
    static final double GRIP_SPREAD = 0.3;
    static final double PUMPKIN_DROP = -0.156;

    private final BedFrame bed;
    private final Vec watcher;
    private final Stage stage;
    private final RandomGenerator random;
    private final Kit.BedDisplay display;
    private final Actor pumpkin;
    private final Vec rest;
    private final Gaze start;
    private final List<GhorTentacle> tentacles = new ArrayList<>();
    private GhorEye eye;
    private Vec center;
    private Gaze gaze;
    private Vec offset = Vec.ZERO;
    private boolean eaten;

    GhorHunt(BedFrame bed, Vec watcher, Stage stage, RandomGenerator random) {
        this.bed = bed;
        this.watcher = watcher;
        this.stage = stage;
        this.random = random;
        this.display = Kit.bed(stage, bed);
        this.pumpkin = stage.armorStand(pumpkinAt(bed, Vec.ZERO), bed.yaw() + 180, StandStyle.HIDDEN_SMALL);
        pumpkin.equip(Slot.HELMET, Material.JACK_O_LANTERN, 0);
        this.rest = rest(bed);
        this.start = Gaze.toward(GhorEye.neck(rest), bed.middle());
        this.center = rest.subtract(start.ahead().multiply(EMERGE_DISTANCE));
        this.gaze = start;
    }

    static Vec rest(BedFrame bed) {
        return bed.at(REST_AHEAD, REST_HEIGHT, 0);
    }

    static double pull(int steps) {
        return PULL_START * (Math.pow(PULL_GROWTH, steps) - 1);
    }

    static Vec lift(BedFrame bed, int tick) {
        if (tick <= GhorAnimation.GRAB) {
            return Vec.ZERO;
        }
        if (tick <= GhorAnimation.PULL) {
            return new Vec(0, LIFT_STEP * (tick - GhorAnimation.GRAB), 0);
        }
        double distance = pull(Math.min(tick, GhorAnimation.EAT) - GhorAnimation.PULL);
        return bed.forward().multiply(distance).add(0, LIFT_STEP * (GhorAnimation.PULL - GhorAnimation.GRAB) + PULL_RISE * distance, 0);
    }

    static double bob(int tick) {
        return -BOB * Math.cos(2 * Math.PI * (tick - GhorAnimation.GROWL) / BOB_TICKS);
    }

    static double emerging(int tick) {
        return Math.pow(EMERGE_KEEP, Math.max(0, tick - GhorAnimation.APPEAR));
    }

    static Vec drift(BedFrame bed, int tick) {
        double recoil = 0;
        if (tick >= GhorAnimation.PULL && tick <= GhorAnimation.EAT) {
            recoil = RECOIL * (tick - GhorAnimation.PULL) / (GhorAnimation.EAT - GhorAnimation.PULL);
        } else if (tick > GhorAnimation.EAT && tick < GhorAnimation.SETTLED) {
            recoil = RECOIL * (1 - (tick - GhorAnimation.EAT) / (double) (GhorAnimation.SETTLED - GhorAnimation.EAT));
        }
        double lunge = tick > GhorAnimation.EAT && tick < GhorAnimation.CHEW
            ? Math.sin(Math.PI * (tick - GhorAnimation.EAT) / (GhorAnimation.CHEW - GhorAnimation.EAT))
            : 0;
        double hover = bob(tick > GhorAnimation.PULL && tick <= GhorAnimation.EAT ? GhorAnimation.PULL : tick);
        return bed.forward().multiply(recoil - LUNGE * lunge).add(0, hover - LUNGE_DROP * lunge, 0);
    }

    static double retreat(int tick) {
        if (tick <= GhorAnimation.GROAN) {
            return 0;
        }
        return RETREAT_START * (Math.pow(RETREAT_GROWTH, tick - GhorAnimation.GROAN) - 1) / (RETREAT_GROWTH - 1);
    }

    static double reach(int tick, int launch) {
        if (tick < launch || tick > GhorAnimation.EAT) {
            return 0;
        }
        return 1 - Math.pow(REACH_LEFT, tick - launch + 1);
    }

    static Vec grip(BedFrame bed, int up, int side) {
        return bed.at(up < 0 ? NEAR_GRIP : FAR_GRIP, GRIP_DEPTH, -side * GRIP_SPREAD);
    }

    static boolean watchesBed(int tick) {
        return tick <= GhorAnimation.STARE || tick > GhorAnimation.GROWL && tick <= GhorAnimation.EAT;
    }

    static Vec pumpkinAt(BedFrame bed, Vec offset) {
        return bed.head().add(0, PUMPKIN_DROP, 0).add(offset);
    }

    void appear() {
        eye = GhorEye.spawn(stage, center, gaze);
        for (int[] limb : GhorAnimation.LIMBS) {
            tentacles.add(GhorTentacle.spawn(stage, GhorTentacle.root(center, gaze, limb[0], limb[1]), limb[0], limb[1]));
        }
    }

    void step(int tick) {
        if (!eaten) {
            offset = lift(bed, tick);
            if (tick > GhorAnimation.GRAB && tick <= GhorAnimation.PULL) {
                offset = offset.add(Kit.between(random, -SHAKE, SHAKE), 0, Kit.between(random, -SHAKE, SHAKE));
            }
            display.move(bed, offset);
            pumpkin.teleport(pumpkinAt(bed, offset), bed.yaw() + 180, 0);
        }
        Vec base = rest.add(drift(bed, tick)).subtract(start.ahead().multiply(EMERGE_DISTANCE * emerging(tick)));
        Vec target = watchesBed(tick) ? bedMiddle() : watcher;
        gaze = gaze.follow(Gaze.toward(GhorEye.neck(base), target), FOLLOW);
        center = base.subtract(gaze.ahead().multiply(retreat(tick)));
        Gaze shown = gaze;
        if (tick > GhorAnimation.GROWL && tick <= GhorAnimation.SHUDDER) {
            shown = gaze.turned(Kit.between(random, -SHUDDER, SHUDDER), 0);
        } else if (tick > GhorAnimation.EAT && tick <= GhorAnimation.CHEW) {
            shown = gaze.turned(Kit.between(random, -CHEW_YAW, CHEW_YAW), Kit.between(random, -CHEW_PITCH, CHEW_PITCH));
        }
        eye.place(center, shown);
        for (int index = 0; index < tentacles.size(); index++) {
            GhorTentacle tentacle = tentacles.get(index);
            Vec root = GhorTentacle.root(center, shown, tentacle.up(), tentacle.side());
            Vec tip = root.add(grip(bed, tentacle.up(), tentacle.side()).add(offset).subtract(root).multiply(reach(tick, GhorAnimation.LAUNCHES[index])));
            tentacle.place(GhorTentacle.points(root, tip, tentacle.up(), tentacle.side(), tick));
        }
    }

    void show(GhorEye.Face face) {
        eye.show(face);
    }

    void bleed(int tick, double chance, double[] color) {
        for (int index = 0; index < tentacles.size(); index++) {
            if (reach(tick, GhorAnimation.LAUNCHES[index]) > 0 && random.nextDouble() < chance) {
                Vec at = tentacles.get(index).point(1 + random.nextInt(GhorTentacle.SEGMENTS - 1));
                Kit.dust(stage, at.add(0, GhorTentacle.SMALL_LIFT + 0.75, 0), color[0], color[1], color[2]);
            }
        }
    }

    void swallow() {
        eaten = true;
        display.remove();
        pumpkin.remove();
    }

    void leave() {
        eye.remove();
        tentacles.forEach(GhorTentacle::remove);
    }

    Vec bedMiddle() {
        return bed.middle().add(offset);
    }

    Vec center() {
        return center;
    }

    Gaze gaze() {
        return gaze;
    }

    Vec middle() {
        return GhorEye.middle(center, gaze);
    }

    Vec mouth() {
        return GhorEye.mouth(center, gaze);
    }

    Vec crown() {
        return GhorEye.crown(center, gaze);
    }
}
