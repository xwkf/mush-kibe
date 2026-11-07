package br.com.muxe.minigame.bedwars.cosmetic.menu;

import br.com.muxe.core.bukkit.item.ItemBuilder;
import br.com.muxe.minigame.api.cosmetic.menu.CosmeticMenuSettings;
import br.com.muxe.minigame.api.cosmetic.menu.MenuSlot;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import org.bukkit.configuration.ConfigurationSection;

public record BedWarsMenuSettings(
    String title,
    int rows,
    MenuSlot cosmetics,
    Optional<MenuSlot> missions,
    Optional<MenuSlot> quickBuy,
    Optional<MenuSlot> preferences,
    Optional<MenuSlot> hotbar,
    Map<Integer, ItemBuilder> items,
    Map<Integer, ItemBuilder> soon
) {

    public BedWarsMenuSettings {
        items = Map.copyOf(items);
        Map<Integer, ItemBuilder> stillSoon = new LinkedHashMap<>(soon);
        stillSoon.remove(cosmetics.slot());
        missions.ifPresent(button -> stillSoon.remove(button.slot()));
        quickBuy.ifPresent(button -> stillSoon.remove(button.slot()));
        preferences.ifPresent(button -> stillSoon.remove(button.slot()));
        hotbar.ifPresent(button -> stillSoon.remove(button.slot()));
        soon = Map.copyOf(stillSoon);
    }

    public static BedWarsMenuSettings from(ConfigurationSection menus) {
        ConfigurationSection bedwars = CosmeticMenuSettings.section(menus, "bedwars");
        return new BedWarsMenuSettings(
            bedwars.getString("title", "Menu do Bed Wars"),
            bedwars.getInt("rows", 3),
            MenuSlot.from(CosmeticMenuSettings.section(bedwars, "cosmetics")),
            Optional.ofNullable(bedwars.getConfigurationSection("missions")).map(MenuSlot::from),
            Optional.ofNullable(bedwars.getConfigurationSection("quick-buy")).map(MenuSlot::from),
            Optional.ofNullable(bedwars.getConfigurationSection("preferences")).map(MenuSlot::from),
            Optional.ofNullable(bedwars.getConfigurationSection("hotbar")).map(MenuSlot::from),
            CosmeticMenuSettings.itemsBySlot(CosmeticMenuSettings.section(bedwars, "items")),
            Optional.ofNullable(bedwars.getConfigurationSection("soon")).map(CosmeticMenuSettings::itemsBySlot).orElseGet(Map::of)
        );
    }
}
