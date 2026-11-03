package br.com.muxe.core.bukkit.config;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public final class PluginConfig {

    private PluginConfig() {
    }

    public static FileConfiguration load(JavaPlugin plugin) {
        plugin.saveDefaultConfig();
        plugin.getConfig().options().copyDefaults(true);
        plugin.saveConfig();
        plugin.reloadConfig();
        return plugin.getConfig();
    }

    public static YamlConfiguration completed(YamlConfiguration onDisk) throws InvalidConfigurationException {
        onDisk.options().copyDefaults(true);
        YamlConfiguration completed = new YamlConfiguration();
        completed.loadFromString(onDisk.saveToString());
        return completed;
    }
}
