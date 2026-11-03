package br.com.muxe.minigame.bedwars.cosmetic.preview;

import br.com.muxe.core.bukkit.cosmetic.animation.Vec;
import br.com.muxe.core.common.config.source.DurationParser;
import br.com.muxe.minigame.bedwars.settings.BedWarsSettings.HotbarItem;
import java.time.Duration;
import java.util.List;
import java.util.Objects;
import org.bukkit.configuration.ConfigurationSection;

public record ScenePreviewSettings(
    String world,
    double x,
    double y,
    double z,
    float yaw,
    float pitch,
    List<String> countdown,
    int countdownTicks,
    Duration timeout,
    Duration returnDelay,
    HotbarItem back,
    String title,
    List<String> lines
) {

    public ScenePreviewSettings {
        countdown = List.copyOf(countdown);
        lines = List.copyOf(lines);
    }

    public static ScenePreviewSettings from(ConfigurationSection section) {
        ConfigurationSection spawn = section(section, "spawn");
        ConfigurationSection scoreboard = section(section, "scoreboard");
        ConfigurationSection back = section(section, "back-item");
        return new ScenePreviewSettings(
            section.getString("world", "previa_efeito_de_vitoria"),
            spawn.getDouble("x"),
            spawn.getDouble("y"),
            spawn.getDouble("z"),
            (float) spawn.getDouble("yaw"),
            (float) spawn.getDouble("pitch"),
            section.getStringList("countdown"),
            section.getInt("countdown-ticks", 20),
            DurationParser.parse(section.getString("timeout", "60s")),
            DurationParser.parse(section.getString("return-delay", "1s")),
            new HotbarItem(back.getInt("slot"), back.getString("material"), back.getString("name")),
            scoreboard.getString("title", "&b&lMUXE"),
            scoreboard.getStringList("lines")
        );
    }

    public Vec origin() {
        return new Vec(x, y, z);
    }

    public Vec facing() {
        double radians = Math.toRadians(yaw);
        return new Vec(-Math.sin(radians), 0, Math.cos(radians));
    }

    public int sceneStart() {
        return countdown.size() * countdownTicks;
    }

    public boolean goesBack(boolean rightClick, int heldSlot) {
        return rightClick && heldSlot == back.slot();
    }

    public List<String> lines(String name) {
        return lines.stream().map(line -> line.replace("{name}", name)).toList();
    }

    private static ConfigurationSection section(ConfigurationSection parent, String path) {
        return Objects.requireNonNull(parent.getConfigurationSection(path), "Missing configuration section " + parent.getCurrentPath() + "." + path);
    }
}
