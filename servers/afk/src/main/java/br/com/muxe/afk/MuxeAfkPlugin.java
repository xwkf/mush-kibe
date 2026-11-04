package br.com.muxe.afk;

import br.com.muxe.afk.bootstrap.AfkBootstrap;
import java.util.logging.Level;
import org.bukkit.plugin.java.JavaPlugin;

public final class MuxeAfkPlugin extends JavaPlugin {

    @Override
    public void onEnable() {
        try {
            new AfkBootstrap(this).enable();
        } catch (RuntimeException exception) {
            getLogger().log(Level.SEVERE, "Muxe-AFK failed to start", exception);
            getServer().getPluginManager().disablePlugin(this);
        }
    }
}
