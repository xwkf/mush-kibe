package br.com.muxe.minigame.api.cosmetic.menu;

import br.com.muxe.core.bukkit.item.ItemBuilder;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import org.bukkit.configuration.ConfigurationSection;

public record CosmeticMenuSettings(
    String categoriesTitle,
    int categoriesRows,
    Map<String, CategoryMenu> categories,
    Optional<MenuSlot> teamColor,
    Map<Integer, ItemBuilder> soon,
    MenuSlot categoriesBack
) {

    public CosmeticMenuSettings {
        categories = Collections.unmodifiableMap(new LinkedHashMap<>(categories));
        Map<Integer, ItemBuilder> categorySoon = new LinkedHashMap<>(soon);
        categories.values().forEach(category -> categorySoon.remove(category.button().slot()));
        teamColor.ifPresent(button -> categorySoon.remove(button.slot()));
        soon = Map.copyOf(categorySoon);
    }

    public static CosmeticMenuSettings from(ConfigurationSection menus, String baseList, List<String> keys) {
        ConfigurationSection categories = section(menus, "categories");
        CosmeticListSettings base = CosmeticListSettings.from(section(menus, baseList));
        Map<String, CategoryMenu> byKey = new LinkedHashMap<>();
        for (String key : keys) {
            ConfigurationSection button = categories.getConfigurationSection(key);
            if (button != null) {
                CosmeticListSettings list = key.equals(baseList) ? base : base.over(Optional.ofNullable(menus.getConfigurationSection(key)));
                byKey.put(key, new CategoryMenu(key, MenuSlot.from(button), list));
            }
        }
        return new CosmeticMenuSettings(
            categories.getString("title", "Cosméticos"),
            categories.getInt("rows", 6),
            byKey,
            Optional.ofNullable(categories.getConfigurationSection("team-color")).map(MenuSlot::from),
            itemsBySlot(section(categories, "soon")),
            MenuSlot.from(section(categories, "back"))
        );
    }

    public CategoryMenu category(String key) {
        return Objects.requireNonNull(categories.get(key), "No cosmetic category " + key);
    }

    public static Map<Integer, ItemBuilder> itemsBySlot(ConfigurationSection section) {
        Map<Integer, ItemBuilder> items = new LinkedHashMap<>();
        for (String slot : section.getKeys(false)) {
            items.put(Integer.parseInt(slot), ItemBuilder.from(section(section, slot)));
        }
        return items;
    }

    public static ConfigurationSection section(ConfigurationSection parent, String path) {
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
        String vipTag,
        String ultraTag,
        String ultraPlusTag,
        List<String> header,
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
                section.getString("vip-tag", "&7Incluso no &a&lVIP"),
                section.getString("ultra-tag", "&7Incluso no &d&lULTRA"),
                section.getString("ultra-plus-tag", "&7Incluso no &d&lULTRA&6+"),
                section.getStringList("header"),
                section.getStringList("select-only"),
                section.getStringList("select"),
                section.getStringList("selected"),
                section.getStringList("selected-with-preview"),
                section.getStringList("buy"),
                section.getStringList("buy-affordable"),
                section.getStringList("unavailable")
            );
        }

        LoreTexts over(ConfigurationSection section) {
            return new LoreTexts(
                section.getString("star", star),
                section.getString("stars-on", starsOn),
                section.getString("stars-off", starsOff),
                section.getString("owned-name", ownedName),
                section.getString("locked-name", lockedName),
                section.getString("vip-tag", vipTag),
                section.getString("ultra-tag", ultraTag),
                section.getString("ultra-plus-tag", ultraPlusTag),
                lines(section, "header", header),
                lines(section, "select-only", selectOnly),
                lines(section, "select", select),
                lines(section, "selected", selected),
                lines(section, "selected-with-preview", selectedWithPreview),
                lines(section, "buy", buy),
                lines(section, "buy-affordable", buyAffordable),
                lines(section, "unavailable", unavailable)
            );
        }

        private static List<String> lines(ConfigurationSection section, String path, List<String> inherited) {
            return section.isList(path) ? section.getStringList(path) : inherited;
        }
    }
}
