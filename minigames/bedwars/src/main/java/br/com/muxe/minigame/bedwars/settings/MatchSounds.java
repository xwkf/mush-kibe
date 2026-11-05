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
    Optional<SoundCue> bedLost,
    Optional<SoundCue> finalKill,
    Optional<SoundCue> eliminated,
    Optional<SoundCue> teamEliminated,
    Optional<SoundCue> matchEnd,
    Optional<SoundCue> matchEndFirework
) {

    public static MatchSounds from(ConfigurationSection section) {
        if (section == null) {
            return new MatchSounds(Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(),
                Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty());
        }
        return new MatchSounds(
            SoundCue.from(section.getConfigurationSection("countdown")),
            SoundCue.from(section.getConfigurationSection("bought")),
            SoundCue.from(section.getConfigurationSection("upgrade")),
            SoundCue.from(section.getConfigurationSection("cannot-afford")),
            SoundCue.from(section.getConfigurationSection("ender-chest")),
            SoundCue.from(section.getConfigurationSection("deposited")),
            SoundCue.from(section.getConfigurationSection("bed-destroyed")),
            SoundCue.from(section.getConfigurationSection("bed-lost")),
            SoundCue.from(section.getConfigurationSection("final-kill")),
            SoundCue.from(section.getConfigurationSection("eliminated")),
            SoundCue.from(section.getConfigurationSection("team-eliminated")),
            SoundCue.from(section.getConfigurationSection("match-end")),
            SoundCue.from(section.getConfigurationSection("match-end-firework"))
        );
    }

    public static void play(Optional<SoundCue> cue, Player player) {
        cue.ifPresent(sound -> sound.play(player));
    }
}
