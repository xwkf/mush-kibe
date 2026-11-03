package br.com.muxe.minigame.bedwars.shop;

import java.util.List;
import java.util.Optional;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public final class ResourcePool {

    private final List<Inventory> sources;

    public ResourcePool(List<Inventory> sources) {
        this.sources = List.copyOf(sources);
    }

    public int count(Material material) {
        return sources.stream().mapToInt(source -> count(source, material)).sum();
    }

    public boolean take(Material material, int amount) {
        Optional<Payment> payment = Payment.plan(amount, sources.stream().map(source -> count(source, material)).toList());
        if (payment.isEmpty()) {
            return false;
        }
        for (int index = 0; index < sources.size(); index++) {
            remove(sources.get(index), material, payment.get().takes().get(index));
        }
        return true;
    }

    static int count(Inventory inventory, Material material) {
        int total = 0;
        for (ItemStack item : inventory.getContents()) {
            if (item != null && item.getType() == material) {
                total += item.getAmount();
            }
        }
        return total;
    }

    static void remove(Inventory inventory, Material material, int amount) {
        int left = amount;
        ItemStack[] contents = inventory.getContents();
        for (int slot = 0; slot < contents.length && left > 0; slot++) {
            ItemStack item = contents[slot];
            if (item == null || item.getType() != material) {
                continue;
            }
            if (item.getAmount() <= left) {
                left -= item.getAmount();
                inventory.setItem(slot, null);
            } else {
                item.setAmount(item.getAmount() - left);
                inventory.setItem(slot, item);
                left = 0;
            }
        }
    }
}
