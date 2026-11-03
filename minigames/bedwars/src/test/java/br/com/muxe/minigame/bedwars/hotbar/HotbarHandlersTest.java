package br.com.muxe.minigame.bedwars.hotbar;

import static org.assertj.core.api.Assertions.assertThat;

import br.com.muxe.core.common.testing.DirectTransactions;
import br.com.muxe.core.common.testing.MutableClock;
import br.com.muxe.minigame.bedwars.hotbar.application.command.SaveHotbarCommand;
import br.com.muxe.minigame.bedwars.hotbar.application.command.SaveHotbarHandler;
import br.com.muxe.minigame.bedwars.hotbar.application.query.FindHotbarHandler;
import br.com.muxe.minigame.bedwars.hotbar.application.query.FindHotbarQuery;
import br.com.muxe.minigame.bedwars.hotbar.domain.HotbarEntry;
import br.com.muxe.minigame.bedwars.hotbar.domain.HotbarLayout;
import br.com.muxe.minigame.bedwars.hotbar.infrastructure.memory.InMemoryHotbarRepository;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class HotbarHandlersTest {

    private static final Instant NOW = Instant.parse("2026-11-03T03:00:00Z");

    private final InMemoryHotbarRepository repository = new InMemoryHotbarRepository();
    private final DirectTransactions transactions = new DirectTransactions();
    private final MutableClock clock = new MutableClock(NOW);
    private final SaveHotbarHandler save = new SaveHotbarHandler(repository, transactions, clock);
    private final FindHotbarHandler find = new FindHotbarHandler(repository, transactions);
    private final UUID player = new UUID(0, 1);

    @Test
    void aPlayerWhoNeverEditedTheHotbarHasNoneSaved() {
        assertThat(find.handle(new FindHotbarQuery(player))).isEmpty();
    }

    @Test
    void aSavedHotbarIsFoundInTheNextMatch() {
        HotbarLayout layout = HotbarLayout.empty().add(0, HotbarEntry.category("blocos")).add(0, HotbarEntry.item("la")).add(4, HotbarEntry.item("picareta"));

        assertThat(save.handle(new SaveHotbarCommand(player, layout)).isSuccess()).isTrue();

        assertThat(find.handle(new FindHotbarQuery(player))).contains(layout);
        assertThat(repository.updatedAt(player)).contains(NOW);
    }

    @Test
    void theLastSaveWins() {
        save.handle(new SaveHotbarCommand(player, HotbarLayout.empty().add(0, HotbarEntry.item("la"))));
        HotbarLayout latest = HotbarLayout.empty().add(1, HotbarEntry.item("tnt"));

        save.handle(new SaveHotbarCommand(player, latest));

        assertThat(find.handle(new FindHotbarQuery(player))).contains(latest);
        assertThat(find.handle(new FindHotbarQuery(new UUID(0, 2)))).isEmpty();
    }

    @Test
    void emptyingTheHotbarIsSavedToo() {
        save.handle(new SaveHotbarCommand(player, HotbarLayout.empty().add(0, HotbarEntry.item("la"))));

        save.handle(new SaveHotbarCommand(player, HotbarLayout.empty()));

        assertThat(find.handle(new FindHotbarQuery(player))).contains(HotbarLayout.empty());
    }
}
