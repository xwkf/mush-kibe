package br.com.muxe.minigame.bedwars.cosmetic.aura;

import br.com.muxe.minigame.bedwars.cosmetic.catalog.CosmeticCatalog;
import br.com.muxe.minigame.bedwars.cosmetic.profile.CosmeticWallet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.random.RandomGenerator;

public final class AuraCosmetics {

    private final CosmeticCatalog catalog;
    private final Function<UUID, Optional<CosmeticWallet>> wallets;
    private final AuraAssignments assignments = new AuraAssignments();

    public AuraCosmetics(CosmeticCatalog catalog, Function<UUID, Optional<CosmeticWallet>> wallets) {
        this.catalog = catalog;
        this.wallets = wallets;
    }

    public Optional<Aura> of(UUID player, String room, RandomGenerator random) {
        return wallets.apply(player).flatMap(wallet ->
            assignments.of(player, room, () -> wallet.animation(catalog, random).flatMap(Auras::find)));
    }

    public void retain(Set<UUID> players) {
        assignments.retain(players);
    }
}
