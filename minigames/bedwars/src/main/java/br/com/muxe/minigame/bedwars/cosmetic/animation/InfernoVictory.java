package br.com.muxe.minigame.bedwars.cosmetic.animation;

import br.com.muxe.core.bukkit.cosmetic.animation.Scene;
import br.com.muxe.core.bukkit.cosmetic.animation.SceneAnimation;
import br.com.muxe.core.bukkit.cosmetic.animation.Stage;
import br.com.muxe.core.bukkit.cosmetic.animation.Stage.Actor;
import br.com.muxe.core.bukkit.cosmetic.animation.Stage.BlockChange;
import br.com.muxe.core.bukkit.cosmetic.animation.Timeline;
import br.com.muxe.core.bukkit.cosmetic.animation.Vec;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.random.RandomGenerator;
import org.bukkit.Material;

public final class InfernoVictory implements SceneAnimation {

    static final int LENGTH = 200;
    static final int WAVE_FIRST = 1;
    static final int DUSK_TICKS = 20;
    static final long NOON = 6000;
    static final long DUSK_RATE = 1200;
    static final long DUSK_EASE = 30;
    static final int BLAZES = 15;
    static final double BLAZE_RING = 10;
    static final int BLAZE_MAX_HEIGHT = 19;
    static final double BLAZE_FALL = 0.114;
    static final double BLAZE_EYES = 1.5;
    static final double BREATHE_CHANCE = 1.0 / 190;
    static final float BREATHE_LOW = 54 / 63f;
    static final float BREATHE_HIGH = 66 / 63f;
    static final int FIRST_SHOT = 5;
    static final int SHOT_EVERY = 5;
    static final int FIREBALL_LIFE = 60;
    static final int RESYNC_EVERY = 10;
    static final double FLAME_REACH = 15;
    static final int FLAME_COUNT = 15;
    static final double FLAME_SPREAD = 0.4;
    static final double FLAME_SPEED = 1;
    static final int LAUNCH_FIRST = 1;
    static final int LAUNCH_EVERY = 10;
    static final double KNOCK_SIDEWAYS = 1.5;
    static final double KNOCK_UP = 1.0;
    static final float EXPLODE_LOW = 37 / 63f;
    static final float EXPLODE_HIGH = 48 / 63f;
    static final float EXPLODE_VOLUME = 4;
    static final double CRATER_RADIUS = 3;
    static final int MAX_DEBRIS = 60;
    static final int DEBRIS_DELAY = 2;
    static final int DEBRIS_LIFE = 42;
    static final double DEBRIS_MIN_SPEED = 1.2;
    static final double DEBRIS_MAX_SPEED = 2.0;
    static final double DEBRIS_LIFT = 1.0;
    static final double EYE_HEIGHT = 1.62;

    @Override
    public String id() {
        return "inferno";
    }

    @Override
    public void script(Scene scene, Stage stage, Timeline timeline, RandomGenerator random) {
        Show show = new Show(scene, stage, random);
        timeline.repeat(0, LENGTH, 1, show::tick);
        timeline.lasts(LENGTH);
    }

    static List<Vec> blazeSpots(Vec origin, RandomGenerator random) {
        List<Vec> spots = new ArrayList<>(BLAZES);
        for (int index = 0; index < BLAZES; index++) {
            Vec around = Kit.horizontal(random.nextDouble() * 360, BLAZE_RING);
            double height = random.nextInt(BLAZE_MAX_HEIGHT + 1);
            spots.add(new Vec(Math.floor(origin.x() + around.x()) + 0.5, Math.floor(origin.y()) + height, Math.floor(origin.z() + around.z()) + 0.5));
        }
        return spots;
    }

    static long dayTime(int tick) {
        long clamped = Math.min(Math.max(tick, 0), DUSK_TICKS);
        return NOON + DUSK_RATE * clamped - DUSK_EASE * clamped * clamped;
    }

    static Vec knock(Vec motion) {
        Vec flat = new Vec(motion.x(), 0, motion.z()).normalize();
        return new Vec(flat.x() * KNOCK_SIDEWAYS, KNOCK_UP, flat.z() * KNOCK_SIDEWAYS);
    }

    static Vec debrisVelocity(Vec block, Vec centre, RandomGenerator random) {
        Vec away = new Vec(block.x() - centre.x(), 0, block.z() - centre.z()).normalize();
        double speed = Kit.between(random, DEBRIS_MIN_SPEED, DEBRIS_MAX_SPEED);
        return new Vec(away.x() * speed, DEBRIS_LIFT, away.z() * speed);
    }

