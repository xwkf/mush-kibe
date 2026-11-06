package br.com.muxe.minigame.bedwars.cosmetic.trail;

import br.com.muxe.minigame.bedwars.cosmetic.catalog.CosmeticCatalog;
import br.com.muxe.minigame.bedwars.cosmetic.profile.CosmeticWallet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.random.RandomGenerator;

public final class TrailCosmetics {

    private final CosmeticCatalog catalog;
    private final Function<UUID, Optional<CosmeticWallet>> wallets;
    private final Function<String, Optional<ProjectileTrail>> trails;
    private final TrailAssignments assignments = new TrailAssignments();

    public TrailCosmetics(CosmeticCatalog catalog, Function<UUID, Optional<CosmeticWallet>> wallets, Function<String, Optional<ProjectileTrail>> trails) {
        this.catalog = catalog;
        this.wallets = wallets;
        this.trails = trails;
    }

    public Optional<ProjectileTrail> of(UUID player, String room, RandomGenerator random) {
        return wallets.apply(player).flatMap(wallet ->
            assignments.of(player, room, () -> wallet.animation(catalog, random).flatMap(trails)));
    }

    public Optional<ProjectileTrail> find(String id) {
        return trails.apply(id);
    }

    public void retain(Set<UUID> players) {
        assignments.retain(players);
    }

    public void forget(UUID player) {
        assignments.forget(player);
    }
}
