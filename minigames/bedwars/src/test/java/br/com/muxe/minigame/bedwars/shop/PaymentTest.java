package br.com.muxe.minigame.bedwars.shop;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;

class PaymentTest {

    @Test
    void theInventoryPaysFirstAndTheChestCoversTheRest() {
        assertThat(Payment.plan(12, List.of(5, 7))).contains(new Payment(List.of(5, 7)));
        assertThat(Payment.plan(6, List.of(5, 7))).contains(new Payment(List.of(5, 1)));
    }

    @Test
    void notEnoughAcrossEverySourceBuysNothing() {
        assertThat(Payment.plan(13, List.of(5, 7))).isEmpty();
        assertThat(Payment.plan(1, List.of())).isEmpty();
    }

    @Test
    void sourcesNotNeededAreLeftAlone() {
        assertThat(Payment.plan(3, List.of(4, 0, 9))).contains(new Payment(List.of(3, 0, 0)));
    }

    @Test
    void teammatesCanCoverDiamondsLeftAfterTheChests() {
        assertThat(Payment.plan(8, List.of(2, 1, 0, 6))).contains(new Payment(List.of(2, 1, 0, 5)));
    }
}
