package br.com.muxe.minigame.bedwars.lobby.settings;

import br.com.muxe.core.bukkit.npc.NpcLabelLayout;
import java.util.List;
import java.util.Objects;
import org.bukkit.configuration.ConfigurationSection;

public record StatsNpcSettings(Spot spot, List<String> lines, int barSegments, NpcLabelLayout labels) {

    public StatsNpcSettings {
        lines = List.copyOf(lines);
    }

    public static StatsNpcSettings from(ConfigurationSection section) {
        return new StatsNpcSettings(Spot.from(Objects.requireNonNull(section.getConfigurationSection("location"), "Missing lobby.stats-npc.location")),
            section.getStringList("lines"), section.getInt("bar-segments", 12), NpcLabelLayout.from(section.getConfigurationSection("labels")));
    }
}
