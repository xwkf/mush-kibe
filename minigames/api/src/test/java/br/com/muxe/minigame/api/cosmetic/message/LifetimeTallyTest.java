package br.com.muxe.minigame.api.cosmetic.message;

import static org.assertj.core.api.Assertions.assertThat;

import br.com.muxe.minigame.api.cosmetic.message.LifetimeTally.Counter;
import br.com.muxe.minigame.api.cosmetic.message.LifetimeTally.Totals;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class LifetimeTallyTest {

    private static final Totals STORED = new Totals(17_088, 19_214, 12_537, 2_536, 11_937);

    private final LifetimeTally tally = new LifetimeTally();
    private final UUID player = UUID.randomUUID();

    @Test
    void theCounterIncludesTheEventThatJustHappened() {
        tally.load(player, STORED);

        assertThat(tally.record(player, Counter.KILLS)).hasValue(17_089);
        assertThat(tally.record(player, Counter.FINAL_KILLS)).hasValue(19_215);
        assertThat(tally.record(player, Counter.DEATHS)).hasValue(12_538);
        assertThat(tally.record(player, Counter.FINAL_DEATHS)).hasValue(2_537);
        assertThat(tally.record(player, Counter.BEDS)).hasValue(11_938);
    }

    @Test
    void everyEventOfTheMatchCountsOnTopOfTheStoredTotals() {
        tally.load(player, STORED);

        tally.record(player, Counter.KILLS);
        tally.record(player, Counter.KILLS);

        assertThat(tally.record(player, Counter.KILLS)).hasValue(17_091);
        assertThat(tally.record(player, Counter.DEATHS)).hasValue(12_538);
    }

    @Test
    void aPlayerWhoseTotalsAreNotLoadedHasNoCounterButTheMatchStillCounts() {
        assertThat(tally.record(player, Counter.KILLS)).isEmpty();
        tally.load(player, STORED);

        assertThat(tally.record(player, Counter.KILLS)).hasValue(17_090);
    }

    @Test
    void eachPlayerHasHisOwnCounters() {
        UUID other = UUID.randomUUID();
        tally.load(player, STORED);
        tally.load(other, Totals.ZERO);

        tally.record(player, Counter.KILLS);

        assertThat(tally.record(other, Counter.KILLS)).hasValue(1);
    }
}
