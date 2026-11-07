package br.com.muxe.minigame.bedwars.cosmetic.animation;

import br.com.muxe.core.bukkit.cosmetic.animation.Stage;
import br.com.muxe.core.bukkit.cosmetic.animation.Stage.Actor;
import br.com.muxe.core.bukkit.cosmetic.animation.Stage.Pose;
import br.com.muxe.core.bukkit.cosmetic.animation.Stage.Slot;
import br.com.muxe.core.bukkit.cosmetic.animation.Stage.StandStyle;
import br.com.muxe.core.bukkit.cosmetic.animation.Vec;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.random.RandomGenerator;
import org.bukkit.Material;

final class KrakenTentacle {

    static final int SEGMENTS = 7;
    static final int SMALL_FROM = 5;
    static final int[] WOOL = {10, 10, 2, 10, 2, 2, 6};
    static final double[] LINKS = {0.6, 0.56, 0.56, 0.52, 0.56, 0.38};
    static final double LIFT = 0.25;
    static final double BURIED = -0.75;
    static final double NECK = 1.4375;
    static final double SMALL_NECK = 0.756;
    static final double CROWN = 0.25;
    static final double SMALL_CROWN = 0.175;
    static final double DEPTH = 3;
    static final int RISE = 12;
    static final double[] BEND = {7, 14, 27, 38, 45, 58};
    static final double[] CURL = {6, 10, 12, 14, 15, 20};
    static final double[] SWAY = {0, 15, 30, 32, 34, 35};
    static final double CURL_PERIOD = 25;
    static final double SWAY_PERIOD = 42;
    static final double LAG = 3;
    static final double[] GRAB_BEND = {16, 58, 141, 114, 99, 107};
    static final double[] GRAB_TURN = {-3, 0, 0, -70, -25, 20};
    static final double[] SQUEEZE_BEND = {0, -4.5, 0, 9, 0, 0};
    static final int[] STRIKE_DELAY = {6, 6, 6, 1, 0, 0};
    static final int[] STRIKE_TICKS = {10, 12, 12, 16, 8, 6};
    static final double[] WHIP = {0, 0, 0, 0, 20, 55};
    static final int WHIP_FROM = 2;
    static final int WHIP_TICKS = 16;
    static final double SQUEEZE_SINK = 0.18;
    static final int SQUEEZE_TICKS = 3;
    static final double PLUNGE_PULL = 0.028;
    private static final Vec UP = new Vec(0, 1, 0);
    private static final double TINY = 1.0E-6;

    private final Vec base;
    private final Vec ahead;
    private final Vec side;
    private final float yaw;
    private final int born;
    private final double phase;
    private final List<Actor> segments;
    private List<Vec> cubes;

    private KrakenTentacle(Vec base, Vec aim, int born, double phase, List<Actor> segments) {
        this.base = base;
        this.ahead = new Vec(aim.x() - base.x(), 0, aim.z() - base.z()).normalize();
        this.side = new Vec(-ahead.z(), 0, ahead.x());
        this.yaw = Vec.ZERO.yawTowards(ahead);
        this.born = born;
        this.phase = phase;
        this.segments = segments;
    }

    static KrakenTentacle spawn(Stage stage, Vec base, Vec aim, int born, double phase) {
        List<Actor> segments = new ArrayList<>();
        KrakenTentacle tentacle = new KrakenTentacle(base, aim, born, phase, segments);
        List<Vec> cubes = tentacle.shape(born);
        for (int segment = 0; segment < SEGMENTS; segment++) {
            Actor stand = stage.armorStand(tentacle.stand(cubes, segment), tentacle.yaw, segment < SMALL_FROM ? StandStyle.HIDDEN : StandStyle.HIDDEN_SMALL);
            stand.equip(Slot.HELMET, Material.WOOL, WOOL[segment]);
            segments.add(stand);
        }
        tentacle.place(cubes);
        return tentacle;
    }

    void move(int tick) {
        place(shape(tick));
    }

    void remove() {
        segments.forEach(Actor::remove);
    }

    Vec cube(int segment) {
        return cubes.get(segment);
    }

    Optional<Vec> drip(RandomGenerator random) {
        List<Vec> wet = cubes.stream().filter(cube -> cube.y() > base.y()).toList();
        return wet.isEmpty() ? Optional.empty() : Optional.of(wet.get(random.nextInt(wet.size())));
    }

