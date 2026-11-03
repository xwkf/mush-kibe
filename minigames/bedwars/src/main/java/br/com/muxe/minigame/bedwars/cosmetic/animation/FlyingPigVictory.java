package br.com.muxe.minigame.bedwars.cosmetic.animation;

import br.com.muxe.core.bukkit.cosmetic.animation.Scene;
import br.com.muxe.core.bukkit.cosmetic.animation.SceneAnimation;
import br.com.muxe.core.bukkit.cosmetic.animation.Stage;
import br.com.muxe.core.bukkit.cosmetic.animation.Stage.Actor;
import br.com.muxe.core.bukkit.cosmetic.animation.Stage.Mount;
import br.com.muxe.core.bukkit.cosmetic.animation.Timeline;
import br.com.muxe.core.bukkit.cosmetic.animation.Vec;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.random.RandomGenerator;

public final class FlyingPigVictory implements SceneAnimation {

    static final double SEAT_ABOVE_GROUND = 1.78125;
    static final double PIG_BELOW_SEAT = 0.78125;
    static final int FOLLOWERS = 6;
    static final int BABY_FOLLOWER = 3;
    static final int FIRST_DELAY = 5;
    static final int DELAY_STEP = 6;
    static final int SADDLE = 16;
    static final int AGE = 12;
    static final Vec TRAIL_SPREAD = new Vec(0.3, 0.1, 0.3);
    static final Vec HEART_SPREAD = new Vec(0.3, 0.2, 0.3);
    static final double HEART_HEIGHT = 0.9;
    static final double HEART_CHANCE = 0.05;
    static final double SAY_CHANCE = 1.0 / 33;
    static final int LAUNCH_EVERY = 10;
    static final int LAST_LAUNCH = 190;
    static final int WINGS_FIRST = 29;
    static final int WINGS_EVERY = 30;
    static final int LENGTH = 200;

    @Override
    public String id() {
        return "como-treinar-seu-porco";
    }

    @Override
    public void script(Scene scene, Stage stage, Timeline timeline, RandomGenerator random) {
        Vec seat = scene.origin().add(0, SEAT_ABOVE_GROUND, 0);
        Vec spawn = seat.add(0, -PIG_BELOW_SEAT, 0);
        float yaw = Vec.ZERO.yawTowards(scene.facing());
        Flight flight = new Flight(seat, yaw);
        Mount[] mount = new Mount[1];
        Train[] train = new Train[1];
        timeline.at(0, () -> {
            mount[0] = stage.mount(seat, yaw, scene.riders());
            train[0] = train(stage, spawn, yaw);
            stage.sound("mob.pig.say", spawn, 0.6f, 1);
        });
        timeline.repeat(1, LENGTH - 1, 1, tick -> {
            float lookYaw = mount[0].lookYaw();
            flight.step(mount[0].pushing(), lookYaw, mount[0].lookPitch());
            mount[0].move(flight.position(), lookYaw);
            train[0].lead(flight.position().add(0, -PIG_BELOW_SEAT, 0), flight.yaw());
        });
        timeline.repeat(0, LENGTH - 1, 1, tick -> {
            train[0].follow();
            ambience(stage, random, train[0]);
        });
        timeline.repeat(0, LAST_LAUNCH, LAUNCH_EVERY, tick -> stage.sound("fireworks.launch", mount[0].seat(), 3, 1));
        timeline.repeat(WINGS_FIRST, LENGTH - 1, WINGS_EVERY, tick -> stage.sound("mob.enderdragon.wings", mount[0].seat(), 0.3f, 1.587f));
        timeline.at(LENGTH, () -> train[0].remove());
    }

    static int delay(int follower) {
        return FIRST_DELAY + follower * DELAY_STEP;
    }

    static Optional<Track> replay(List<Track> history, int delay) {
        int index = history.size() - 1 - delay;
        return index < 0 ? Optional.empty() : Optional.of(history.get(index));
    }

    private static Train train(Stage stage, Vec spawn, float yaw) {
        List<Actor> followers = new ArrayList<>();
        for (int follower = 0; follower < FOLLOWERS; follower++) {
            Actor pig = stage.mob("Pig", spawn, yaw);
            if (follower == BABY_FOLLOWER) {
                pig.watch(AGE, (byte) -1);
            }
            followers.add(pig);
        }
        Actor leader = stage.mob("Pig", spawn, yaw);
        leader.watch(SADDLE, (byte) 1);
        return new Train(leader, followers, new Track(spawn, yaw));
    }

    private static void ambience(Stage stage, RandomGenerator random, Train train) {
        stage.particle("CLOUD", train.at(0).position(), 1, TRAIL_SPREAD, 0.01);
        for (int follower = 0; follower < FOLLOWERS; follower++) {
            if (random.nextDouble() < HEART_CHANCE) {
                stage.particle("HEART", train.at(follower).position().add(0, HEART_HEIGHT, 0), 1, HEART_SPREAD, 1.0);
            }
        }
        if (random.nextDouble() < SAY_CHANCE) {
            Vec voice = train.at(random.nextInt(FOLLOWERS)).position();
            stage.sound("mob.pig.say", voice, 0.3f, (float) Kit.between(random, 0.92, 1.14));
        }
    }

    record Track(Vec position, float yaw) {
    }

    private static final class Train {

        private final Actor leader;
        private final List<Actor> followers;
        private final List<Track> history = new ArrayList<>();

        Train(Actor leader, List<Actor> followers, Track start) {
            this.leader = leader;
            this.followers = followers;
            history.add(start);
        }

        void lead(Vec position, float yaw) {
            leader.teleport(position, yaw, 0);
            history.add(new Track(position, yaw));
        }

        void follow() {
            for (int follower = 0; follower < followers.size(); follower++) {
                Actor pig = followers.get(follower);
                replay(history, delay(follower)).ifPresent(track -> pig.teleport(track.position(), track.yaw(), 0));
            }
        }

        Track at(int follower) {
            return replay(history, delay(follower)).orElse(history.getFirst());
        }

        void remove() {
            leader.remove();
            followers.forEach(Actor::remove);
        }
    }
}
