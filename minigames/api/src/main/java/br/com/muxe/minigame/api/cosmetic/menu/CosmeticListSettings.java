package br.com.muxe.minigame.api.cosmetic.menu;

import br.com.muxe.core.bukkit.item.ItemBuilder;
import br.com.muxe.minigame.api.cosmetic.menu.CosmeticMenuSettings.LoreTexts;
import java.util.List;
import java.util.Optional;
import org.bukkit.configuration.ConfigurationSection;

public record CosmeticListSettings(
    String title,
    int rows,
    List<Integer> slots,
    ItemBuilder none,
    ItemBuilder random,
    MenuSlot back,
    MenuSlot previous,
    MenuSlot next,
    MenuSlot selected,
    MenuSlot shop,
    LoreTexts lore,
    Entries entries
) {

    public CosmeticListSettings {
        slots = List.copyOf(slots);
    }

    static CosmeticListSettings from(ConfigurationSection section) {
        return new CosmeticListSettings(
            section.getString("title", "Destruição de Cama ({page}/{pages})"),
            section.getInt("rows", 6),
            section.getIntegerList("slots"),
            ItemBuilder.from(CosmeticMenuSettings.section(section, "none")),
            ItemBuilder.from(CosmeticMenuSettings.section(section, "random")),
            MenuSlot.from(CosmeticMenuSettings.section(section, "back")),
            MenuSlot.from(CosmeticMenuSettings.section(section, "previous")),
            MenuSlot.from(CosmeticMenuSettings.section(section, "next")),
            MenuSlot.from(CosmeticMenuSettings.section(section, "selected")),
            MenuSlot.from(CosmeticMenuSettings.section(section, "shop")),
            LoreTexts.from(CosmeticMenuSettings.section(section, "lore")),
            Entries.DEFAULT.over(Optional.ofNullable(section.getConfigurationSection("entries")))
        );
    }

    CosmeticListSettings over(Optional<ConfigurationSection> section) {
        if (section.isEmpty()) {
            return this;
        }
        ConfigurationSection own = section.get();
        return new CosmeticListSettings(
            own.getString("title", title),
            own.getInt("rows", rows),
            own.isList("slots") ? own.getIntegerList("slots") : slots,
            item(own, "none").orElse(none),
            item(own, "random").orElse(random),
            slot(own, "back").orElse(back),
            slot(own, "previous").orElse(previous),
            slot(own, "next").orElse(next),
            slot(own, "selected").orElse(selected),
            slot(own, "shop").orElse(shop),
            Optional.ofNullable(own.getConfigurationSection("lore")).map(lore::over).orElse(lore),
            entries.over(Optional.ofNullable(own.getConfigurationSection("entries")))
        );
    }

    private static Optional<ItemBuilder> item(ConfigurationSection section, String path) {
        return Optional.ofNullable(section.getConfigurationSection(path)).map(ItemBuilder::from);
    }

    private static Optional<MenuSlot> slot(ConfigurationSection section, String path) {
        return Optional.ofNullable(section.getConfigurationSection(path)).map(MenuSlot::from);
    }

    public record Entries(boolean none, boolean random, int randomAfter, boolean ownedFirst, boolean previews, boolean selectedIcon) {

        public static final Entries DEFAULT = new Entries(true, true, 0, true, true, false);

        public Entries {
            if (randomAfter < 0) {
                throw new IllegalArgumentException("random-after cannot be negative");
            }
        }

        Entries over(Optional<ConfigurationSection> section) {
            if (section.isEmpty()) {
                return this;
            }
            ConfigurationSection own = section.get();
            return new Entries(
                own.getBoolean("none", none),
                own.getBoolean("random", random),
                own.getInt("random-after", randomAfter),
                own.getBoolean("owned-first", ownedFirst),
                own.getBoolean("previews", previews),
                own.getBoolean("selected-icon", selectedIcon)
            );
        }

        public int entryIndex(int cosmeticIndex, int cosmetics) {
            int before = none ? 1 : 0;
            if (random && Math.min(randomAfter, cosmetics) <= cosmeticIndex) {
                before++;
            }
            return cosmeticIndex + before;
        }
    }
}
