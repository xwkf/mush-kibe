package br.com.muxe.minigame.api.settings;

import java.util.List;
import org.bukkit.configuration.ConfigurationSection;

public record Teleporter(String title, List<String> entryLore) {

    public Teleporter {
        entryLore = List.copyOf(entryLore);
    }

    public static Teleporter from(ConfigurationSection section) {
        return new Teleporter(section == null ? "Teleportador" : section.getString("title", "Teleportador"),
            section == null ? List.of() : section.getStringList("entry-lore"));
    }
}
