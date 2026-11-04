package br.com.muxe.minigame.bedwars.cosmetic.message;

import br.com.muxe.minigame.bedwars.cosmetic.catalog.BedCosmetic;
import br.com.muxe.minigame.bedwars.cosmetic.menu.CosmeticDescriptions;
import java.util.List;

public final class MessageDescriptions implements CosmeticDescriptions {

    private final MessageSets sets;
    private final MessagePreview preview;

    public MessageDescriptions(MessageSets sets, MessagePreview preview) {
        this.sets = sets;
        this.preview = preview;
    }

    @Override
    public List<String> of(BedCosmetic cosmetic, String viewerName) {
        return sets.find(cosmetic.id()).map(set -> preview.lines(set, viewerName)).orElse(cosmetic.description());
    }
}
