package br.com.muxe.core.bukkit.config.edit;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.bukkit.configuration.ConfigurationSection;

public final class ConfigReloads {

    private final List<Entry> entries = new ArrayList<>();

    public ConfigReloads register(String name, String prefix, Reloadable reloadable) {
        entries.add(new Entry(name, prefix, reloadable));
        return this;
    }

    public boolean covers(String path) {
        return entries.stream().anyMatch(entry -> ConfigPaths.covers(entry.prefix(), path));
    }

    public List<String> names() {
        return entries.stream().map(Entry::name).toList();
    }

    public ReloadReport apply(Collection<String> paths, ConfigurationSection config) {
        return run(entries.stream().filter(entry -> paths.stream().anyMatch(path -> ConfigPaths.covers(entry.prefix(), path))).toList(), config);
    }

    public ReloadReport applyAll(ConfigurationSection config) {
        return run(entries, config);
    }

    private static ReloadReport run(List<Entry> chosen, ConfigurationSection config) {
        List<String> applied = new ArrayList<>();
        Map<String, String> failed = new LinkedHashMap<>();
        for (Entry entry : chosen) {
            try {
                entry.reloadable().reload(config);
                applied.add(entry.name());
            } catch (RuntimeException exception) {
                failed.put(entry.name(), reason(exception));
            }
        }
        return new ReloadReport(applied, failed);
    }

    private static String reason(RuntimeException exception) {
        return exception.getMessage() == null ? exception.getClass().getSimpleName() : exception.getMessage();
    }

    private record Entry(String name, String prefix, Reloadable reloadable) {
    }
}
