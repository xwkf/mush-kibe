package br.com.muxe.core.bukkit.world;

import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.weather.ThunderChangeEvent;
import org.bukkit.event.weather.WeatherChangeEvent;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.plugin.Plugin;

public final class ClearMidday implements Listener {

    public static final long MIDDAY = 6000L;
    public static final String DAYLIGHT_CYCLE = "doDaylightCycle";

    public static ClearMidday start(Plugin plugin) {
        ClearMidday started = new ClearMidday();
        plugin.getServer().getPluginManager().registerEvents(started, plugin);
        plugin.getServer().getWorlds().forEach(ClearMidday::apply);
        return started;
    }

    public static void apply(World world) {
        world.setGameRuleValue(DAYLIGHT_CYCLE, "false");
        world.setTime(MIDDAY);
        world.setStorm(false);
        world.setThundering(false);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onWorldLoad(WorldLoadEvent event) {
        apply(event.getWorld());
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onWeatherChange(WeatherChangeEvent event) {
        if (event.toWeatherState()) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onThunderChange(ThunderChangeEvent event) {
        if (event.toThunderState()) {
            event.setCancelled(true);
        }
    }
}
