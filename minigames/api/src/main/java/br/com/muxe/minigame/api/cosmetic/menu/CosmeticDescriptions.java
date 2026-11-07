package br.com.muxe.minigame.api.cosmetic.menu;

import br.com.muxe.minigame.api.cosmetic.catalog.Cosmetic;
import java.util.List;

@FunctionalInterface
public interface CosmeticDescriptions {

    CosmeticDescriptions CONFIGURED = (cosmetic, viewerName) -> cosmetic.description();

    List<String> of(Cosmetic cosmetic, String viewerName);
}
