package br.com.muxe.core.bukkit.config.edit;

import org.bukkit.configuration.ConfigurationSection;

@FunctionalInterface
public interface Reloadable {

    void reload(ConfigurationSection config);
}
