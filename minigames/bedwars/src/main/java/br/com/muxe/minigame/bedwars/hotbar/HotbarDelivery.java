package br.com.muxe.minigame.bedwars.hotbar;

import br.com.muxe.minigame.bedwars.hotbar.domain.HotbarContent;
import br.com.muxe.minigame.bedwars.hotbar.domain.HotbarLayout;
import br.com.muxe.minigame.bedwars.hotbar.domain.HotbarPlacement;
import java.util.ArrayList;
import java.util.List;
import java.util.OptionalInt;
import java.util.Set;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

public final class HotbarDelivery {

    private final HotbarStore hotbars;
    private final HotbarChoices choices;

    public HotbarDelivery(HotbarStore hotbars, HotbarChoices choices) {
        this.hotbars = hotbars;
        this.choices = choices;
    }

    public void give(Player player, String itemId, ItemStack stack) {
        PlayerInventory inventory = player.getInventory();
        HotbarLayout layout = hotbars.of(player.getUniqueId());
        if (layout.isEmpty()) {
            inventory.addItem(stack);
            return;
        }
        Set<String> categories = choices.categoriesOf(itemId);
        OptionalInt slot = HotbarPlacement.slotFor(layout, itemId, categories, stack.getAmount(), contents(inventory, stack));
        if (slot.isEmpty()) {
            inventory.addItem(stack);
            return;
        }
        ItemStack held = inventory.getItem(slot.getAsInt());
        if (held == null || held.getType() == Material.AIR) {
            inventory.setItem(slot.getAsInt(), stack);
            return;
        }
        ItemStack merged = held.clone();
        merged.setAmount(held.getAmount() + stack.getAmount());
        inventory.setItem(slot.getAsInt(), merged);
    }

    private static List<HotbarContent> contents(PlayerInventory inventory, ItemStack incoming) {
        List<HotbarContent> contents = new ArrayList<>(HotbarLayout.SLOTS);
        for (int slot = 0; slot < HotbarLayout.SLOTS; slot++) {
            ItemStack held = inventory.getItem(slot);
            if (held == null || held.getType() == Material.AIR) {
                contents.add(HotbarContent.EMPTY);
            } else if (held.isSimilar(incoming)) {
                contents.add(HotbarContent.same(held.getAmount(), held.getMaxStackSize()));
            } else {
                contents.add(HotbarContent.OTHER);
            }
        }
        return contents;
    }
}
