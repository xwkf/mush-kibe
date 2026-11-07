package br.com.muxe.minigame.api.settings;

import br.com.muxe.core.bukkit.effect.SoundCue;
import java.util.Optional;
import org.bukkit.configuration.ConfigurationSection;

public record MatchSounds(
    Optional<SoundCue> countdown,
    Optional<SoundCue> eliminated,
    Optional<SoundCue> teamEliminated,
    Optional<SoundCue> matchEnd,
    Optional<SoundCue> matchEndFirework
) {

    public static MatchSounds from(ConfigurationSection section) {
        if (section == null) {
            return new MatchSounds(Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty());
        }
        return new MatchSounds(
            SoundCue.from(section.getConfigurationSection("countdown")),
            SoundCue.from(section.getConfigurationSection("eliminated")),
            SoundCue.from(section.getConfigurationSection("team-eliminated")),
            SoundCue.from(section.getConfigurationSection("match-end")),
            SoundCue.from(section.getConfigurationSection("match-end-firework"))
        );
    }
}
