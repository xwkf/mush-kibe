package br.com.muxe.minigame.api.match;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class RankingTest {

    @Test
    void theTopThreeAreSortedByScoreAndTiesKeepTheJoinOrder() {
        UUID a = new UUID(0, 1);
        UUID b = new UUID(0, 2);
        UUID c = new UUID(0, 3);
        UUID d = new UUID(0, 4);

        List<Ranking.Entry> top = Ranking.top(Map.of(a, 3, b, 3, c, 1, d, 0), List.of(b, a, c, d));

        assertThat(top).containsExactly(new Ranking.Entry(b, 3), new Ranking.Entry(a, 3), new Ranking.Entry(c, 1));
    }
}
