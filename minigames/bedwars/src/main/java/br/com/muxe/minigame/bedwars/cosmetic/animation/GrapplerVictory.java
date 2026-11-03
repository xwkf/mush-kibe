package br.com.muxe.minigame.bedwars.cosmetic.animation;

import br.com.muxe.core.bukkit.cosmetic.animation.Scene;
import br.com.muxe.core.bukkit.cosmetic.animation.SceneAnimation;
import br.com.muxe.core.bukkit.cosmetic.animation.Stage;
import br.com.muxe.core.bukkit.cosmetic.animation.Stage.Actor;
import br.com.muxe.core.bukkit.cosmetic.animation.Stage.Click;
import br.com.muxe.core.bukkit.cosmetic.animation.Timeline;
import br.com.muxe.core.bukkit.cosmetic.animation.Vec;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.random.RandomGenerator;
import org.bukkit.Material;

public final class GrapplerVictory implements SceneAnimation {

    static final int LENGTH = 200;
    static final int TOOL_SLOT = 0;
    static final String TOOL_NAME = "§aGrappler";
    static final double STEP = 0.25;
    static final double RANGE = 80;
    static final int REFINEMENTS = 10;
    static final double BASE_SPEED = 1.3;
    static final double SPEED_PER_BLOCK = 0.15;
    static final double MAX_SPEED = 3.9;
    static final double MIN_LIFT = 0.1;
    static final double CLIMB_BONUS = 0.3;
    static final double EYE_HEIGHT = 1.62;
    static final int LAUNCH_FIRST = 1;
    static final int LAUNCH_EVERY = 10;

    @Override
    public String id() {
        return "aventureiro";
    }

    @Override
    public void script(Scene scene, Stage stage, Timeline timeline, RandomGenerator random) {
        Session session = new Session(stage);
        timeline.at(0, () -> begin(stage, feet(scene, stage)));
        for (UUID rider : scene.riders()) {
            stage.give(rider, TOOL_SLOT, Material.LEASH, TOOL_NAME, click -> session.use(rider, click));
        }
        timeline.repeat(LAUNCH_FIRST, LENGTH, LAUNCH_EVERY, offset -> stage.sound("fireworks.launch", feet(scene, stage), 3, 1));
        timeline.repeat(1, LENGTH, 1, offset -> session.keepHooks());
        timeline.at(LENGTH, session::finish);
        timeline.lasts(LENGTH);
    }

    static Optional<Vec> trace(Stage stage, Vec eyes, Vec look) {
        Vec direction = look.normalize();
        if (direction.length() == 0) {
            return Optional.empty();
        }
        Vec previous = eyes;
        for (double distance = STEP; distance <= RANGE; distance += STEP) {
            Vec point = eyes.add(direction.multiply(distance));
            if (solid(stage, point)) {
                return Optional.of(surface(stage, previous, point));
            }
            previous = point;
        }
        return Optional.empty();
    }

    static Vec pull(Vec from, Vec hook) {
        Vec offset = hook.subtract(from);
        Vec direction = offset.normalize();
        double speed = speed(offset.length());
        double lift = Math.max(MIN_LIFT, direction.y() * speed + (direction.y() > 0 ? CLIMB_BONUS : 0));
        return new Vec(direction.x() * speed, Math.min(lift, MAX_SPEED), direction.z() * speed);
    }

    static double speed(double distance) {
        return Math.min(BASE_SPEED + SPEED_PER_BLOCK * distance, MAX_SPEED);
    }

    private static Vec surface(Stage stage, Vec outside, Vec inside) {
        Vec near = outside;
        Vec far = inside;
        for (int step = 0; step < REFINEMENTS; step++) {
            Vec middle = near.add(far.subtract(near).multiply(0.5));
            if (solid(stage, middle)) {
                far = middle;
            } else {
                near = middle;
            }
        }
        return far;
    }

    private static boolean solid(Stage stage, Vec point) {
        return stage.solid((int) Math.floor(point.x()), (int) Math.floor(point.y()), (int) Math.floor(point.z()));
    }

    private static Vec feet(Scene scene, Stage stage) {
        return scene.riders().stream().findFirst().flatMap(stage::eyes).map(eyes -> eyes.add(0, -EYE_HEIGHT, 0)).orElse(scene.origin());
    }

    private static void begin(Stage stage, Vec feet) {
        stage.particle("VILLAGER_HAPPY", feet.add(0, 1, 0), 20, new Vec(0.5, 1, 0.5), 1);
        stage.sound("random.pop", feet, 0.8f, 0.79f);
        stage.sound("mob.horse.armor", feet, 0.8f, 1.19f);
    }

    private static final class Session {

        private final Stage stage;
        private final Map<UUID, Hook> hooks = new ConcurrentHashMap<>();
        private volatile boolean over;

        Session(Stage stage) {
            this.stage = stage;
        }

        void use(UUID player, Click click) {
            if (over) {
                return;
            }
            if (click == Click.LEFT) {
                throwHook(player);
            } else {
                pullTowardHook(player);
            }
        }

        void keepHooks() {
            hooks.values().forEach(hook -> hook.actor().teleport(hook.point(), 0, 0));
        }

        void finish() {
            over = true;
            hooks.values().forEach(hook -> hook.actor().remove());
            hooks.clear();
        }

        private void throwHook(UUID player) {
            Optional<Vec> eyes = stage.eyes(player);
            Optional<Vec> look = stage.look(player);
            if (eyes.isEmpty() || look.isEmpty()) {
                return;
            }
            trace(stage, eyes.get(), look.get()).ifPresent(point -> {
                Hook previous = hooks.put(player, new Hook(stage.hook(point, player), point));
                if (previous != null) {
                    previous.actor().remove();
                }
            });
        }

        private void pullTowardHook(UUID player) {
            Hook hook = hooks.get(player);
            Optional<Vec> eyes = stage.eyes(player);
            if (hook == null || eyes.isEmpty()) {
                return;
            }
            Vec center = eyes.get().add(0, -EYE_HEIGHT / 2, 0);
            stage.push(player, pull(center, hook.point()));
            stage.sound("step.gravel", eyes.get().add(0, -EYE_HEIGHT, 0), 1, 1);
        }
    }

    private record Hook(Actor actor, Vec point) {
    }
}
