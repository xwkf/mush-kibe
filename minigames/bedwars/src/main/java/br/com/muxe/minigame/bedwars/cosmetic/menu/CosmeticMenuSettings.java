package br.com.muxe.minigame.bedwars.cosmetic.menu;

import br.com.muxe.core.bukkit.item.ItemBuilder;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import org.bukkit.configuration.ConfigurationSection;

public record CosmeticMenuSettings(
    String bedwarsTitle,
    int bedwarsRows,
    MenuSlot cosmetics,
    Optional<MenuSlot> missions,
    Optional<MenuSlot> quickBuy,
    Optional<MenuSlot> preferences,
    Optional<MenuSlot> hotbar,
    Map<Integer, ItemBuilder> bedwarsItems,
    Map<Integer, ItemBuilder> bedwarsSoon,
    String categoriesTitle,
    int categoriesRows,
    Map<String, CategoryMenu> categories,
    Map<Integer, ItemBuilder> soon,
    MenuSlot categoriesBack
) {

    public static final String BED_DESTRUCTION = "bed-destruction";
    public static final String FINAL_KILL = "final-kill";
    public static final String VICTORY_EFFECT = "victory-effect";
    private static final List<String> CATEGORY_KEYS = List.of(BED_DESTRUCTION, FINAL_KILL, VICTORY_EFFECT);

    public CosmeticMenuSettings {
        bedwarsItems = Map.copyOf(bedwarsItems);
        Map<Integer, ItemBuilder> stillSoon = new LinkedHashMap<>(bedwarsSoon);
        stillSoon.remove(cosmetics.slot());
        missions.ifPresent(button -> stillSoon.remove(button.slot()));
        quickBuy.ifPresent(button -> stillSoon.remove(button.slot()));
        preferences.ifPresent(button -> stillSoon.remove(button.slot()));
        hotbar.ifPresent(button -> stillSoon.remove(button.slot()));
        bedwarsSoon = Map.copyOf(stillSoon);
        categories = Collections.unmodifiableMap(new LinkedHashMap<>(categories));
        Map<Integer, ItemBuilder> categorySoon = new LinkedHashMap<>(soon);
        categories.values().forEach(category -> categorySoon.remove(category.button().slot()));
        soon = Map.copyOf(categorySoon);
    }

    public static CosmeticMenuSettings from(ConfigurationSection menus) {
        ConfigurationSection bedwars = section(menus, "bedwars");
        ConfigurationSection categories = section(menus, "categories");
        CosmeticListSettings bedList = CosmeticListSettings.from(section(menus, BED_DESTRUCTION));
        Map<String, CategoryMenu> byKey = new LinkedHashMap<>();
        for (String key : CATEGORY_KEYS) {
            ConfigurationSection button = categories.getConfigurationSection(key);
            if (button != null) {
                CosmeticListSettings list = key.equals(BED_DESTRUCTION) ? bedList : bedList.over(Optional.ofNullable(menus.getConfigurationSection(key)));
                byKey.put(key, new CategoryMenu(key, MenuSlot.from(button), list));
            }
        }
        return new CosmeticMenuSettings(
            bedwars.getString("title", "Menu do Bed Wars"),
            bedwars.getInt("rows", 3),
            MenuSlot.from(section(bedwars, "cosmetics")),
            Optional.ofNullable(bedwars.getConfigurationSection("missions")).map(MenuSlot::from),
            Optional.ofNullable(bedwars.getConfigurationSection("quick-buy")).map(MenuSlot::from),
            Optional.ofNullable(bedwars.getConfigurationSection("preferences")).map(MenuSlot::from),
            Optional.ofNullable(bedwars.getConfigurationSection("hotbar")).map(MenuSlot::from),
            itemsBySlot(section(bedwars, "items")),
            Optional.ofNullable(bedwars.getConfigurationSection("soon")).map(CosmeticMenuSettings::itemsBySlot).orElseGet(Map::of),
            categories.getString("title", "Cosméticos do Bed Wars"),
            categories.getInt("rows", 6),
            byKey,
            itemsBySlot(section(categories, "soon")),
            MenuSlot.from(section(categories, "back"))
        );
    }

    public CategoryMenu category(String key) {
        return Objects.requireNonNull(categories.get(key), "No cosmetic category " + key);
    }

    private static Map<Integer, ItemBuilder> itemsBySlot(ConfigurationSection section) {
        Map<Integer, ItemBuilder> items = new LinkedHashMap<>();
        for (String slot : section.getKeys(false)) {
            items.put(Integer.parseInt(slot), ItemBuilder.from(section(section, slot)));
        }
        return items;
    }

    static ConfigurationSection section(ConfigurationSection parent, String path) {
        return Objects.requireNonNull(parent.getConfigurationSection(path), "Missing configuration section " + parent.getCurrentPath() + "." + path);
    }

    public record CategoryMenu(String key, MenuSlot button, CosmeticListSettings list) {
    }

    public record LoreTexts(
        String star,
        String starsOn,
        String starsOff,
        String ownedName,
        String lockedName,
        String ultraTag,
        List<String> selectOnly,
        List<String> select,
        List<String> selected,
        List<String> selectedWithPreview,
        List<String> buy,
        List<String> buyAffordable,
        List<String> unavailable
    ) {

        static LoreTexts from(ConfigurationSection section) {
            return new LoreTexts(
                section.getString("star", "*"),
                section.getString("stars-on", "&6&l"),
                section.getString("stars-off", "&7&l"),
                section.getString("owned-name", "&a{name}"),
                section.getString("locked-name", "&c{name}"),
                section.getString("ultra-tag", "&7Incluso no &d&lULTRA"),
                section.getStringList("select-only"),
                section.getStringList("select"),
                section.getStringList("selected"),
                section.getStringList("selected-with-preview"),
                section.getStringList("buy"),
                section.getStringList("buy-affordable"),
                section.getStringList("unavailable")
            );
        }
    }
}