    private static final class Show {

        private final Scene scene;
        private final Stage stage;
        private final RandomGenerator random;
        private final MapWave wave;
        private final Vec heading;
        private final float yaw;
        private final List<Blaze> blazes = new ArrayList<>();
        private final List<Shot> shots = new ArrayList<>();
        private final List<Throw> pending = new ArrayList<>();
        private final List<Piece> debris = new ArrayList<>();
        private int now;

        Show(Scene scene, Stage stage, RandomGenerator random) {
            this.scene = scene;
            this.stage = stage;
            this.random = random;
            int[] box = MapWave.box(scene.area(), (int) Math.floor(scene.origin().x()), (int) Math.floor(scene.origin().z()));
            Patches lava = new Patches(box, MapWave.BOTTOM, MapWave.TOP, InfernoPalette.LAVA_PER_COLUMN, random);
            this.wave = new MapWave(stage, scene, new InfernoPalette(lava), random);
            this.heading = scene.facing();
            this.yaw = Vec.ZERO.yawTowards(heading);
        }

        void tick(int tick) {
            now = tick;
            if (tick >= LENGTH) {
                clear();
                return;
            }
            if (tick == 0) {
                blazeSpots(scene.origin(), random).forEach(spot -> blazes.add(new Blaze(stage.mob("Blaze", spot, yaw), spot)));
            }
            if (tick >= WAVE_FIRST && !wave.done()) {
                wave.step();
            }
            if (tick >= 1 && tick <= DUSK_TICKS) {
                stage.time(dayTime(tick));
            }
            if (tick >= LAUNCH_FIRST && (tick - LAUNCH_FIRST) % LAUNCH_EVERY == 0) {
                stage.sound("fireworks.launch", winner(), 3, 1);
            }
            if (tick >= 1) {
                flame();
            }
            blazes.forEach(Blaze::fall);
            breathe();
            if (tick >= FIRST_SHOT && (tick - FIRST_SHOT) % SHOT_EVERY == 0) {
                shoot();
            }
            fly();
            settleDebris();
        }

        private Vec winner() {
            if (scene.riders().isEmpty()) {
                return scene.origin();
            }
            return stage.eyes(scene.riders().getFirst()).map(eyes -> eyes.add(0, -EYE_HEIGHT, 0)).orElse(scene.origin());
        }

        private Map<UUID, Vec> feet() {
            Map<UUID, Vec> feet = new LinkedHashMap<>();
            for (UUID rider : scene.riders()) {
                stage.eyes(rider).ifPresent(eyes -> feet.put(rider, eyes.add(0, -EYE_HEIGHT, 0)));
            }
            return feet;
        }

        private void flame() {
            Vec origin = scene.origin();
            Vec spot = new Vec(origin.x() + Kit.between(random, -FLAME_REACH, FLAME_REACH), origin.y(), origin.z() + Kit.between(random, -FLAME_REACH, FLAME_REACH));
            stage.particle("FLAME", spot, FLAME_COUNT, new Vec(FLAME_SPREAD, 0, FLAME_SPREAD), FLAME_SPEED);
        }

        private void shoot() {
            Blaze shooter = blazes.get(random.nextInt(blazes.size()));
            List<Vec> targets = new ArrayList<>(feet().values());
            Vec target = targets.isEmpty() ? scene.origin() : targets.get(random.nextInt(targets.size()));
            Vec eyes = target.add(0, EYE_HEIGHT, 0);
            Vec start = shooter.position.add(0, BLAZE_EYES, 0);
            shooter.face(target);
            Fireball fireball = new Fireball(start, eyes, heading);
            Actor actor = stage.fireball(start, heading);
            actor.velocity(fireball.motion());
            shots.add(new Shot(fireball, actor));
        }

        private void fly() {
            Map<UUID, Vec> feet = feet();
            Iterator<Shot> flying = shots.iterator();
            while (flying.hasNext()) {
                Shot shot = flying.next();
                shot.age++;
                Optional<Fireball.Impact> impact = shot.fireball.step(stage::solid, feet);
                if (impact.isPresent()) {
                    shot.actor.remove();
                    flying.remove();
                    explode(impact.get(), shot.fireball.motion(), feet);
                } else if (shot.age >= FIREBALL_LIFE) {
                    shot.actor.remove();
                    flying.remove();
                } else if (shot.age % RESYNC_EVERY == 0) {
                    shot.actor.teleport(shot.fireball.position(), 0, 0);
                    shot.actor.velocity(shot.fireball.motion());
                }
            }
        }

