package br.com.muxe.afk.listener;

import br.com.muxe.afk.route.AfkReturner;
import br.com.muxe.core.common.activity.Displacement;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

public final class AfkMoveListener implements Listener {

    private final AfkReturner returner;

    public AfkMoveListener(AfkReturner returner) {
        this.returner = returner;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onMove(PlayerMoveEvent event) {
        Location from = event.getFrom();
        Location to = event.getTo();
        if (to != null && Displacement.between(from.getX(), from.getY(), from.getZ(), to.getX(), to.getY(), to.getZ()).walked()) {
            returner.moved(event.getPlayer().getUniqueId());
        }
    }
}
