package br.com.muxe.core.bukkit.effect;

import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

public record SoundCue(Sound sound, float volume, float pitch) {

    public SoundCue {
        Objects.requireNonNull(sound, "sound");
    }

    public static Optional<SoundCue> from(ConfigurationSection section) {
        if (section == null) {
            return Optional.empty();
        }
        String name = section.getString("sound");
        if (name == null || name.isBlank()) {
            return Optional.empty();
        }
        try {
            Sound sound = Sound.valueOf(name.trim().toUpperCase(Locale.ROOT));
            return Optional.of(new SoundCue(sound, number(section, "volume"), number(section, "pitch")));
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException("Unknown sound '" + name + "' at " + section.getCurrentPath(), exception);
        }
    }

    private static float number(ConfigurationSection section, String key) {
        return section.contains(key) ? (float) section.getDouble(key) : 1f;
    }

    public void play(Player player) {
        player.playSound(player.getLocation(), sound, volume, pitch);
    }
}
