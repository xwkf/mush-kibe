package br.com.muxe.core.bukkit.nms;

import net.minecraft.server.v1_8_R3.PacketPlayOutEntityEquipment;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public final class ArmorVisibility {

    private static final int FIRST_ARMOR_SLOT = 1;
    private static final int ARMOR_SLOTS = 4;

    private ArmorVisibility() {
    }

    public static void hide(Player viewer, Player target) {
        for (int index = 0; index < ARMOR_SLOTS; index++) {
            send(viewer, new PacketPlayOutEntityEquipment(target.getEntityId(), FIRST_ARMOR_SLOT + index, null));
        }
    }

    public static void show(Player viewer, Player target) {
        ItemStack[] armor = target.getInventory().getArmorContents();
        for (int index = 0; index < armor.length; index++) {
            send(viewer, new PacketPlayOutEntityEquipment(target.getEntityId(), FIRST_ARMOR_SLOT + index, CraftItemStack.asNMSCopy(armor[index])));
        }
    }

    private static void send(Player viewer, PacketPlayOutEntityEquipment packet) {
        if (viewer.isOnline()) {
            ((CraftPlayer) viewer).getHandle().playerConnection.sendPacket(packet);
        }
    }
}
