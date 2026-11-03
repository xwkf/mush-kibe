package br.com.muxe.minigame.bedwars.hotbar.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

class HotbarLayoutTest {

    private static final HotbarEntry BLOCKS = HotbarEntry.category("blocos");
    private static final HotbarEntry WOOL = HotbarEntry.item("la");
    private static final HotbarEntry PICKAXE = HotbarEntry.item("picareta");

    @Test
    void aPlayerWhoNeverEditedTheHotbarHasNineEmptySlots() {
        HotbarLayout layout = HotbarLayout.empty();

        assertThat(layout.slots()).hasSize(HotbarLayout.SLOTS);
        assertThat(layout.isEmpty()).isTrue();
        assertThat(layout.entries(0)).isEmpty();
        assertThat(layout.entries(8)).isEmpty();
    }

    @Test
    void anAddedEntryGoesToTheEndOfTheSlotInOrder() {
        HotbarLayout layout = HotbarLayout.empty().add(2, BLOCKS).add(2, WOOL).add(2, PICKAXE);

        assertThat(layout.entries(2)).containsExactly(BLOCKS, WOOL, PICKAXE);
        assertThat(layout.isEmpty()).isFalse();
        assertThat(layout.entries(1)).isEmpty();
    }

    @Test
    void addingWhatIsAlreadyInTheSlotChangesNothing() {
        HotbarLayout layout = HotbarLayout.empty().add(0, BLOCKS).add(0, WOOL);

        assertThat(layout.add(0, BLOCKS)).isSameAs(layout);
        assertThat(layout.add(0, BLOCKS).entries(0)).containsExactly(BLOCKS, WOOL);
    }

    @Test
    void anEntryIsInOneSlotOnlyAndAddingItElsewhereMovesIt() {
        HotbarLayout layout = HotbarLayout.empty().add(0, BLOCKS).add(0, WOOL).add(4, WOOL);

        assertThat(layout.entries(0)).containsExactly(BLOCKS);
        assertThat(layout.entries(4)).containsExactly(WOOL);
        assertThat(layout.slotOf(WOOL)).hasValue(4);
        assertThat(layout.contains(PICKAXE)).isFalse();
        assertThat(layout.slotOf(PICKAXE)).isEmpty();
    }

    @Test
    void aMovedEntryKeepsTheOrderOfTheOtherEntriesInBothSlots() {
        HotbarLayout layout = HotbarLayout.empty().add(0, BLOCKS).add(0, WOOL).add(0, PICKAXE).add(1, HotbarEntry.item("tnt")).add(1, WOOL);

        assertThat(layout.entries(0)).containsExactly(BLOCKS, PICKAXE);
        assertThat(layout.entries(1)).containsExactly(HotbarEntry.item("tnt"), WOOL);
    }

    @Test
    void aCategoryAndAnItemOfThatCategoryCanLiveInDifferentSlots() {
        HotbarLayout layout = HotbarLayout.empty().add(0, BLOCKS).add(1, WOOL);

        assertThat(layout.slotOf(BLOCKS)).hasValue(0);
        assertThat(layout.slotOf(WOOL)).hasValue(1);
    }

    @Test
    void removingAnEntryTakesItOutOfItsSlotAndKeepsTheRest() {
        HotbarLayout layout = HotbarLayout.empty().add(3, BLOCKS).add(3, WOOL).add(3, PICKAXE);

        assertThat(layout.remove(3, WOOL).entries(3)).containsExactly(BLOCKS, PICKAXE);
        assertThat(layout.remove(3, WOOL).contains(WOOL)).isFalse();
    }

    @Test
    void removingFromTheWrongSlotOrWhatIsNotThereChangesNothing() {
        HotbarLayout layout = HotbarLayout.empty().add(3, BLOCKS);

        assertThat(layout.remove(4, BLOCKS)).isSameAs(layout);
        assertThat(layout.remove(3, WOOL)).isSameAs(layout);
    }

