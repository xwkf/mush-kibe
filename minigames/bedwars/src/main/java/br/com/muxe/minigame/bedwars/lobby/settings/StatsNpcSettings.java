package br.com.muxe.minigame.bedwars.lobby.settings;

import java.util.List;
import java.util.Objects;
import org.bukkit.configuration.ConfigurationSection;

public record StatsNpcSettings(Spot spot, List<String> lines, int barSegments) {

    public StatsNpcSettings {
        lines = List.copyOf(lines);
    }

    public static StatsNpcSettings from(ConfigurationSection section) {
        return new StatsNpcSettings(Spot.from(Objects.requireNonNull(section.getConfigurationSection("location"), "Missing lobby.stats-npc.location")),
            section.getStringList("lines"), section.getInt("bar-segments", 12));
    }
}
