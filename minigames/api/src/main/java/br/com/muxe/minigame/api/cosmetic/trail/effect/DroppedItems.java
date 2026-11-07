package br.com.muxe.minigame.api.cosmetic.trail.effect;

import br.com.muxe.core.bukkit.cosmetic.animation.Stage.Actor;
import br.com.muxe.core.bukkit.cosmetic.animation.Stage;
import br.com.muxe.core.bukkit.cosmetic.animation.Vec;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.random.RandomGenerator;
import org.bukkit.Material;

public final class DroppedItems {

    public static final int LIFETIME = 5;
    public static final double DROP = 0.5;
    public static final double RISE = 0.1;
    public static final double SCATTER = 0.1;

    private final Map<Stage, Deque<Dropped>> alive = new WeakHashMap<>();

    public void expire(Stage stage, int age) {
        Deque<Dropped> items = alive.get(stage);
        while (items != null && !items.isEmpty() && items.peekFirst().expiry() <= age) {
            items.pollFirst().actor().remove();
        }
    }

    public void drop(Stage stage, Vec point, Material material, int data, int age, RandomGenerator random) {
        Actor item = stage.item(point.add(0, -DROP, 0), material, data);
        item.velocity(new Vec(random.nextDouble(-SCATTER, SCATTER), RISE, random.nextDouble(-SCATTER, SCATTER)));
        alive.computeIfAbsent(stage, key -> new ArrayDeque<>()).addLast(new Dropped(item, age + LIFETIME));
    }

    public void forget(Stage stage) {
        alive.remove(stage);
    }

    private record Dropped(Actor actor, int expiry) {
    }
}