        private void explode(Fireball.Impact impact, Vec motion, Map<UUID, Vec> feet) {
            Vec at = impact.at();
            stage.particle("EXPLOSION_LARGE", at, 1, Vec.ZERO, 0);
            stage.sound("random.explode", at, EXPLODE_VOLUME, (float) Kit.between(random, EXPLODE_LOW, EXPLODE_HIGH));
            if (impact.rider().isPresent()) {
                UUID rider = impact.rider().get();
                Vec spot = feet.getOrDefault(rider, at);
                stage.particle("EXPLOSION_HUGE", spot, 1, Vec.ZERO, 0);
                stage.sound("random.explode", spot, 1, 1);
                stage.push(rider, knock(motion));
                return;
            }
            crater(at);
        }

        private void crater(Vec at) {
            int reach = (int) Math.ceil(CRATER_RADIUS);
            int centreX = (int) Math.floor(at.x());
            int centreY = (int) Math.floor(at.y());
            int centreZ = (int) Math.floor(at.z());
            List<BlockChange> changes = new ArrayList<>();
            for (int x = centreX - reach; x <= centreX + reach; x++) {
                for (int y = centreY - reach; y <= centreY + reach; y++) {
                    for (int z = centreZ - reach; z <= centreZ + reach; z++) {
                        if (new Vec(x + 0.5, y + 0.5, z + 0.5).distance(at) <= CRATER_RADIUS && stage.solid(x, y, z)) {
                            changes.add(new BlockChange(x, y, z, Material.AIR, 0));
                            wave.hold(x, y, z);
                        }
                    }
                }
            }
            if (changes.isEmpty()) {
                return;
            }
            stage.blocks(changes);
            for (BlockChange block : changes.subList(0, Math.min(MAX_DEBRIS, changes.size()))) {
                pending.add(new Throw(now + DEBRIS_DELAY, new Vec(block.x() + 0.5, block.y(), block.z() + 0.5), at));
            }
        }

        private void settleDebris() {
            Iterator<Throw> waiting = pending.iterator();
            while (waiting.hasNext()) {
                Throw next = waiting.next();
                if (next.due() <= now) {
                    waiting.remove();
                    Actor piece = stage.fallingBlock(next.spot(), Material.NETHERRACK, 0);
                    piece.velocity(debrisVelocity(next.spot(), next.centre(), random));
                    debris.add(new Piece(piece, now));
                }
            }
            debris.removeIf(piece -> {
                if (now - piece.born() >= DEBRIS_LIFE) {
                    piece.actor().remove();
                    return true;
                }
                return false;
            });
        }

        private void breathe() {
            for (Blaze blaze : blazes) {
                if (random.nextDouble() < BREATHE_CHANCE) {
                    stage.sound("mob.blaze.breathe", blaze.position, 1, (float) Kit.between(random, BREATHE_LOW, BREATHE_HIGH));
                }
            }
        }

        private void clear() {
            blazes.forEach(blaze -> blaze.actor.remove());
            shots.forEach(shot -> shot.actor.remove());
            debris.forEach(piece -> piece.actor().remove());
            blazes.clear();
            shots.clear();
            debris.clear();
            pending.clear();
        }

        private final class Blaze {

            private final Actor actor;
            private Vec position;
            private float facing;
            private boolean landed;

            Blaze(Actor actor, Vec position) {
                this.actor = actor;
                this.position = position;
                this.facing = yaw;
            }

            void fall() {
                if (landed) {
                    return;
                }
                Vec next = position.add(0, -BLAZE_FALL, 0);
                int blockY = (int) Math.floor(next.y());
                if (stage.solid((int) Math.floor(next.x()), blockY, (int) Math.floor(next.z()))) {
                    next = new Vec(next.x(), blockY + 1, next.z());
                    landed = true;
                }
                position = next;
                actor.teleport(position, facing, 0);
            }

            void face(Vec target) {
                facing = position.yawTowards(target);
                actor.teleport(position, facing, 0);
            }
        }

        private record Throw(int due, Vec spot, Vec centre) {
        }

        private record Piece(Actor actor, int born) {
        }

        private static final class Shot {

            private final Fireball fireball;
            private final Actor actor;
            private int age;

            Shot(Fireball fireball, Actor actor) {
                this.fireball = fireball;
                this.actor = actor;
            }
        }
    }
}
