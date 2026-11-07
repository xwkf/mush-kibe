package br.com.muxe.minigame.api.cosmetic.message;

import br.com.muxe.minigame.api.cosmetic.catalog.Cosmetic;
import br.com.muxe.minigame.api.cosmetic.menu.CosmeticDescriptions;
import java.util.List;

public final class MessageDescriptions implements CosmeticDescriptions {

    private final MessageSets sets;
    private final MessagePreview preview;

    public MessageDescriptions(MessageSets sets, MessagePreview preview) {
        this.sets = sets;
        this.preview = preview;
    }

    @Override
    public List<String> of(Cosmetic cosmetic, String viewerName) {
        return sets.find(cosmetic.id()).map(set -> preview.lines(set, viewerName)).orElse(cosmetic.description());
    }
}
