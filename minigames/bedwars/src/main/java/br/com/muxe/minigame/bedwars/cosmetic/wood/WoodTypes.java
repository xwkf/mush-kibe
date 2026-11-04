package br.com.muxe.minigame.bedwars.cosmetic.wood;

import br.com.muxe.minigame.bedwars.cosmetic.catalog.BedCosmetic;
import br.com.muxe.minigame.bedwars.cosmetic.catalog.CosmeticCatalog;
import br.com.muxe.minigame.bedwars.cosmetic.profile.CosmeticWallet;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;

public final class WoodTypes {

    public static final String ITEM_ID = "madeira";

    private final CosmeticCatalog catalog;
    private final Function<UUID, Optional<CosmeticWallet>> wallets;

    public WoodTypes(CosmeticCatalog catalog, Function<UUID, Optional<CosmeticWallet>> wallets) {
        this.catalog = catalog;
        this.wallets = wallets;
    }

    public WoodChoice of(UUID player) {
        return wallets.apply(player)
            .flatMap(wallet -> wallet.chosen(catalog))
            .or(catalog::defaultCosmetic)
            .map(BedCosmetic::icon)
            .map(WoodChoice::of)
            .orElse(WoodChoice.OAK);
    }
}
