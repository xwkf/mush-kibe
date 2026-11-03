package br.com.muxe.minigame.bedwars.hotbar.domain;

import java.util.List;
import java.util.OptionalInt;
import java.util.Set;

public final class HotbarPlacement {

    private HotbarPlacement() {
    }

    public static OptionalInt slotFor(HotbarLayout layout, String itemId, Set<String> categoryIds, int amount, List<HotbarContent> hotbar) {
        if (hotbar.size() != HotbarLayout.SLOTS) {
            throw new IllegalArgumentException("The hotbar has " + HotbarLayout.SLOTS + " slots, got " + hotbar.size());
        }
        for (int slot = 0; slot < HotbarLayout.SLOTS; slot++) {
            if (layout.accepts(slot, itemId, categoryIds) && hotbar.get(slot).canTake(amount)) {
                return OptionalInt.of(slot);
            }
        }
        return OptionalInt.empty();
    }
}
