package br.com.muxe.minigame.api.cosmetic.catalog;

import br.com.muxe.core.bukkit.item.ItemBuilder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import org.bukkit.configuration.ConfigurationSection;

public final class CosmeticCatalog {

    private final String category;
    private final Map<Integer, Long> prices;
    private final Map<String, Cosmetic> cosmetics;
    private final Optional<String> defaultId;

    public CosmeticCatalog(String category, Map<Integer, Long> prices, List<Cosmetic> cosmetics) {
        this(category, prices, cosmetics, Optional.empty());
    }

    public CosmeticCatalog(String category, Map<Integer, Long> prices, List<Cosmetic> cosmetics, Optional<String> defaultId) {
        this.category = category;
        this.prices = Map.copyOf(prices);
        Map<String, Cosmetic> byId = new LinkedHashMap<>();
        cosmetics.forEach(cosmetic -> byId.put(cosmetic.id(), cosmetic));
        this.cosmetics = byId;
        if (defaultId.isPresent() && !byId.containsKey(defaultId.get())) {
            throw new IllegalArgumentException("The default cosmetic " + defaultId.get() + " is not in the category " + category);
        }
        this.defaultId = defaultId;
    }

    public static CosmeticCatalog from(ConfigurationSection section, String listKey, String defaultCategory, Predicate<String> animated) {
        Map<Integer, Long> prices = new HashMap<>();
        ConfigurationSection priceSection = section.getConfigurationSection("prices");
        if (priceSection != null) {
            for (String stars : priceSection.getKeys(false)) {
                prices.put(Integer.parseInt(stars), priceSection.getLong(stars));
            }
        }
        List<Cosmetic> cosmetics = new ArrayList<>();
        ConfigurationSection list = required(section, listKey);
        for (String id : list.getKeys(false)) {
            ConfigurationSection entry = required(list, id);
            cosmetics.add(new Cosmetic(
                id,
                entry.getString("name", id),
                ItemBuilder.from(required(entry, "icon")),
                entry.getInt("stars", 1),
                Optional.ofNullable(entry.getString("tag")),
                Availability.valueOf(entry.getString("availability", "EVENT").toUpperCase(Locale.ROOT)),
                entry.getStringList("description"),
                animated.test(id)
            ));
        }
        return new CosmeticCatalog(section.getString("category", defaultCategory), prices, cosmetics, Optional.ofNullable(section.getString("default")));
    }

    public String category() {
        return category;
    }

    public List<Cosmetic> all() {
        return List.copyOf(cosmetics.values());
    }

    public Optional<Cosmetic> find(String id) {
        return Optional.ofNullable(cosmetics.get(id));
    }

    public Optional<String> defaultId() {
        return defaultId;
    }

    public Optional<Cosmetic> defaultCosmetic() {
        return defaultId.flatMap(this::find);
    }

    public long price(Cosmetic cosmetic) {
        Long price = prices.get(cosmetic.stars());
        if (price == null) {
            throw new IllegalStateException("No price for " + cosmetic.stars() + " stars");
        }
        return price;
    }

    private static ConfigurationSection required(ConfigurationSection section, String path) {
        return Objects.requireNonNull(section.getConfigurationSection(path), "Missing configuration section " + section.getCurrentPath() + "." + path);
    }
}
