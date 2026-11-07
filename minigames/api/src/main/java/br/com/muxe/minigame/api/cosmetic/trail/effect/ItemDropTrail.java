package br.com.muxe.minigame.api.cosmetic.trail.effect;

import br.com.muxe.core.bukkit.cosmetic.animation.Stage;
import br.com.muxe.minigame.api.cosmetic.trail.Flight;
import br.com.muxe.minigame.api.cosmetic.trail.ProjectileTrail;
import java.util.List;
import java.util.Objects;
import java.util.random.RandomGenerator;
import org.bukkit.Material;

public final class ItemDropTrail implements ProjectileTrail {

    private static final int BONE_MEAL = 15;
    private static final int LAPIS = 4;

    public static final ItemDropTrail OSSOS = new ItemDropTrail("ossos", 2, List.of(new Drop(Material.BONE, 0), new Drop(Material.INK_SACK, BONE_MEAL)));
    public static final ItemDropTrail SORTE_AZUL = new ItemDropTrail("sorte-azul", 1, List.of(new Drop(Material.INK_SACK, LAPIS)));

    private final String id;
    private final int interval;
    private final List<Drop> drops;
    private final DroppedItems items = new DroppedItems();

    public ItemDropTrail(String id, int interval, List<Drop> drops) {
        this.id = Objects.requireNonNull(id, "id");
        this.drops = List.copyOf(drops);
        this.interval = interval;
        if (interval < 1) {
            throw new IllegalArgumentException("interval must be positive");
        }
        if (this.drops.isEmpty()) {
            throw new IllegalArgumentException("drops must not be empty");
        }
    }

    @Override
    public String id() {
        return id;
    }

    public int interval() {
        return interval;
    }

    public List<Drop> drops() {
        return drops;
    }

    @Override
    public void tick(Flight flight, Stage stage, RandomGenerator random) {
        items.expire(stage, flight.age());
        if (flight.age() % interval == 0) {
            Drop drop = drops.get(random.nextInt(drops.size()));
            items.drop(stage, flight.position(), drop.material(), drop.data(), flight.age(), random);
        }
    }

    @Override
    public void land(Flight flight, Stage stage, RandomGenerator random) {
        items.forget(stage);
    }

    @Override
    public int linger() {
        return DroppedItems.LIFETIME;
    }

    public record Drop(Material material, int data) {

        public Drop {
            Objects.requireNonNull(material, "material");
        }
    }
}
