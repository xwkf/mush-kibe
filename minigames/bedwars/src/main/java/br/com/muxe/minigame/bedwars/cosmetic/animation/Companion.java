package br.com.muxe.minigame.bedwars.cosmetic.animation;

import br.com.muxe.core.bukkit.cosmetic.animation.SignedSkin;
import br.com.muxe.core.bukkit.cosmetic.animation.Stage.Actor;
import br.com.muxe.core.bukkit.cosmetic.animation.Stage.Pose;
import br.com.muxe.core.bukkit.cosmetic.animation.Stage.Slot;
import br.com.muxe.core.bukkit.cosmetic.animation.Stage.StandStyle;
import br.com.muxe.core.bukkit.cosmetic.animation.Stage;
import br.com.muxe.core.bukkit.cosmetic.animation.Vec;
import java.util.List;
import org.bukkit.Material;

final class Companion {

    static final List<Place> PLACES = List.of(new Place(4, -4), new Place(4, 4), new Place(3, -7), new Place(3, 7));
    static final double FOLLOW_DISTANCE = 4;
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
    private final BroomFlight flight;

    private Companion(Actor seat, Actor witch, Actor hat, Broom broom, BroomFlight flight) {
        this.seat = seat;
        this.witch = witch;
        this.hat = hat;
        this.broom = broom;
        this.flight = flight;
    }

    static Companion spawn(Stage stage, Vec position, float yaw, SignedSkin skin, int phase) {
        Actor seat = stage.armorStand(position, yaw, StandStyle.HIDDEN);
        Actor witch = stage.player(position, yaw, skin);
        witch.equip(Slot.HAND, Material.STICK, 0);
        witch.ride(seat);
        Actor hat = stage.armorStand(position.add(0, HAT_UP, 0), yaw + SIDEWAYS, UNSEEN);
        hat.equip(Slot.HELMET, Material.HOPPER, 0);
        hat.pose(UPSIDE_DOWN);
        stage.particle("CLOUD", Broom.spot(position, yaw, PUFF_FORWARD, PUFF_UP), 25, PUFF_SPREAD, 0.1);
        BroomFlight flight = new BroomFlight(position, yaw);
        for (int tick = 0; tick < phase; tick++) {
            flight.hover();
        }
        return new Companion(seat, witch, hat, Broom.build(stage, position, yaw), flight);
    }

    static Vec place(Vec leader, double heading, Place place) {
        return leader.add(Broom.forward(heading).multiply(place.ahead())).add(Broom.right(heading).multiply(place.right()));
    }

    static boolean chasing(Vec from, Vec place) {
        return place.distance(from) > FOLLOW_DISTANCE;
    }

    static float pitchTowards(Vec offset) {
        return (float) -Math.toDegrees(Math.atan2(offset.y(), Math.hypot(offset.x(), offset.z())));
    }

    void follow(Vec place) {
        Vec offset = place.subtract(flight.position());
        if (chasing(flight.position(), place)) {
            flight.step(true, Vec.ZERO.yawTowards(offset), pitchTowards(offset));
        } else {
            flight.hover();
        }
        Vec position = flight.position();
        float facing = flight.yaw();
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
