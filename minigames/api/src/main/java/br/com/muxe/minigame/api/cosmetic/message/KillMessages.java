package br.com.muxe.minigame.api.cosmetic.message;

import br.com.muxe.minigame.api.cosmetic.catalog.BedCosmetic;
import br.com.muxe.minigame.api.cosmetic.catalog.CosmeticCatalog;
import br.com.muxe.minigame.api.cosmetic.profile.CosmeticWallet;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.random.RandomGenerator;

public final class KillMessages {

    private final MessageSets sets;
    private final CosmeticCatalog catalog;
    private final Function<UUID, Optional<CosmeticWallet>> wallets;
    private final MessageAssignments assignments = new MessageAssignments();

    public KillMessages(MessageSets sets, CosmeticCatalog catalog, Function<UUID, Optional<CosmeticWallet>> wallets) {
        this.sets = sets;
        this.catalog = catalog;
        this.wallets = wallets;
    }

    public MessageSet of(UUID player, String room, RandomGenerator random) {
        return wallets.apply(player).map(wallet -> chosen(player, room, wallet, random)).orElse(sets.fallback());
    }

    public void forget(UUID player) {
        assignments.forget(player);
    }

    private MessageSet chosen(UUID player, String room, CosmeticWallet wallet, RandomGenerator random) {
        if (CosmeticWallet.RANDOM.equals(wallet.selected())) {
            return assignments.of(player, room, () -> wallet.drawn(catalog, random).flatMap(this::set).orElse(sets.fallback()));
        }
        return wallet.chosen(catalog).flatMap(this::set).orElse(sets.fallback());
    }

    private Optional<MessageSet> set(BedCosmetic cosmetic) {
        return sets.find(cosmetic.id());
    }
}
