package br.com.muxe.minigame.bedwars.listener;

import br.com.muxe.minigame.api.match.MatchPhase;
import br.com.muxe.minigame.api.runtime.MatchRuntime;
import br.com.muxe.minigame.api.runtime.Matches;
import br.com.muxe.minigame.bedwars.room.BedWarsMatch;
import java.util.Optional;
import org.bukkit.Location;
import org.bukkit.block.Chest;
import org.bukkit.block.DoubleChest;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

public final class ChestListener implements Listener {

    private final Matches<BedWarsMatch> matches;
    private final BedWarsHandlers handlers;

    public ChestListener(Matches<BedWarsMatch> matches, BedWarsHandlers handlers) {
        this.matches = matches;
        this.handlers = handlers;
    }

    @EventHandler(ignoreCancelled = true)
    public void onStoreClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player) || !storage(player, event.getView().getTopInventory())) {
            return;
        }
        Inventory top = event.getView().getTopInventory();
        boolean intoTop = event.getClickedInventory() != null && event.getClickedInventory().equals(top);
        ItemStack moving = null;
        ItemStack cursor = null;
        if (event.isShiftClick() && !intoTop) {
            moving = event.getCurrentItem();
        } else if (intoTop && event.getClick() == ClickType.NUMBER_KEY) {
            moving = player.getInventory().getItem(event.getHotbarButton());
        } else if (intoTop) {
            moving = event.getCursor();
            cursor = moving;
        }
        if (moving != null && !handlers.mayStore(player, moving, cursor)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onStoreDrag(InventoryDragEvent event) {
        if (!(event.getWhoClicked() instanceof Player player) || !storage(player, event.getView().getTopInventory())) {
            return;
        }
        int topSize = event.getView().getTopInventory().getSize();
        if (event.getRawSlots().stream().anyMatch(slot -> slot < topSize) && !handlers.mayStore(player, event.getOldCursor(), event.getOldCursor())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onChestOpen(InventoryOpenEvent event) {
        if (!(event.getPlayer() instanceof Player player)) {
            return;
        }
        InventoryHolder holder = event.getInventory().getHolder();
        Location chest;
        if (holder instanceof Chest single) {
            chest = single.getLocation().add(0.5, 0, 0.5);
        } else if (holder instanceof DoubleChest twin) {
            chest = twin.getLocation();
        } else {
            return;
        }
        matches.of(player.getUniqueId())
            .filter(runtime -> runtime.match().phase() == MatchPhase.PLAYING)
            .filter(runtime -> !runtime.state().mayOpenChest(player, chest))
            .ifPresent(runtime -> event.setCancelled(true));
    }

    private boolean storage(Player player, Inventory top) {
        Optional<MatchRuntime<BedWarsMatch>> runtime = matches.of(player.getUniqueId());
        if (runtime.isEmpty() || runtime.get().match().phase() != MatchPhase.PLAYING) {
            return false;
        }
        InventoryHolder holder = top.getHolder();
        return holder instanceof Chest || holder instanceof DoubleChest || top.equals(runtime.get().state().enderChest(player));
    }
}
