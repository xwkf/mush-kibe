package br.com.muxe.afk.listener;

import br.com.muxe.core.bukkit.buildmode.BuildMode;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.weather.WeatherChangeEvent;

public final class AfkProtectionListener implements Listener {

    private final BuildMode build;

    public AfkProtectionListener(BuildMode build) {
        this.build = build;
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onHunger(FoodLevelChangeEvent event) {
        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onBreak(BlockBreakEvent event) {
        onlyWhileBuilding(event.getPlayer(), event);
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onPlace(BlockPlaceEvent event) {
        onlyWhileBuilding(event.getPlayer(), event);
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onInteract(PlayerInteractEvent event) {
        onlyWhileBuilding(event.getPlayer(), event);
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onDrop(PlayerDropItemEvent event) {
        onlyWhileBuilding(event.getPlayer(), event);
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onPickup(PlayerPickupItemEvent event) {
        onlyWhileBuilding(event.getPlayer(), event);
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onInventoryClick(InventoryClickEvent event) {
        onlyWhileBuilding(event.getWhoClicked(), event);
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        if (event.getSpawnReason() != SpawnReason.CUSTOM) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onWeather(WeatherChangeEvent event) {
        if (event.toWeatherState()) {
            event.setCancelled(true);
        }
    }

    private void onlyWhileBuilding(HumanEntity entity, Cancellable event) {
        if (!(entity instanceof Player player) || !build.isBuilding(player)) {
            event.setCancelled(true);
        }
    }
}
