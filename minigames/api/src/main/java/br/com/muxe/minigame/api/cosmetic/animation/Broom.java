package br.com.muxe.minigame.api.cosmetic.animation;

import br.com.muxe.core.bukkit.cosmetic.animation.Stage.Actor;
import br.com.muxe.core.bukkit.cosmetic.animation.Stage.Pose;
import br.com.muxe.core.bukkit.cosmetic.animation.Stage.Slot;
import br.com.muxe.core.bukkit.cosmetic.animation.Stage.StandStyle;
import br.com.muxe.core.bukkit.cosmetic.animation.Stage;
import br.com.muxe.core.bukkit.cosmetic.animation.Vec;
import java.util.ArrayList;
import java.util.List;

final class Broom {

    static final String BRISTLES = "63261e1642f2eaa6f3e335c05940e2a78cf9aefe90d48c7960c7337f14227ec8";
    static final String HANDLE = "a9d806b75ec95006c5ec3866c548c5571af3e788c7d1629ede64bc1b748859cd";
    static final Pose LYING = Pose.STANDING.withHead(new Vec(0, 0, 90));
    static final double BRISTLES_FORWARD = -1.54;
    static final double BRISTLES_UP = -1.375;
    static final int HANDLE_PIECES = 5;
    static final double HANDLE_FORWARD = -0.94;
    static final double HANDLE_STEP = 0.406;
    static final double HANDLE_UP = -0.688;
    static final double HANDLE_DIP = 0.031;
    static final float SIDEWAYS = 90;
    static final double CAT_FORWARD = -0.77;
    static final double CAT_UP = 0.25;
    static final float CAT_TURN = 30;
    static final int AGE = 12;
    static final int SITTING = 16;
    static final int CAT_SKIN = 18;
    static final byte BLACK_CAT = 1;

    private final List<Part> parts;

    private Broom(List<Part> parts) {
        this.parts = parts;
    }

    static Broom build(Stage stage, Vec seat, float yaw) {
        List<Part> parts = new ArrayList<>();
        parts.add(stick(stage, seat, yaw, StandStyle.HIDDEN, BRISTLES, BRISTLES_FORWARD, BRISTLES_UP));
        for (int piece = 0; piece < HANDLE_PIECES; piece++) {
            parts.add(stick(stage, seat, yaw, StandStyle.HIDDEN_SMALL, HANDLE, HANDLE_FORWARD + piece * HANDLE_STEP, HANDLE_UP - piece * HANDLE_DIP));
        }
        Actor cat = stage.mob("Ozelot", spot(seat, yaw, CAT_FORWARD, CAT_UP), yaw + CAT_TURN);
        cat.watch(AGE, (byte) -1);
        cat.watch(SITTING, (byte) 1);
        cat.watch(CAT_SKIN, BLACK_CAT);
        parts.add(new Part(cat, CAT_FORWARD, CAT_UP, CAT_TURN));
        return new Broom(parts);
    }

    static Vec spot(Vec seat, float yaw, double forward, double up) {
        return seat.add(forward(yaw).multiply(forward)).add(0, up, 0);
    }

    static Vec forward(double yaw) {
        return Flight.direction(yaw, 0);
    }

    static Vec right(double yaw) {
        Vec forward = forward(yaw);
        return new Vec(-forward.z(), 0, forward.x());
    }

    void place(Vec seat, float yaw) {
        for (Part part : parts) {
            part.actor().teleport(spot(seat, yaw, part.forward(), part.up()), yaw + part.turn(), 0);
        }
    }

    void remove() {
        parts.forEach(part -> part.actor().remove());
    }

    private static Part stick(Stage stage, Vec seat, float yaw, StandStyle style, String texture, double forward, double up) {
        Actor stand = stage.armorStand(spot(seat, yaw, forward, up), yaw + SIDEWAYS, style);
        stand.equipSkull(Slot.HELMET, texture);
        stand.pose(LYING);
        return new Part(stand, forward, up, SIDEWAYS);
    }

    private record Part(Actor actor, double forward, double up, float turn) {
    }
}
