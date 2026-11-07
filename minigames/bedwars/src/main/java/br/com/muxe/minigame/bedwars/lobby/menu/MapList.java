package br.com.muxe.minigame.bedwars.lobby.menu;

import br.com.muxe.core.common.cosmetic.domain.CosmeticOrder;
import br.com.muxe.minigame.api.network.ArenaInfo;
import java.text.Normalizer;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

public final class MapList {

    private MapList() {
    }

    public static List<ArenaInfo> order(List<ArenaInfo> arenas, Set<String> favourites, Optional<String> search) {
        Optional<String> wanted = search.map(MapList::plain).filter(text -> !text.isBlank());
        return arenas.stream()
            .filter(arena -> wanted.map(text -> plain(arena.displayName()).contains(text) || plain(arena.name()).contains(text)).orElse(true))
            .sorted(CosmeticOrder.ownedFirst(arena -> favourites.contains(arena.name()), ArenaInfo::displayName))
            .toList();
    }

    static String plain(String text) {
        return Normalizer.normalize(text, Normalizer.Form.NFD).replaceAll("\\p{M}", "").toLowerCase(Locale.ROOT).trim();
    }
}
