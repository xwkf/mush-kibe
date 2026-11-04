package br.com.muxe.lobby.scoreboard;

import br.com.muxe.lobby.profile.PlayerInfo;
import java.util.List;
import java.util.Objects;
import org.bukkit.configuration.ConfigurationSection;

public record ScoreboardTemplate(String title, List<String> lines) {

    public ScoreboardTemplate {
        Objects.requireNonNull(title, "title");
        lines = List.copyOf(lines);
    }

    public static ScoreboardTemplate from(ConfigurationSection config) {
        return new ScoreboardTemplate(config.getString("scoreboard.title", "&b&lMUXE"), config.getStringList("scoreboard.lines"));
    }

    public List<String> render(PlayerInfo info, String lobby, int players) {
        return lines.stream()
            .map(line -> line
                .replace("{tag}", info.tag())
                .replace("{credits}", Long.toString(info.credits()))
                .replace("{lobby}", lobby)
                .replace("{players}", Integer.toString(players)))
            .toList();
    }
}
