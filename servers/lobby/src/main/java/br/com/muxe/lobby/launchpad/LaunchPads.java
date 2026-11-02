package br.com.muxe.lobby.launchpad;

import br.com.muxe.core.bukkit.api.MuxeCore;
import br.com.muxe.lobby.config.LobbySettings;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class LaunchPads {

    private LaunchPads() {
    }

    public static void start(JavaPlugin plugin, MuxeCore core, LaunchPadSettings settings) {
        if (!settings.enabled()) {
            return;
        }
        PadIndex index = new PadIndex();
        PadScanner scanner = new PadScanner(index, settings.block(), core.async(), core.mainThread(), plugin.getLogger());
        PluginManager plugins = plugin.getServer().getPluginManager();
        plugins.registerEvents(scanner, plugin);
        plugins.registerEvents(new LaunchPadListener(settings.block(), settings.launch(), new LaunchCooldown(core.clock(), settings.cooldown()), core.buildMode(), settings.sound()), plugin);
        scanner.scanLoaded(plugin.getServer().getWorlds());
        long period = LobbySettings.ticks(settings.particleInterval());
        plugin.getServer().getScheduler().runTaskTimer(plugin, new PadParticles(plugin.getServer(), index, settings.particlesPerBlock(), settings.viewRadius()), period, period);
    }
}
