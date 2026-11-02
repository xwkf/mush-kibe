package br.com.muxe.core.bukkit.scoreboard;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;

class SidebarTest {

    private final FakeScoreboard fake = new FakeScoreboard();
    private final Sidebar sidebar = Sidebar.on(fake.scoreboard());

    @Test
    void unchangedLinesDoNotRewriteTheirScores() {
        sidebar.lines(List.of("&eCoins: 10", "", "&7muxe.com.br"));
        int afterFirstRender = fake.scoreWrites();

        sidebar.lines(List.of("&eCoins: 10", "", "&7muxe.com.br"));

        assertThat(afterFirstRender).isEqualTo(3);
        assertThat(fake.scoreWrites()).isEqualTo(afterFirstRender);
        assertThat(fake.scores().values()).containsExactly(3, 2, 1);
    }

    @Test
    void aChangedLineKeepsItsPlaceWithoutRewritingTheOthers() {
        sidebar.lines(List.of("&eCoins: 10", "", "&7muxe.com.br"));
        int afterFirstRender = fake.scoreWrites();

        sidebar.lines(List.of("&eCoins: 11", "", "&7muxe.com.br"));

        assertThat(fake.scoreWrites()).isEqualTo(afterFirstRender);
        assertThat(fake.scores().values()).containsExactlyInAnyOrder(3, 2, 1);
    }

    @Test
    void addingOrRemovingLinesRenumbersEveryLine() {
        sidebar.lines(List.of("&eCoins: 10", "&7muxe.com.br"));

        sidebar.lines(List.of("&eCoins: 10", "", "&7muxe.com.br"));
        assertThat(fake.scores().values()).containsExactlyInAnyOrder(3, 2, 1);

        sidebar.lines(List.of("&7muxe.com.br"));
        assertThat(fake.scores().values()).containsExactly(1);
    }
}
