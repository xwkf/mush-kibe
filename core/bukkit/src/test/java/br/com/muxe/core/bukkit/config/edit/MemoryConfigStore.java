package br.com.muxe.core.bukkit.config.edit;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

public final class MemoryConfigStore implements ConfigStore {

    private final String defaults;
    private String disk;
    private FileConfiguration config;
    private int saves;
    private int reloads;

    public MemoryConfigStore(String defaults) {
        this(defaults, defaults);
    }

    public MemoryConfigStore(String defaults, String disk) {
        this.defaults = defaults;
        this.disk = disk;
        this.config = parse();
    }

    @Override
    public FileConfiguration config() {
        return config;
    }

    @Override
    public void save() {
        saves++;
        disk = config.saveToString();
    }

    @Override
    public void reload() {
        reloads++;
        config = parse();
    }

    public int saves() {
        return saves;
    }

    public int reloads() {
        return reloads;
    }

    public String disk() {
        return disk;
    }

    public void edit(String text) {
        disk = text;
    }

    public YamlConfiguration onDisk() {
        return load(disk);
    }

    private FileConfiguration parse() {
        YamlConfiguration loaded = load(disk);
        loaded.setDefaults(load(defaults));
        return loaded;
    }

    private static YamlConfiguration load(String text) {
        YamlConfiguration yaml = new YamlConfiguration();
        try {
            yaml.loadFromString(text);
        } catch (InvalidConfigurationException exception) {
            throw new IllegalStateException(exception);
        }
        return yaml;
    }
}
