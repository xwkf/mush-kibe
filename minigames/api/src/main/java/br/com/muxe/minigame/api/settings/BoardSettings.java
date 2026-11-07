package br.com.muxe.minigame.api.settings;

import java.util.List;
import org.bukkit.configuration.ConfigurationSection;

public record BoardSettings(String title, List<String> waiting, List<String> starting, List<String> playing, List<String> playingEightTeams,
                            String event, String noEvent, String team, String you, String online, String offline, String players, String eliminated) {

    public BoardSettings {
        waiting = List.copyOf(waiting);
        starting = List.copyOf(starting);
        playing = List.copyOf(playing);
        playingEightTeams = List.copyOf(playingEightTeams);
    }

    public static BoardSettings from(ConfigurationSection section) {
        if (section == null) {
            return new BoardSettings("", List.of(), List.of(), List.of(), List.of(), "&f{event} em &a{time}", "&fFim de jogo",
                "{color}{letter} &f{name}: {status}{you}", " &7VOCÊ", "&a✔", "&e✔", "&a{count}", "&c✘");
        }
        return new BoardSettings(
            section.getString("title", ""),
            section.getStringList("waiting"),
            section.getStringList("starting"),
            section.getStringList("playing"),
            section.getStringList("playing-eight-teams"),
            section.getString("event", "&f{event} em &a{time}"),
            section.getString("no-event", "&fFim de jogo"),
            section.getString("team", "{color}{letter} &f{name}: {status}{you}"),
            section.getString("team-you", " &7VOCÊ"),
            section.getString("status-online", "&a✔"),
            section.getString("status-offline", "&e✔"),
            section.getString("status-players", "&a{count}"),
            section.getString("status-eliminated", "&c✘")
        );
    }
}
