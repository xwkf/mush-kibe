package br.com.muxe.minigame.bedwars.listener;

import br.com.muxe.minigame.bedwars.game.Activity;
import br.com.muxe.minigame.bedwars.game.Slingshot;
import java.time.Clock;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public final class ActivityListener implements Listener {

    private final Activity activity;
    private final Slingshot slingshot;
    private final Clock clock;

    public ActivityListener(Activity activity, Slingshot slingshot, Clock clock) {
        this.activity = activity;
        this.slingshot = slingshot;
        this.clock = clock;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onJoin(PlayerJoinEvent event) {
        activity.active(event.getPlayer().getUniqueId(), clock.instant());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onQuit(PlayerQuitEvent event) {
        activity.forget(event.getPlayer().getUniqueId());
        slingshot.forget(event.getPlayer().getUniqueId());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onMove(PlayerMoveEvent event) {
        activity.active(event.getPlayer().getUniqueId(), clock.instant());
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onInteract(PlayerInteractEvent event) {
        activity.active(event.getPlayer().getUniqueId(), clock.instant());
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onChat(AsyncPlayerChatEvent event) {
        activity.active(event.getPlayer().getUniqueId(), clock.instant());
    }
}
