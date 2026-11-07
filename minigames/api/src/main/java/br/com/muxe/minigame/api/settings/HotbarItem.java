package br.com.muxe.minigame.api.settings;

import java.util.Locale;
import java.util.Objects;
import org.bukkit.configuration.ConfigurationSection;

public record HotbarItem(int slot, String material, String name) {

    public static HotbarItem from(ConfigurationSection section) {
        Objects.requireNonNull(section, "Missing hotbar item");
        return new HotbarItem(section.getInt("slot"), section.getString("material", "STONE").toUpperCase(Locale.ROOT), section.getString("name", ""));
    }
}
