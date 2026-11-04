package br.com.muxe.core.bukkit.config.edit;

import java.io.File;
import java.util.concurrent.Executor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public final class PluginConfigStore implements ConfigStore {

    private final JavaPlugin plugin;
    private final LatestWriter writer;

    public PluginConfigStore(JavaPlugin plugin, Executor io) {
        this.plugin = plugin;
        this.writer = new LatestWriter(new File(plugin.getDataFolder(), "config.yml").toPath(), io, plugin.getLogger());
    }

    @Override
    public FileConfiguration config() {
        return plugin.getConfig();
    }

    @Override
    public void save() {
        writer.write(plugin.getConfig().saveToString());
    }

    @Override
    public void reload() {
        writer.flush();
        plugin.reloadConfig();
    }

    public void flush() {
        writer.flush();
    }
}
