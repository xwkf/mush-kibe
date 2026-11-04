package br.com.muxe.lobby.afk;

import br.com.muxe.core.common.config.source.DurationParser;
import java.time.Duration;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

public record AfkSettings(boolean enabled, Duration idleAfter, String serverGroup, Duration checkInterval, Duration originTtl) {

    public static AfkSettings from(ConfigurationSection section) {
        ConfigurationSection afk = section == null ? new YamlConfiguration() : section;
        return new AfkSettings(
            afk.getBoolean("enabled", true),
            DurationParser.parse(afk.getString("idle-after", "5m")),
            afk.getString("server-group", "afk"),
            DurationParser.parse(afk.getString("check-interval", "1s")),
            DurationParser.parse(afk.getString("origin-ttl", "12h"))
        );
    }
}
