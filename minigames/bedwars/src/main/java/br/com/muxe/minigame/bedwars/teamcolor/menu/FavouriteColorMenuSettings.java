package br.com.muxe.minigame.bedwars.teamcolor.menu;

import br.com.muxe.minigame.api.cosmetic.menu.MenuSlot;
import br.com.muxe.minigame.engine.team.TeamColor;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.bukkit.configuration.ConfigurationSection;

public record FavouriteColorMenuSettings(String title, int rows, List<String> choose, List<String> chosen, MenuSlot none,
                                         Map<TeamColor, Option> colors, MenuSlot back) {

    public FavouriteColorMenuSettings {
        choose = List.copyOf(choose);
        chosen = List.copyOf(chosen);
        colors = Collections.unmodifiableMap(new LinkedHashMap<>(colors));
    }

    public static FavouriteColorMenuSettings from(ConfigurationSection section) {
        ConfigurationSection configured = section(section, "colors");
        Map<TeamColor, Option> colors = new LinkedHashMap<>();
        for (String key : configured.getKeys(false)) {
            TeamColor color = TeamColor.find(key).orElseThrow(() -> new IllegalArgumentException("Unknown team colour '" + key + "' at " + configured.getCurrentPath()));
            ConfigurationSection option = section(configured, key);
            colors.put(color, new Option(option.getInt("slot"), option.getString("name")));
        }
        return new FavouriteColorMenuSettings(
            section.getString("title"),
            section.getInt("rows", 6),
            section.getStringList("choose"),
            section.getStringList("chosen"),
            MenuSlot.from(section(section, "none")),
            colors,
            MenuSlot.from(section(section, "back"))
        );
    }

    private static ConfigurationSection section(ConfigurationSection parent, String path) {
        return Objects.requireNonNull(parent.getConfigurationSection(path), "Missing configuration section " + parent.getCurrentPath() + "." + path);
    }

    public record Option(int slot, String name) {
    }
}
