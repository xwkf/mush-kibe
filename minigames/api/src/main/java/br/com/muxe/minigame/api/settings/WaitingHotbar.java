package br.com.muxe.minigame.api.settings;

import java.util.List;
import java.util.Objects;
import org.bukkit.configuration.ConfigurationSection;

public record WaitingHotbar(HotbarItem menu, HotbarItem leave) {

    public static WaitingHotbar from(ConfigurationSection section) {
        Objects.requireNonNull(section, "Missing configuration section hotbar.waiting");
        return new WaitingHotbar(HotbarItem.from(section.getConfigurationSection("menu")), HotbarItem.from(section.getConfigurationSection("leave")));
    }

    public List<HotbarItem> items() {
        return List.of(menu, leave);
    }
}