    List<Vec> shape(int tick) {
        double depth = depth(tick);
        List<Vec> shape = new ArrayList<>();
        Vec cube = base.add(0, LIFT - depth, 0);
        boolean buried = true;
        for (int link = 0; link <= LINKS.length; link++) {
            buried = buried && cube.y() < base.y();
            shape.add(buried ? new Vec(cube.x(), Math.max(cube.y(), base.y() + BURIED), cube.z()) : cube);
            if (link < LINKS.length) {
                Vec heading = buried ? UP : direction(bend(link, tick), turn(link, tick));
                cube = cube.add(heading.multiply(LINKS[link]));
            }
        }
        return List.copyOf(shape);
    }

    double depth(int tick) {
        double age = Math.min(tick - born + 1, RISE);
        double rising = DEPTH * Math.pow(1 - Math.max(age, 0) / RISE, 3);
        double plunge = tick >= KrakenAnimation.PLUNGE ? PLUNGE_PULL * Math.pow(tick - KrakenAnimation.PLUNGE + 1, 2) : 0;
        return rising + SQUEEZE_SINK * squeezed(tick) + plunge;
    }

    double bend(int link, int tick) {
        double idle = BEND[link] + CURL[link] * Math.sin(2 * Math.PI * (tick - born - LAG * link) / CURL_PERIOD + phase);
        return mix(idle, GRAB_BEND[link] + WHIP[link] * whip(tick) + SQUEEZE_BEND[link] * squeezed(tick), grip(link, tick));
    }

    double turn(int link, int tick) {
        double idle = SWAY[link] * Math.sin(2 * Math.PI * (tick - born - LAG * link) / SWAY_PERIOD + phase);
        return mix(idle, GRAB_TURN[link], grip(link, tick));
    }

    static double grip(int link, int tick) {
        double share = Math.clamp((tick - KrakenAnimation.STRIKE - STRIKE_DELAY[link]) / (double) STRIKE_TICKS[link], 0, 1);
        return share * share * (3 - 2 * share);
    }

    static double whip(int tick) {
        return Math.sin(Math.PI * Math.clamp((tick - KrakenAnimation.STRIKE - WHIP_FROM) / (double) WHIP_TICKS, 0, 1));
    }

    static double squeezed(int tick) {
        double squeezed = 0;
        for (int squeeze : KrakenAnimation.SQUEEZES) {
            squeezed += Math.clamp((tick - squeeze) / (double) SQUEEZE_TICKS, 0, 1);
        }
        return squeezed;
    }

    private Vec direction(double bend, double turn) {
        double tilt = Math.toRadians(bend);
        double around = Math.toRadians(turn);
        Vec outward = ahead.multiply(Math.cos(around)).add(side.multiply(Math.sin(around)));
        return UP.multiply(Math.cos(tilt)).add(outward.multiply(Math.sin(tilt)));
    }

    private void place(List<Vec> cubes) {
        this.cubes = cubes;
        for (int segment = 0; segment < SEGMENTS; segment++) {
            Vec along = along(cubes, segment);
            float facing = Math.hypot(along.x(), along.z()) > TINY ? Vec.ZERO.yawTowards(along) : yaw;
            float tilt = (float) Math.toDegrees(Math.acos(Math.clamp(along.y(), -1, 1)));
            Actor stand = segments.get(segment);
            stand.teleport(stand(cubes, segment), facing, 0);
            stand.pose(Pose.STANDING.withHead(new Vec(tilt, 0, 0)));
        }
    }

    private Vec stand(List<Vec> cubes, int segment) {
        boolean small = segment >= SMALL_FROM;
        return cubes.get(segment).subtract(along(cubes, segment).multiply(small ? SMALL_CROWN : CROWN)).add(0, -(small ? SMALL_NECK : NECK), 0);
    }

    private static Vec along(List<Vec> cubes, int segment) {
        int from = segment < SEGMENTS - 1 ? segment : segment - 1;
        Vec span = cubes.get(from + 1).subtract(cubes.get(from));
        return span.length() > TINY ? span.normalize() : UP;
    }

    private static double mix(double from, double to, double share) {
        return from + (to - from) * share;
    }
}
