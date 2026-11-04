package br.com.muxe.minigame.bedwars.cosmetic.menu;

import br.com.muxe.minigame.bedwars.cosmetic.catalog.BedCosmetic;
import br.com.muxe.minigame.bedwars.cosmetic.catalog.CosmeticCatalog;
import br.com.muxe.minigame.bedwars.cosmetic.menu.CosmeticMenuSettings.CategoryMenu;
import br.com.muxe.minigame.bedwars.cosmetic.menu.CosmeticMenus.PreviewGate;
import br.com.muxe.minigame.bedwars.cosmetic.profile.CosmeticProfiles;
import br.com.muxe.minigame.bedwars.cosmetic.profile.CosmeticWallet;
import java.util.List;

public record CosmeticCategory(CategoryMenu menu, CosmeticCatalog catalog, CosmeticProfiles profiles, CosmeticIcons icons, PreviewGate previews, CosmeticDescriptions descriptions) {

    public static CosmeticCategory of(CategoryMenu menu, CosmeticCatalog catalog, CosmeticProfiles profiles, PreviewGate previews) {
        return of(menu, catalog, profiles, previews, CosmeticDescriptions.CONFIGURED);
    }

    public static CosmeticCategory of(CategoryMenu menu, CosmeticCatalog catalog, CosmeticProfiles profiles, PreviewGate previews, CosmeticDescriptions descriptions) {
        return new CosmeticCategory(menu, catalog, profiles, new CosmeticIcons(menu, catalog), previews, descriptions);
    }

    public String key() {
        return menu.key();
    }

    public CosmeticListSettings list() {
        return menu.list();
    }

    public List<BedCosmetic> ordered(CosmeticWallet wallet) {
        return list().entries().ownedFirst() ? wallet.sorted(catalog) : catalog.all();
    }
}
