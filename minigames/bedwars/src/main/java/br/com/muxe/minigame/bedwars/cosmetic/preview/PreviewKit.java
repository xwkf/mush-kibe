package br.com.muxe.minigame.bedwars.cosmetic.preview;

import br.com.muxe.core.bukkit.item.ItemBuilder;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;

public record PreviewKit(Map<Integer, ItemBuilder> items, int refillTicks) {

    public static final int DEFAULT_REFILL_TICKS = 2;

    public PreviewKit {
        items = Collections.unmodifiableMap(new LinkedHashMap<>(items));
        if (refillTicks < 1) {
            throw new IllegalArgumentException("The kit refill needs at least 1 tick, got " + refillTicks);
        }
    }

    public static Optional<PreviewKit> from(ConfigurationSection preview) {
        ConfigurationSection kit = preview.getConfigurationSection("kit");
        if (kit == null) {
            return Optional.empty();
        }
        ConfigurationSection list = Objects.requireNonNull(kit.getConfigurationSection("items"), "Missing configuration section " + kit.getCurrentPath() + ".items");
        Map<Integer, ItemBuilder> items = new LinkedHashMap<>();
        for (String slot : list.getKeys(false)) {
            items.put(Integer.parseInt(slot), ItemBuilder.from(Objects.requireNonNull(list.getConfigurationSection(slot), "Missing kit item " + slot)));
        }
        return Optional.of(new PreviewKit(items, kit.getInt("refill-ticks", DEFAULT_REFILL_TICKS)));
    }

    public boolean holds(int slot) {
        return items.containsKey(slot);
    }

    public boolean needsRefill(int slot, Material material, int data, int amount) {
        ItemBuilder item = items.get(slot);
        if (item == null) {
            return false;
        }
        return material != item.material() || data != item.data() || amount < item.amount();
    }
}
