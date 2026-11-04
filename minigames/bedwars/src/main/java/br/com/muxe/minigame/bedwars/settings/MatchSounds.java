package br.com.muxe.minigame.bedwars.settings;

import br.com.muxe.core.bukkit.effect.SoundCue;
import java.util.Optional;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

public record MatchSounds(
    Optional<SoundCue> countdown,
    Optional<SoundCue> bought,
    Optional<SoundCue> upgrade,
    Optional<SoundCue> cannotAfford,
    Optional<SoundCue> enderChest,
    Optional<SoundCue> deposited,
    Optional<SoundCue> bedDestroyed,
    Optional<SoundCue> bedLost
) {

    public static MatchSounds from(ConfigurationSection section) {
        if (section == null) {
            return new MatchSounds(Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty());
        }
        return new MatchSounds(
            SoundCue.from(section.getConfigurationSection("countdown")),
            SoundCue.from(section.getConfigurationSection("bought")),
            SoundCue.from(section.getConfigurationSection("upgrade")),
            SoundCue.from(section.getConfigurationSection("cannot-afford")),
            SoundCue.from(section.getConfigurationSection("ender-chest")),
            SoundCue.from(section.getConfigurationSection("deposited")),
            SoundCue.from(section.getConfigurationSection("bed-destroyed")),
            SoundCue.from(section.getConfigurationSection("bed-lost"))
        );
    }

    public static void play(Optional<SoundCue> cue, Player player) {
        cue.ifPresent(sound -> sound.play(player));
    }
}
