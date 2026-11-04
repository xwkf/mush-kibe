package br.com.muxe.afk.config;

import br.com.muxe.core.common.config.source.DurationParser;
import java.time.Duration;
import org.bukkit.configuration.ConfigurationSection;

public record AfkServerSettings(String fallbackGroup, Duration arrivalGrace, Duration retryAfter) {

    public static AfkServerSettings from(ConfigurationSection config) {
        return new AfkServerSettings(
            config.getString("return.fallback-group", "lobby"),
            DurationParser.parse(config.getString("return.arrival-grace", "1s")),
            DurationParser.parse(config.getString("return.retry-after", "5s"))
        );
    }
}
