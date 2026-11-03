package br.com.muxe.minigame.bedwars.quickbuy;

import static org.assertj.core.api.Assertions.assertThat;

import br.com.muxe.core.common.testing.DirectTransactions;
import br.com.muxe.core.common.testing.MutableClock;
import br.com.muxe.minigame.bedwars.quickbuy.application.command.SaveQuickBuyCommand;
import br.com.muxe.minigame.bedwars.quickbuy.application.command.SaveQuickBuyHandler;
import br.com.muxe.minigame.bedwars.quickbuy.application.query.FindQuickBuyHandler;
import br.com.muxe.minigame.bedwars.quickbuy.application.query.FindQuickBuyQuery;
import br.com.muxe.minigame.bedwars.quickbuy.domain.QuickBuy;
import br.com.muxe.minigame.bedwars.quickbuy.infrastructure.memory.InMemoryQuickBuyRepository;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class QuickBuyHandlersTest {

    private static final Instant NOW = Instant.parse("2026-11-03T03:00:00Z");

    private final InMemoryQuickBuyRepository repository = new InMemoryQuickBuyRepository();
    private final DirectTransactions transactions = new DirectTransactions();
    private final MutableClock clock = new MutableClock(NOW);
    private final SaveQuickBuyHandler save = new SaveQuickBuyHandler(repository, transactions, clock);
    private final FindQuickBuyHandler find = new FindQuickBuyHandler(repository, transactions);
    private final UUID player = new UUID(0, 1);

    @Test
    void aPlayerWhoNeverChangedTheQuickBuyHasNoneSaved() {
        assertThat(find.handle(new FindQuickBuyQuery(player))).isEmpty();
    }

    @Test
    void aChangedQuickBuyIsFoundInTheNextMatch() {
        QuickBuy changed = QuickBuy.of(List.of("la", "argila")).place(5, "perola");

        assertThat(save.handle(new SaveQuickBuyCommand(player, changed)).isSuccess()).isTrue();

        assertThat(find.handle(new FindQuickBuyQuery(player))).contains(changed);
        assertThat(repository.updatedAt(player)).contains(NOW);
    }

    @Test
    void theLastChangeWins() {
        save.handle(new SaveQuickBuyCommand(player, QuickBuy.of(List.of("la"))));
        QuickBuy latest = QuickBuy.of(List.of("tnt"));

        save.handle(new SaveQuickBuyCommand(player, latest));

        assertThat(find.handle(new FindQuickBuyQuery(player))).contains(latest);
        assertThat(find.handle(new FindQuickBuyQuery(new UUID(0, 2)))).isEmpty();
    }
}
