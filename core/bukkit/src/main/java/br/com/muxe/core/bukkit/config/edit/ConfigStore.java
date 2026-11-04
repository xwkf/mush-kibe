package br.com.muxe.core.bukkit.config.edit;

import org.bukkit.configuration.file.FileConfiguration;

public interface ConfigStore {

    FileConfiguration config();

    void save();

    void reload();
}
