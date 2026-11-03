package br.com.muxe.minigame.bedwars.hotbar;

import br.com.muxe.minigame.bedwars.hotbar.domain.HotbarEntry;
import br.com.muxe.minigame.bedwars.shop.ShopCatalog;
import br.com.muxe.minigame.bedwars.shop.ShopCatalog.Category;
import br.com.muxe.minigame.bedwars.shop.ShopCatalog.ShopItem;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public final class HotbarChoices {

    private final ShopCatalog shop;
    private final List<Category> categories;
    private final List<ShopItem> items;

    public HotbarChoices(ShopCatalog shop) {
        this.shop = shop;
        this.categories = shop.categories().values().stream()
            .filter(category -> category.items().stream().anyMatch(id -> hotbarItem(shop.items().get(id))))
            .toList();
        this.items = categories.stream()
            .flatMap(category -> category.items().stream())
            .distinct()
            .map(shop.items()::get)
            .filter(HotbarChoices::hotbarItem)
            .toList();
    }

    public List<Category> categories() {
        return categories;
    }

    public List<ShopItem> items() {
        return items;
    }

    public Optional<Category> category(String id) {
        return categories.stream().filter(category -> category.id().equals(id)).findFirst();
    }

    public Optional<ShopItem> item(String id) {
        return items.stream().filter(item -> item.id().equals(id)).findFirst();
    }

    public boolean exists(HotbarEntry entry) {
        return switch (entry.kind()) {
            case CATEGORY -> category(entry.id()).isPresent();
            case ITEM -> item(entry.id()).isPresent();
        };
    }

    public Set<String> categoriesOf(String itemId) {
        return shop.categories().values().stream()
            .filter(category -> category.items().contains(itemId))
            .map(Category::id)
            .collect(Collectors.toUnmodifiableSet());
    }

    private static boolean hotbarItem(ShopItem item) {
        return item.type() != ShopCatalog.Type.ARMOR;
    }
}