    @Test
    void aSlotHoldsAtMostFourteenEntries() {
        HotbarLayout layout = HotbarLayout.empty();
        for (int index = 0; index < HotbarLayout.MAX_ENTRIES; index++) {
            layout = layout.add(0, HotbarEntry.item("item-" + index));
        }

        assertThat(layout.isFull(0)).isTrue();
        assertThat(layout.isFull(1)).isFalse();
        HotbarLayout full = layout;
        assertThatThrownBy(() -> full.add(0, WOOL)).isInstanceOf(IllegalStateException.class);
        assertThat(full.add(0, HotbarEntry.item("item-0"))).isSameAs(full);
    }

    @Test
    void aSlotAcceptsAnItemThroughTheItemOrThroughACategoryItBelongsTo() {
        HotbarLayout layout = HotbarLayout.empty().add(0, BLOCKS).add(1, PICKAXE);

        assertThat(layout.accepts(0, "argila", Set.of("blocos"))).isTrue();
        assertThat(layout.accepts(0, "tnt", Set.of("utilidades"))).isFalse();
        assertThat(layout.accepts(1, "picareta", Set.of("ferramentas"))).isTrue();
        assertThat(layout.accepts(1, "machado", Set.of("ferramentas"))).isFalse();
        assertThat(layout.accepts(2, "argila", Set.of("blocos"))).isFalse();
    }

    @Test
    void entriesTheShopNoLongerSellsAreDropped() {
        HotbarLayout layout = HotbarLayout.empty().add(0, BLOCKS).add(0, WOOL).add(1, PICKAXE);

        HotbarLayout known = layout.known(entry -> !entry.equals(WOOL));

        assertThat(known.entries(0)).containsExactly(BLOCKS);
        assertThat(known.entries(1)).containsExactly(PICKAXE);
    }

    @Test
    void aLayoutIsStoredAsNineTextsAndComesBackTheSame() {
        HotbarLayout layout = HotbarLayout.empty().add(0, BLOCKS).add(0, WOOL).add(8, PICKAXE);

        assertThat(layout.encode()).containsExactly("c:blocos,i:la", "", "", "", "", "", "", "", "i:picareta");
        assertThat(HotbarLayout.decode(layout.encode())).isEqualTo(layout);
        assertThat(HotbarLayout.decode(HotbarLayout.empty().encode())).isEqualTo(HotbarLayout.empty());
    }

    @Test
    void storedTextWithProblemsIsReadAsFarAsItMakesSense() {
        HotbarLayout layout = HotbarLayout.decode(List.of("c:blocos,garbage,i:la,c:blocos", "i:la,x:y,i:tnt", "short"));

        assertThat(layout.entries(0)).containsExactly(BLOCKS, WOOL);
        assertThat(layout.entries(1)).containsExactly(HotbarEntry.item("tnt"));
        assertThat(layout.entries(2)).isEmpty();
        assertThat(layout.entries(8)).isEmpty();
    }

    @Test
    void storedTextWithTooManyEntriesInASlotKeepsTheFirstOnes() {
        List<String> many = new ArrayList<>();
        for (int index = 0; index < HotbarLayout.MAX_ENTRIES + 3; index++) {
            many.add("i:item-" + index);
        }

        HotbarLayout layout = HotbarLayout.decode(List.of(String.join(",", many)));

        assertThat(layout.entries(0)).hasSize(HotbarLayout.MAX_ENTRIES).startsWith(HotbarEntry.item("item-0"));
    }

    @Test
    void aLayoutAlwaysHasNineSlotsWithoutRepeatedEntries() {
        assertThatThrownBy(() -> new HotbarLayout(List.of())).isInstanceOf(IllegalArgumentException.class);
        List<List<HotbarEntry>> repeated = new ArrayList<>(Collections.nCopies(HotbarLayout.SLOTS, List.<HotbarEntry>of()));
        repeated.set(0, List.of(WOOL));
        repeated.set(1, List.of(WOOL));
        assertThatThrownBy(() -> new HotbarLayout(repeated)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> HotbarLayout.empty().add(9, WOOL)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> HotbarLayout.empty().entries(-1)).isInstanceOf(IllegalArgumentException.class);
    }
}
