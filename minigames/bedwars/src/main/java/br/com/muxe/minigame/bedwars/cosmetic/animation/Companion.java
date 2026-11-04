package br.com.muxe.minigame.bedwars.cosmetic.animation;

import br.com.muxe.core.bukkit.cosmetic.animation.Stage;
import br.com.muxe.core.bukkit.cosmetic.animation.Stage.Actor;
import br.com.muxe.core.bukkit.cosmetic.animation.Stage.Pose;
import br.com.muxe.core.bukkit.cosmetic.animation.Stage.Slot;
import br.com.muxe.core.bukkit.cosmetic.animation.Stage.StandStyle;
import br.com.muxe.core.bukkit.cosmetic.animation.Vec;
import java.util.List;
import org.bukkit.Material;

final class Companion {

    static final List<Place> PLACES = List.of(new Place(4, -4), new Place(4, 4), new Place(3, -7), new Place(3, 7));
    static final int FOLLOW_START = 7;
    static final double FOLLOW_GAIN = 0.18;
    static final double TOP_SPEED = 1.0;
    static final double EASING = 0.3;
    static final double TURN_EASING = 0.35;
    static final double TURN_SPEED = 0.05;
    static final double HAT_UP = 0.3125;
    static final float SIDEWAYS = 90;
    static final Pose UPSIDE_DOWN = Pose.STANDING.withHead(new Vec(180, 90, 0));
    static final StandStyle UNSEEN = new StandStyle(true, false, false, false, false);
    static final double PUFF_FORWARD = -0.4;
    static final double PUFF_UP = 1.25;
    static final Vec PUFF_SPREAD = new Vec(1, 0.7, 1);

    private final Actor seat;
    private final Actor witch;
    private final Actor hat;
    private final Broom broom;
    private Vec position;
    private Vec velocity = BroomFlight.START_VELOCITY;
    private double yaw;
    private int age;

    private Companion(Actor seat, Actor witch, Actor hat, Broom broom, Vec position, float yaw) {
        this.seat = seat;
        this.witch = witch;
        this.hat = hat;
        this.broom = broom;
        this.position = position;
        this.yaw = yaw;
    }

    static Companion spawn(Stage stage, Vec position, float yaw) {
        Actor seat = stage.armorStand(position, yaw, StandStyle.HIDDEN);
        Actor witch = stage.player(position, yaw);
        witch.equip(Slot.HAND, Material.STICK, 0);
        witch.ride(seat);
        Actor hat = stage.armorStand(position.add(0, HAT_UP, 0), yaw + SIDEWAYS, UNSEEN);
        hat.equip(Slot.HELMET, Material.HOPPER, 0);
        hat.pose(UPSIDE_DOWN);
        stage.particle("CLOUD", Broom.spot(position, yaw, PUFF_FORWARD, PUFF_UP), 25, PUFF_SPREAD, 0.1);
        return new Companion(seat, witch, hat, Broom.build(stage, position, yaw), position, yaw);
    }

    static Vec place(Vec leader, double leaderYaw, Place place) {
        return leader.add(BroomFlight.forward(leaderYaw).multiply(place.ahead())).add(BroomFlight.right(leaderYaw).multiply(place.right()));
    }

    static Vec chase(Vec from, Vec place) {
        Vec wanted = place.subtract(from).multiply(FOLLOW_GAIN);
        return wanted.length() > TOP_SPEED ? wanted.normalize().multiply(TOP_SPEED) : wanted;
    }

    void follow(Vec place) {
        age++;
        Vec wanted = age < FOLLOW_START ? Vec.ZERO : chase(position, place);
        velocity = velocity.add(wanted.subtract(velocity).multiply(EASING));
        position = position.add(velocity);
        if (Math.hypot(velocity.x(), velocity.z()) > TURN_SPEED) {
            yaw += TURN_EASING * Flight.wrap(Vec.ZERO.yawTowards(velocity) - yaw);
        }
        float facing = (float) yaw;
        seat.teleport(position, facing, 0);
        witch.teleport(position, facing, 0);
        hat.teleport(position.add(0, HAT_UP, 0), facing + SIDEWAYS, 0);
        broom.place(position, facing);
    }

    void remove() {
        broom.remove();
        hat.remove();
        witch.remove();
        seat.remove();
    }

    record Place(double ahead, double right) {
    }
}
