package br.com.muxe.minigame.bedwars.hotbar.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.OptionalInt;
import java.util.Set;
import org.junit.jupiter.api.Test;

class HotbarPlacementTest {

    private static final Set<String> BLOCKS = Set.of("blocos");

    private static List<HotbarContent> hotbar() {
        return new ArrayList<>(Collections.nCopies(HotbarLayout.SLOTS, HotbarContent.EMPTY));
    }

    @Test
    void withoutALayoutNothingIsPlaced() {
        assertThat(HotbarPlacement.slotFor(HotbarLayout.empty(), "argila", BLOCKS, 16, hotbar())).isEmpty();
    }

    @Test
    void anItemGoesToTheSlotThatListsItsCategory() {
        HotbarLayout layout = HotbarLayout.empty().add(4, HotbarEntry.category("blocos"));

        assertThat(HotbarPlacement.slotFor(layout, "argila", BLOCKS, 16, hotbar())).hasValue(4);
    }

    @Test
    void anItemGoesToTheSlotThatListsTheItemItself() {
        HotbarLayout layout = HotbarLayout.empty().add(7, HotbarEntry.item("argila"));

        assertThat(HotbarPlacement.slotFor(layout, "argila", BLOCKS, 16, hotbar())).hasValue(7);
        assertThat(HotbarPlacement.slotFor(layout, "la", BLOCKS, 16, hotbar())).isEmpty();
    }

    @Test
    void anItemOfAnotherCategoryIsNotPlaced() {
        HotbarLayout layout = HotbarLayout.empty().add(0, HotbarEntry.category("combate"));

        assertThat(HotbarPlacement.slotFor(layout, "argila", BLOCKS, 16, hotbar())).isEmpty();
    }

    @Test
    void whenSeveralSlotsMatchTheFirstOneWins() {
        HotbarLayout layout = HotbarLayout.empty().add(6, HotbarEntry.category("blocos")).add(2, HotbarEntry.item("argila"));

        assertThat(HotbarPlacement.slotFor(layout, "argila", BLOCKS, 16, hotbar())).hasValue(2);
    }

    @Test
    void anOccupiedSlotWithAnotherItemIsSkippedForTheNextMatchingSlot() {
        HotbarLayout layout = HotbarLayout.empty().add(0, HotbarEntry.category("blocos")).add(3, HotbarEntry.item("argila"));
        List<HotbarContent> hotbar = hotbar();
        hotbar.set(0, HotbarContent.OTHER);

        assertThat(HotbarPlacement.slotFor(layout, "argila", BLOCKS, 16, hotbar)).hasValue(3);
    }

    @Test
    void whenEveryMatchingSlotIsOccupiedTheItemFallsBack() {
        HotbarLayout layout = HotbarLayout.empty().add(0, HotbarEntry.category("blocos")).add(3, HotbarEntry.item("argila"));
        List<HotbarContent> hotbar = hotbar();
        hotbar.set(0, HotbarContent.OTHER);
        hotbar.set(3, HotbarContent.OTHER);

        assertThat(HotbarPlacement.slotFor(layout, "argila", BLOCKS, 16, hotbar)).isEmpty();
    }

    @Test
    void aSlotAlreadyHoldingTheSameItemTakesMoreWhileItFits() {
        HotbarLayout layout = HotbarLayout.empty().add(1, HotbarEntry.category("blocos"));
        List<HotbarContent> hotbar = hotbar();
        hotbar.set(1, HotbarContent.same(48, 64));

        assertThat(HotbarPlacement.slotFor(layout, "argila", BLOCKS, 16, hotbar)).hasValue(1);
    }

    @Test
    void aSlotThatCannotTakeTheWholeStackIsSkipped() {
        HotbarLayout layout = HotbarLayout.empty().add(1, HotbarEntry.category("blocos")).add(5, HotbarEntry.item("argila"));
        List<HotbarContent> hotbar = hotbar();
        hotbar.set(1, HotbarContent.same(49, 64));

        assertThat(HotbarPlacement.slotFor(layout, "argila", BLOCKS, 16, hotbar)).hasValue(5);
    }

    @Test
    void aSlotHoldingAToolThatDoesNotStackIsNeverReused() {
        HotbarLayout layout = HotbarLayout.empty().add(0, HotbarEntry.category("combate"));
        List<HotbarContent> hotbar = hotbar();
        hotbar.set(0, HotbarContent.same(1, 1));

        assertThat(HotbarPlacement.slotFor(layout, "espada-pedra", Set.of("combate"), 1, hotbar)).isEmpty();
    }

    @Test
    void anEmptyMatchingSlotAfterAnOccupiedOneIsUsed() {
        HotbarLayout layout = HotbarLayout.empty().add(0, HotbarEntry.category("combate")).add(1, HotbarEntry.item("espada-pedra"));
        List<HotbarContent> hotbar = hotbar();
        hotbar.set(0, HotbarContent.OTHER);

        assertThat(HotbarPlacement.slotFor(layout, "espada-pedra", Set.of("combate"), 1, hotbar)).isEqualTo(OptionalInt.of(1));
    }

    @Test
    void theHotbarMustHaveNineSlots() {
        assertThatThrownBy(() -> HotbarPlacement.slotFor(HotbarLayout.empty(), "la", BLOCKS, 16, List.of())).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void emptySameAndOtherContentsDecideWhetherAStackFits() {
        assertThat(HotbarContent.EMPTY.canTake(64)).isTrue();
        assertThat(HotbarContent.OTHER.canTake(1)).isFalse();
        assertThat(HotbarContent.same(60, 64).canTake(4)).isTrue();
        assertThat(HotbarContent.same(60, 64).canTake(5)).isFalse();
    }
}
