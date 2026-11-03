package br.com.muxe.minigame.bedwars.quickbuy.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

class QuickBuyTest {

    private final QuickBuy start = QuickBuy.of(List.of("argila", "tnt", "la"));

    @Test
    void aShortListIsPaddedWithEmptySlots() {
        assertThat(start.slots()).hasSize(QuickBuy.SIZE);
        assertThat(start.at(0)).contains("argila");
        assertThat(start.at(3)).isEmpty();
        assertThat(start.at(20)).isEmpty();
    }

    @Test
    void placingAnItemPutsItInThatSlotAndReplacesWhatWasThere() {
        QuickBuy changed = start.place(1, "perola");

        assertThat(changed.at(1)).contains("perola");
        assertThat(changed.contains("tnt")).isFalse();
        assertThat(changed.at(0)).contains("argila");
    }

    @Test
    void placingAnItemThatIsAlreadyThereMovesIt() {
        QuickBuy changed = start.place(10, "argila");

        assertThat(changed.at(10)).contains("argila");
        assertThat(changed.at(0)).isEmpty();
        assertThat(changed.slots().stream().filter("argila"::equals)).hasSize(1);
    }

    @Test
    void removingAnItemLeavesItsSlotEmpty() {
        QuickBuy changed = start.remove("tnt");

        assertThat(changed.at(1)).isEmpty();
        assertThat(changed.contains("tnt")).isFalse();
        assertThat(changed.at(2)).contains("la");
    }

    @Test
    void itemsTheShopNoLongerSellsBecomeEmptySlots() {
        QuickBuy known = start.known(Set.of("argila", "la")::contains);

        assertThat(known.at(1)).isEmpty();
        assertThat(known.at(0)).contains("argila");
    }

    @Test
    void aQuickBuyAlwaysHasTwentyOneSlots() {
        assertThatThrownBy(() -> new QuickBuy(List.of("argila"))).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> start.place(21, "la")).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void anEmptySlotIsNeverContained() {
        assertThat(start.contains(QuickBuy.EMPTY)).isFalse();
    }
}
