package br.com.muxe.minigame.api.view;

import br.com.muxe.core.bukkit.effect.SoundCue;
import br.com.muxe.core.bukkit.text.Colors;
import br.com.muxe.core.bukkit.text.Title;
import java.util.Optional;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

public final class EndTitles {

    private static final int STAY_TICKS = 20;
    private static final String SUBTITLE = " ";

    private final Plugin plugin;
    private final TitleSweep victory;
    private final TitleSweep defeat;
    private final Optional<SoundCue> victorySound;
    private final Optional<SoundCue> defeatSound;
    private final TitleRuns runs = new TitleRuns();

    public EndTitles(Plugin plugin, TitleSweep victory, TitleSweep defeat, Optional<SoundCue> victorySound, Optional<SoundCue> defeatSound) {
        this.plugin = plugin;
        this.victory = victory;
        this.defeat = defeat;
        this.victorySound = victorySound;
        this.defeatSound = defeatSound;
    }

    public static EndTitles from(Plugin plugin, ConfigurationSection section) {
        ConfigurationSection victory = section == null ? null : section.getConfigurationSection("victory");
        ConfigurationSection defeat = section == null ? null : section.getConfigurationSection("defeat");
        return new EndTitles(plugin,
            TitleSweep.from(victory, "VITÓRIA", "&6"),
            TitleSweep.from(defeat, "DERROTA", "&c"),
            SoundCue.from(victory == null ? null : victory.getConfigurationSection("sound")),
            SoundCue.from(defeat == null ? null : defeat.getConfigurationSection("sound")));
    }

    public void victory(Player player) {
        show(player, victory, victorySound);
    }

    public void defeat(Player player) {
        show(player, defeat, defeatSound);
    }

    public void stop(Player player) {
        runs.stop(player.getUniqueId());
    }

    private void show(Player player, TitleSweep sweep, Optional<SoundCue> sound) {
        Title.clear(player);
        sound.ifPresent(cue -> cue.play(player));
        Sweep run = new Sweep(player, sweep);
        run.runTaskTimer(plugin, 0L, 1L);
        runs.start(player.getUniqueId(), run.stop);
    }

    private final class Sweep extends BukkitRunnable {

        private final Player player;
        private final TitleSweep sweep;
        private final Runnable stop = this::cancel;
        private int tick;

        Sweep(Player player, TitleSweep sweep) {
            this.player = player;
            this.sweep = sweep;
        }

        @Override
        public void run() {
            Optional<String> frame = sweep.frame(tick++);
            if (frame.isEmpty() || !player.isOnline()) {
                cancel();
                runs.done(player.getUniqueId(), stop);
                return;
            }
            Title.builder().title(Colors.translate(frame.get())).subtitle(SUBTITLE).timings(0, STAY_TICKS, 0).build().send(player);
        }
    }
}
