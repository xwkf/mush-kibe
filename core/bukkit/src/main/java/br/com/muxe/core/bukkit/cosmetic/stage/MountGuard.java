package br.com.muxe.core.bukkit.cosmetic.stage;

import br.com.muxe.core.bukkit.cosmetic.animation.Stage.Click;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.vehicle.VehicleExitEvent;
import org.bukkit.plugin.Plugin;

public final class MountGuard implements Listener {

    private static final Map<Plugin, MountGuard> BY_PLUGIN = new ConcurrentHashMap<>();

    private final Set<UUID> seats = ConcurrentHashMap.newKeySet();
    private final Map<UUID, Tool> tools = new ConcurrentHashMap<>();

    private MountGuard() {
    }

    public static MountGuard of(Plugin plugin) {
        return BY_PLUGIN.computeIfAbsent(plugin, owner -> {
            MountGuard guard = new MountGuard();
            owner.getServer().getPluginManager().registerEvents(guard, owner);
            return guard;
        });
    }

    void protect(Entity seat) {
        seats.add(seat.getUniqueId());
    }

    void release(Entity seat) {
        seats.remove(seat.getUniqueId());
    }

    void tool(UUID player, int slot, Consumer<Click> onClick) {
        tools.put(player, new Tool(slot, onClick));
    }

    void dropTool(UUID player) {
        tools.remove(player);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onExit(VehicleExitEvent event) {
        if (seats.contains(event.getVehicle().getUniqueId())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onDamage(EntityDamageEvent event) {
        if (seats.contains(event.getEntity().getUniqueId())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onInteract(PlayerInteractEvent event) {
        Tool tool = tools.get(event.getPlayer().getUniqueId());
        if (tool == null || event.getPlayer().getInventory().getHeldItemSlot() != tool.slot() || event.getAction() == Action.PHYSICAL) {
            return;
        }
        event.setCancelled(true);
        boolean left = event.getAction() == Action.LEFT_CLICK_AIR || event.getAction() == Action.LEFT_CLICK_BLOCK;
        tool.onClick().accept(left ? Click.LEFT : Click.RIGHT);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onDrop(PlayerDropItemEvent event) {
        Tool tool = tools.get(event.getPlayer().getUniqueId());
        if (tool != null && event.getPlayer().getInventory().getHeldItemSlot() == tool.slot()) {
            event.setCancelled(true);
        }
    }

    private record Tool(int slot, Consumer<Click> onClick) {
    }
}
