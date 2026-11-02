package br.com.muxe.minigame.bedwars.lobby.settings;

import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;

public record Spot(String world, double x, double y, double z, float yaw) {

    public static Spot from(ConfigurationSection section) {
        return new Spot(section.getString("world", "lobby"), section.getDouble("x"), section.getDouble("y"), section.getDouble("z"), (float) section.getDouble("yaw"));
    }

    public Location toLocation(Server server) {
        World found = server.getWorld(world);
        return new Location(found != null ? found : server.getWorlds().getFirst(), x, y, z, yaw, 0f);
    }

    public Spot above(double height) {
        return new Spot(world, x, y + height, z, yaw);
    }
}
