package br.com.muxe.minigame.api.cosmetic.preview;

import br.com.muxe.core.bukkit.cosmetic.animation.Area;
import br.com.muxe.core.bukkit.cosmetic.animation.Scene;
import br.com.muxe.core.bukkit.cosmetic.animation.Vec;
import br.com.muxe.core.common.config.source.DurationParser;
import br.com.muxe.minigame.api.settings.HotbarItem;
import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
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
    List<String> lines,
    Optional<Target> target,
    Optional<PreviewKit> kit,
    Optional<Vec> middle
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
            scoreboard.getStringList("lines"),
            Optional.ofNullable(section.getConfigurationSection("target"))
                .map(target -> new Target(target.getDouble("distance", 4), target.getString("action-bar", "&eAcerte o jogador!"),
                    target.getStringList("names"), target.getString("weapon", "WOOD_SWORD"))),
            PreviewKit.from(section),
            Optional.ofNullable(section.getConfigurationSection("middle")).map(middle -> new Vec(middle.getDouble("x"), 0, middle.getDouble("z")))
        );
    }

    public Vec origin() {
        return new Vec(x, y, z);
    }

    public Area area(Vec origin) {
        Area around = Area.around(origin, Scene.REACH);
        return middle.map(at -> around.middleAt(at.x(), at.z())).orElse(around);
    }

    public Vec targetAt() {
        double distance = target.map(Target::distance).orElse(0.0);
        return origin().add(facing().multiply(distance));
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

    public boolean usesKit(boolean rightClick, int heldSlot) {
        return rightClick && heldSlot != back.slot() && kit.map(items -> items.holds(heldSlot)).orElse(false);
    }

    public List<String> lines(String name) {
        return lines.stream().map(line -> line.replace("{name}", name)).toList();
    }

    public record Target(double distance, String actionBar, List<String> names, String weapon) {

        public static final String DEFAULT_NAME = "&7Jogador";

        public Target {
            names = List.copyOf(names);
        }

        public String name(int roll) {
            return names.isEmpty() ? DEFAULT_NAME : names.get(Math.floorMod(roll, names.size()));
        }
    }

    private static ConfigurationSection section(ConfigurationSection parent, String path) {
        return Objects.requireNonNull(parent.getConfigurationSection(path), "Missing configuration section " + parent.getCurrentPath() + "." + path);
    }
}
