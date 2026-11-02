package br.com.muxe.minigame.bedwars.lobby.top;

import br.com.muxe.minigame.bedwars.lobby.settings.Spot;
import br.com.muxe.minigame.bedwars.stats.domain.StatsPeriod;
import br.com.muxe.minigame.bedwars.stats.domain.TopCategory;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import org.bukkit.configuration.ConfigurationSection;

public record TopBoardSettings(String id, TopCategory category, String title, Spot spot, List<StatsPeriod> periods, boolean modes) {

    public TopBoardSettings {
        if (periods.isEmpty()) {
            throw new IllegalArgumentException("The top " + id + " needs at least one period");
        }
        periods = List.copyOf(periods);
    }

    public static List<TopBoardSettings> from(ConfigurationSection boards) {
        List<TopBoardSettings> all = new ArrayList<>();
        for (String id : boards.getKeys(false)) {
            ConfigurationSection board = Objects.requireNonNull(boards.getConfigurationSection(id), "Missing top " + id);
            List<StatsPeriod> periods = board.getStringList("periods").stream().map(name -> StatsPeriod.valueOf(name.toUpperCase(Locale.ROOT))).toList();
            all.add(new TopBoardSettings(id, TopCategory.valueOf(board.getString("category", "").toUpperCase(Locale.ROOT)), board.getString("title", ""),
                Spot.from(Objects.requireNonNull(board.getConfigurationSection("location"), "Missing location of top " + id)), periods, board.getBoolean("modes", true)));
        }
        return all;
    }
}
