package br.com.muxe.minigame.bedwars.hotbar.menu;

import br.com.muxe.core.bukkit.item.ItemBuilder;
import br.com.muxe.minigame.bedwars.hotbar.HotbarChoices;
import br.com.muxe.minigame.bedwars.hotbar.domain.HotbarEntry;
import br.com.muxe.minigame.bedwars.shop.ShopCatalog;
import br.com.muxe.minigame.bedwars.shop.ShopCatalog.Category;
import br.com.muxe.minigame.bedwars.shop.ShopCatalog.ShopItem;
import br.com.muxe.minigame.bedwars.shop.ShopCatalog.Tab;
import br.com.muxe.minigame.bedwars.shop.ShopLook;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.Material;

public final class HotbarIcons {

    private final ShopCatalog shop;
    private final HotbarChoices choices;
    private final HotbarMenuSettings.Adding settings;

    public HotbarIcons(ShopCatalog shop, HotbarChoices choices, HotbarMenuSettings.Adding settings) {
        this.shop = shop;
        this.choices = choices;
        this.settings = settings;
    }

    public ItemBuilder icon(HotbarEntry entry) {
        return switch (entry.kind()) {
            case CATEGORY -> categoryIcon(category(entry));
            case ITEM -> ShopLook.icon(item(entry), "{name}");
        };
    }

    public String name(HotbarEntry entry) {
        return switch (entry.kind()) {
            case CATEGORY -> category(entry).name();
            case ITEM -> item(entry).editor().name();
        };
    }

    public ItemBuilder described(HotbarEntry entry, String hint) {
        String shown = switch (entry.kind()) {
            case CATEGORY -> settings.categoryName().replace("{name}", name(entry));
            case ITEM -> settings.itemName().replace("{name}", name(entry));
        };
        List<String> description = switch (entry.kind()) {
            case CATEGORY -> settings.categoryDescription();
            case ITEM -> item(entry).editor().lore();
        };
        return icon(entry).name(shown).lore(described(description, hint));
    }

    static List<String> described(List<String> description, String hint) {
        List<String> lore = new ArrayList<>();
        lore.add(" ");
        if (!description.isEmpty()) {
            lore.addAll(description);
            lore.add(" ");
        }
        lore.add(hint);
        return lore;
    }

    private ItemBuilder categoryIcon(Category category) {
        Material material = shop.tabs().stream()
            .filter(tab -> tab.category().equals(category.id()))
            .map(Tab::material)
            .findFirst()
            .orElse(Material.CHEST);
        return ItemBuilder.of(material);
    }

    private Category category(HotbarEntry entry) {
        return choices.category(entry.id()).orElseThrow(() -> new IllegalArgumentException("Unknown hotbar category " + entry.id()));
    }

    private ShopItem item(HotbarEntry entry) {
        return choices.item(entry.id()).orElseThrow(() -> new IllegalArgumentException("Unknown hotbar item " + entry.id()));
    }
}
