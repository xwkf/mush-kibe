package br.com.muxe.minigame.bedwars.shop;

import br.com.muxe.core.bukkit.item.ItemBuilder;
import br.com.muxe.minigame.bedwars.shop.ShopCatalog.ShopItem;

public final class ShopLook {

    private ShopLook() {
    }

    public static ItemBuilder icon(ShopItem item, String nameTemplate) {
        boolean dataMatters = item.type() == ShopCatalog.Type.POTION || item.type() == ShopCatalog.Type.SPECIAL;
        ItemBuilder icon = ItemBuilder.of(item.editor().material(), dataMatters ? item.data() : 0)
            .amount(item.amount())
            .name(nameTemplate.replace("{name}", item.editor().name()));
        return !item.enchantments().isEmpty() || item.type() == ShopCatalog.Type.POTION ? icon.glow() : icon;
    }
}
