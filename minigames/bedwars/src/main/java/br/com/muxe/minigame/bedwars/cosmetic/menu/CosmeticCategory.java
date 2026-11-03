package br.com.muxe.minigame.bedwars.cosmetic.menu;

import br.com.muxe.minigame.bedwars.cosmetic.catalog.CosmeticCatalog;
import br.com.muxe.minigame.bedwars.cosmetic.menu.CosmeticMenuSettings.CategoryMenu;
import br.com.muxe.minigame.bedwars.cosmetic.menu.CosmeticMenus.PreviewGate;
import br.com.muxe.minigame.bedwars.cosmetic.profile.CosmeticProfiles;

public record CosmeticCategory(CategoryMenu menu, CosmeticCatalog catalog, CosmeticProfiles profiles, CosmeticIcons icons, PreviewGate previews) {

    public static CosmeticCategory of(CategoryMenu menu, CosmeticCatalog catalog, CosmeticProfiles profiles, PreviewGate previews) {
        return new CosmeticCategory(menu, catalog, profiles, new CosmeticIcons(menu, catalog), previews);
    }

    public String key() {
        return menu.key();
    }

    public CosmeticListSettings list() {
        return menu.list();
    }
}
