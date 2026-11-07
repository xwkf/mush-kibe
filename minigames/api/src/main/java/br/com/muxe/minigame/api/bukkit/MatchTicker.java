package br.com.muxe.minigame.api.bukkit;

import br.com.muxe.minigame.api.runtime.MatchService;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

public final class MatchTicker {

    public static final long SECOND_TICKS = 20L;
    public static final long FAST_TICKS = 5L;

    private final List<BukkitTask> tasks = new ArrayList<>();

    private MatchTicker() {
    }

    public static MatchTicker start(Plugin plugin, MatchService<?, ?> matches) {
        MatchTicker ticker = new MatchTicker();
        ticker.tasks.add(plugin.getServer().getScheduler().runTaskTimer(plugin, matches::second, SECOND_TICKS, SECOND_TICKS));
        ticker.tasks.add(plugin.getServer().getScheduler().runTaskTimer(plugin, matches::fastTick, FAST_TICKS, FAST_TICKS));
        return ticker;
    }

    public void stop() {
        tasks.forEach(BukkitTask::cancel);
        tasks.clear();
    }
}
