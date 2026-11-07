package br.com.muxe.minigame.api.cosmetic.animation;

import br.com.muxe.core.bukkit.cosmetic.animation.Ground;
import br.com.muxe.core.bukkit.cosmetic.animation.Scene;
import br.com.muxe.core.bukkit.cosmetic.animation.SceneAnimation;
import br.com.muxe.core.bukkit.cosmetic.animation.Stage.Actor;
import br.com.muxe.core.bukkit.cosmetic.animation.Stage;
import br.com.muxe.core.bukkit.cosmetic.animation.Timeline;
import br.com.muxe.core.bukkit.cosmetic.animation.Vec;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.UUID;
import java.util.random.RandomGenerator;

public final class EndVictory implements SceneAnimation {

    static final int LENGTH = 200;
    static final int WAVE_FIRST = 1;
    static final int[] ENDERMEN_TICKS = {5, 8};
    static final double ENDERMAN_MIN_DISTANCE = 18;
    static final double ENDERMAN_MAX_DISTANCE = 25;
    static final double NEAR_MIN_DISTANCE = 4;
    static final double NEAR_MAX_DISTANCE = 10;
    static final int SPOT_ATTEMPTS = 24;
    static final int GROUND_DEPTH = 40;
    static final double GROUND_ABOVE = 6;
    static final int TELEPORT_TICK = 47;
    static final double BESIDE_MIN = 1.5;
    static final double BESIDE_MAX = 3.5;
    static final int IDLE_TICK = 78;
    static final int WALK_FROM = 115;
    static final double WALK_SPEED = 0.17;
    static final double CLIMB = 1.1;
    static final int STEP_EVERY = 12;
    static final float STEP_VOLUME = 0.15f;
    static final int LAUNCH_FIRST = 1;
    static final int LAUNCH_EVERY = 10;
    static final double EYE_HEIGHT = 1.62;

    @Override
    public String id() {
        return "o-end";
    }

    @Override
    public void script(Scene scene, Stage stage, Timeline timeline, RandomGenerator random) {
        Show show = new Show(scene, stage, random);
        timeline.repeat(0, LENGTH, 1, show::tick);
        timeline.lasts(LENGTH);
    }

    static OptionalDouble ground(Ground.Solid solid, double x, double y, double z) {
        return Ground.hit(solid, x, y + GROUND_ABOVE, z, GROUND_DEPTH);
    }

    static Optional<Vec> spot(Ground.Solid solid, Vec centre, double minDistance, double maxDistance, RandomGenerator random) {
        for (int attempt = 0; attempt < SPOT_ATTEMPTS; attempt++) {
            Vec around = Kit.horizontal(random.nextDouble() * 360, Kit.between(random, minDistance, maxDistance));
            double x = Math.floor(centre.x() + around.x()) + 0.5;
            double z = Math.floor(centre.z() + around.z()) + 0.5;
            OptionalDouble floor = ground(solid, x, centre.y(), z);
            if (floor.isPresent()) {
                return Optional.of(new Vec(x, floor.getAsDouble(), z));
            }
        }
        return Optional.empty();
    }

    private static final class Show {

        private final Scene scene;
        private final Stage stage;
        private final RandomGenerator random;
        private final MapWave wave;
        private final EndPillars pillars;
        private final List<Enderman> endermen = new ArrayList<>();
        private Enderman visitor;

        Show(Scene scene, Stage stage, RandomGenerator random) {
            this.scene = scene;
            this.stage = stage;
            this.random = random;
            int[] box = MapWave.box(scene.area(), (int) Math.floor(scene.origin().x()), (int) Math.floor(scene.origin().z()));
            Patches patches = new Patches(box, MapWave.BOTTOM, MapWave.TOP, EndPalette.PATCHES_PER_COLUMN, random);
            this.wave = new MapWave(stage, scene, new EndPalette(patches), random);
            this.pillars = new EndPillars(scene.area());
            pillars.holdIn(wave);
        }

