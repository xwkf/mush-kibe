package br.com.muxe.lobby.launchpad;

import br.com.muxe.core.common.config.source.DurationParser;
import java.time.Duration;
import java.util.Locale;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;

public record LaunchPadSettings(
    boolean enabled,
    Material block,
    double distance,
    double height,
    Duration cooldown,
    Duration particleInterval,
    int particlesPerBlock,
    double viewRadius
) {

    public static final double DEFAULT_DISTANCE = 20;
    public static final double DEFAULT_HEIGHT = 5;

    public static LaunchPadSettings from(ConfigurationSection section) {
        if (section == null) {
            return new LaunchPadSettings(false, Material.SLIME_BLOCK, DEFAULT_DISTANCE, DEFAULT_HEIGHT, Duration.ofSeconds(1), Duration.ofMillis(250), 1, 32);
        }
        return new LaunchPadSettings(
            section.getBoolean("enabled", true),
            Material.valueOf(section.getString("block", Material.SLIME_BLOCK.name()).toUpperCase(Locale.ROOT)),
            section.getDouble("distance", DEFAULT_DISTANCE),
            section.getDouble("height", DEFAULT_HEIGHT),
            DurationParser.parse(section.getString("cooldown", "1s")),
            DurationParser.parse(section.getString("particles.interval", "250ms")),
            section.getInt("particles.per-block", 1),
            section.getDouble("particles.view-radius", 32)
        );
    }

    public LaunchVelocity.Launch launch() {
        return LaunchVelocity.solve(distance, height);
    }
}
