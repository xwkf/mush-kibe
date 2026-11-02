package br.com.muxe.minigame.bedwars.lobby.top;

import br.com.muxe.core.bukkit.api.MuxeCore;
import br.com.muxe.minigame.bedwars.stats.application.query.FindTopQuery;
import br.com.muxe.minigame.bedwars.stats.domain.StatsPeriod;
import br.com.muxe.minigame.bedwars.stats.domain.TopCategory;
import br.com.muxe.minigame.bedwars.stats.domain.TopEntry;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import org.bukkit.plugin.Plugin;

public final class TopData {

    public static final int SIZE = FindTopQuery.MAX_LIMIT;

    private final Plugin plugin;
    private final MuxeCore core;
    private final Duration maxAge;
    private final Map<Key, List<TopEntry>> tops = new ConcurrentHashMap<>();
    private final Map<Key, Instant> loadedAt = new ConcurrentHashMap<>();
    private final Set<Key> loading = ConcurrentHashMap.newKeySet();
    private volatile Runnable onChange = () -> {
    };

    public TopData(Plugin plugin, MuxeCore core, Duration maxAge) {
        this.plugin = plugin;
        this.core = core;
        this.maxAge = maxAge;
    }

    public void onChange(Runnable listener) {
        this.onChange = listener;
    }

    public List<TopEntry> get(TopCategory category, String mode, StatsPeriod period) {
        Key key = new Key(category, mode, period);
        Instant at = loadedAt.get(key);
        if (at == null || at.plus(maxAge).isBefore(core.clock().instant())) {
            load(key);
        }
        return tops.getOrDefault(key, List.of());
    }

    private void load(Key key) {
        if (!loading.add(key)) {
            return;
        }
        core.queries().ask(new FindTopQuery(key.category(), key.mode(), key.period(), SIZE)).whenCompleteAsync((entries, error) -> {
            loading.remove(key);
            loadedAt.put(key, core.clock().instant());
            if (error != null) {
                plugin.getLogger().log(Level.FINE, "Could not load the top " + key, error);
                return;
            }
            tops.put(key, List.copyOf(entries));
            onChange.run();
        }, core.mainThread());
    }

    private record Key(TopCategory category, String mode, StatsPeriod period) {
    }
}