        void tick(int tick) {
            if (tick >= LENGTH) {
                endermen.forEach(enderman -> enderman.actor.remove());
                endermen.clear();
                return;
            }
            if (tick >= WAVE_FIRST && !wave.done()) {
                wave.step();
                pillars.raiseReached(stage, wave);
            }
            if (tick >= LAUNCH_FIRST && (tick - LAUNCH_FIRST) % LAUNCH_EVERY == 0) {
                stage.sound("fireworks.launch", feet(winner()), 3, 1);
            }
            for (int spawn : ENDERMEN_TICKS) {
                if (tick == spawn) {
                    summon();
                }
            }
            if (tick == TELEPORT_TICK && !endermen.isEmpty()) {
                visit();
            }
            if (tick == IDLE_TICK && visitor != null) {
                stage.sound("mob.endermen.idle", visitor.position, 1, 1);
            }
            if (tick >= WALK_FROM && visitor != null) {
                visitor.walk(tick - WALK_FROM);
            }
        }

        private Optional<UUID> winner() {
            return scene.riders().isEmpty() ? Optional.empty() : Optional.of(scene.riders().getFirst());
        }

        private Vec feet(Optional<UUID> rider) {
            return rider.flatMap(stage::eyes).map(eyes -> eyes.add(0, -EYE_HEIGHT, 0)).orElse(scene.origin());
        }

        private void summon() {
            Vec origin = scene.origin();
            Vec position = spot(stage::solid, origin, ENDERMAN_MIN_DISTANCE, ENDERMAN_MAX_DISTANCE, random)
                .or(() -> spot(stage::solid, origin, NEAR_MIN_DISTANCE, NEAR_MAX_DISTANCE, random))
                .orElseGet(() -> origin.add(Kit.horizontal(random.nextDouble() * 360, NEAR_MAX_DISTANCE)));
            float yaw = (float) (random.nextDouble() * 360 - 180);
            endermen.add(new Enderman(stage.mob("Enderman", position, yaw), position, yaw));
        }

        private void visit() {
            List<UUID> riders = scene.riders().stream().filter(rider -> stage.eyes(rider).isPresent()).toList();
            Optional<UUID> host = riders.isEmpty() ? Optional.empty() : Optional.of(riders.get(random.nextInt(riders.size())));
            Vec target = feet(host);
            Vec beside = spot(stage::solid, target, BESIDE_MIN, BESIDE_MAX, random)
                .orElseGet(() -> target.add(Kit.horizontal(random.nextDouble() * 360, BESIDE_MAX)));
            visitor = endermen.getFirst();
            visitor.position = beside;
            visitor.yaw = visitor.position.yawTowards(target);
            visitor.actor.teleport(visitor.position, visitor.yaw, 0);
            stage.sound("mob.endermen.portal", visitor.position, 1, 1);
        }

        private final class Enderman {

            private final Actor actor;
            private Vec position;
            private float yaw;
            private boolean stopped;

            Enderman(Actor actor, Vec position, float yaw) {
                this.actor = actor;
                this.position = position;
                this.yaw = yaw;
            }

            void walk(int step) {
                if (stopped) {
                    return;
                }
                Vec ahead = Kit.horizontal(yaw + 90, WALK_SPEED);
                Vec next = position.add(ahead.x(), 0, ahead.z());
                OptionalDouble floor = ground(stage::solid, next.x(), position.y(), next.z());
                if (floor.isEmpty() || Math.abs(floor.getAsDouble() - position.y()) > CLIMB) {
                    stopped = true;
                    return;
                }
                position = new Vec(next.x(), floor.getAsDouble(), next.z());
                actor.teleport(position, yaw, 0);
                if (step % STEP_EVERY == 0) {
                    stage.sound("step.stone", position, STEP_VOLUME, 1);
                }
            }
        }
    }
}
