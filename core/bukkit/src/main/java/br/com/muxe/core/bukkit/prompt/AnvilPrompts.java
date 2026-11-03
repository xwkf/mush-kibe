package br.com.muxe.core.bukkit.prompt;

import br.com.muxe.core.bukkit.item.ItemBuilder;
import br.com.muxe.core.bukkit.text.Colors;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import net.minecraft.server.v1_8_R3.BlockPosition;
import net.minecraft.server.v1_8_R3.ChatComponentText;
import net.minecraft.server.v1_8_R3.ContainerAnvil;
import net.minecraft.server.v1_8_R3.EntityHuman;
import net.minecraft.server.v1_8_R3.EntityPlayer;
import net.minecraft.server.v1_8_R3.PacketPlayOutOpenWindow;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;

public final class AnvilPrompts implements Listener {

    public static final int RESULT_SLOT = 2;

    private final Plugin plugin;
    private final Map<UUID, Session> sessions = new ConcurrentHashMap<>();

    private AnvilPrompts(Plugin plugin) {
        this.plugin = plugin;
    }

    public static AnvilPrompts start(Plugin plugin) {
        AnvilPrompts started = new AnvilPrompts(plugin);
        plugin.getServer().getPluginManager().registerEvents(started, plugin);
        return started;
    }

    public void ask(Player player, String placeholder, Consumer<String> onAnswer) {
        player.closeInventory();
        EntityPlayer handle = ((CraftPlayer) player).getHandle();
        ContainerAnvil container = new ContainerAnvil(handle.inventory, handle.world, new BlockPosition(0, 0, 0), handle) {
            @Override
            public boolean a(EntityHuman human) {
                return true;
            }
        };
        container.checkReachable = false;
        int windowId = handle.nextContainerCounter();
        handle.playerConnection.sendPacket(new PacketPlayOutOpenWindow(windowId, "minecraft:anvil", new ChatComponentText(""), 0));
        handle.activeContainer = container;
        container.windowId = windowId;
        container.addSlotListener(handle);
        Inventory top = container.getBukkitView().getTopInventory();
        String placeholderText = Colors.translate(placeholder);
        top.setItem(0, ItemBuilder.of(Material.PAPER).name(placeholderText).build());
        sessions.put(player.getUniqueId(), new Session(top, placeholderText, onAnswer));
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }
        Session session = sessions.get(player.getUniqueId());
        if (session == null || !session.inventory().equals(event.getView().getTopInventory())) {
            return;
        }
        event.setCancelled(true);
        if (event.getRawSlot() != RESULT_SLOT) {
            return;
        }
        Optional<String> text = AnvilText.typed(name(event.getCurrentItem()), session.placeholder());
        if (text.isEmpty()) {
            return;
        }
        sessions.remove(player.getUniqueId());
        session.inventory().clear();
        plugin.getServer().getScheduler().runTask(plugin, () -> {
            player.closeInventory();
            if (player.isOnline()) {
                session.onAnswer().accept(text.get());
            }
        });
    }

    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        Session session = sessions.get(event.getPlayer().getUniqueId());
        if (session != null && session.inventory().equals(event.getView().getTopInventory())) {
            sessions.remove(event.getPlayer().getUniqueId());
            session.inventory().clear();
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Session session = sessions.remove(event.getPlayer().getUniqueId());
        if (session != null) {
            session.inventory().clear();
        }
    }

    private static String name(ItemStack item) {
        if (item == null || !item.hasItemMeta()) {
            return null;
        }
        ItemMeta meta = item.getItemMeta();
        return meta.hasDisplayName() ? meta.getDisplayName() : null;
    }

    private record Session(Inventory inventory, String placeholder, Consumer<String> onAnswer) {
    }
}
