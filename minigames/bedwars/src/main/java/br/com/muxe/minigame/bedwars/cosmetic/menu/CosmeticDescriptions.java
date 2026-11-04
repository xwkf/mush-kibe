package br.com.muxe.minigame.bedwars.cosmetic.menu;

import br.com.muxe.minigame.bedwars.cosmetic.catalog.BedCosmetic;
import java.util.List;

@FunctionalInterface
public interface CosmeticDescriptions {

    CosmeticDescriptions CONFIGURED = (cosmetic, viewerName) -> cosmetic.description();

    List<String> of(BedCosmetic cosmetic, String viewerName);
}
