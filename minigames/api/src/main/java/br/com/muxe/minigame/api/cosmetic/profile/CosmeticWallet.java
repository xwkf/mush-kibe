package br.com.muxe.minigame.api.cosmetic.profile;

import br.com.muxe.core.common.cosmetic.domain.CosmeticOrder;
import br.com.muxe.minigame.api.cosmetic.catalog.BedCosmetic;
import br.com.muxe.minigame.api.cosmetic.catalog.CosmeticCatalog;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.random.RandomGenerator;

public record CosmeticWallet(Set<String> owned, String selected, long credits, CosmeticAccess access) {

    public static final String RANDOM = "aleatorio";
    public static final String NONE = "nenhuma";
    public static final CosmeticWallet EMPTY = new CosmeticWallet(Set.of(), RANDOM, 0, CosmeticAccess.REGULAR);

    public CosmeticWallet {
        owned = Set.copyOf(owned);
        selected = selected == null ? RANDOM : selected;
    }

    public CosmeticWallet(Set<String> owned, String selected, long credits, boolean ultra) {
        this(owned, selected, credits, ultra ? CosmeticAccess.ULTRA : CosmeticAccess.REGULAR);
    }

    public static CosmeticWallet loaded(Set<String> owned, String selected, long credits, CosmeticAccess access, CosmeticCatalog catalog) {
        Set<String> all = new HashSet<>(owned);
        if (access == CosmeticAccess.ALL) {
            catalog.all().forEach(cosmetic -> all.add(cosmetic.id()));
        }
        return new CosmeticWallet(all, selected, credits, access);
    }

    public boolean owns(BedCosmetic cosmetic) {
        return owned.contains(cosmetic.id()) || access.covers(cosmetic.availability());
    }

    public boolean usable(BedCosmetic cosmetic) {
        return cosmetic.animated() && owns(cosmetic);
    }

    public CosmeticState state(BedCosmetic cosmetic) {
        if (usable(cosmetic)) {
            return cosmetic.id().equals(selected) ? CosmeticState.SELECTED : CosmeticState.OWNED;
        }
        if (!owns(cosmetic) && cosmetic.forSale()) {
            return CosmeticState.BUY;
        }
        return CosmeticState.UNAVAILABLE;
    }

    public long ownedCount(CosmeticCatalog catalog) {
        return catalog.all().stream().filter(this::owns).count();
    }

    public List<BedCosmetic> sorted(CosmeticCatalog catalog) {
        return catalog.all().stream().sorted(CosmeticOrder.ownedFirst(this::owns, BedCosmetic::name)).toList();
    }

    public Optional<String> animation(CosmeticCatalog catalog, RandomGenerator random) {
        if (NONE.equals(selected)) {
            return Optional.empty();
        }
        Optional<BedCosmetic> chosen = catalog.find(selected).filter(this::usable);
        if (chosen.isPresent()) {
            return chosen.map(BedCosmetic::id);
        }
        List<BedCosmetic> usable = catalog.all().stream().filter(this::usable).toList();
        if (usable.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(usable.get(random.nextInt(usable.size())).id());
    }

    public Optional<BedCosmetic> chosen(CosmeticCatalog catalog) {
        return catalog.find(selected).filter(this::usable).or(() -> catalog.defaultCosmetic().filter(this::usable));
    }

    public Optional<BedCosmetic> drawn(CosmeticCatalog catalog, RandomGenerator random) {
        List<BedCosmetic> usable = catalog.all().stream().filter(this::usable).toList();
        return usable.isEmpty() ? Optional.empty() : Optional.of(usable.get(random.nextInt(usable.size())));
    }

    public CosmeticWallet selecting(String id) {
        return new CosmeticWallet(owned, id, credits, access);
    }

    public CosmeticWallet withCredits(long balance) {
        return new CosmeticWallet(owned, selected, balance, access);
    }

    public CosmeticWallet buying(String id, long balance) {
        Set<String> more = new HashSet<>(owned);
        more.add(id);
        return new CosmeticWallet(more, selected, balance, access);
    }
}
