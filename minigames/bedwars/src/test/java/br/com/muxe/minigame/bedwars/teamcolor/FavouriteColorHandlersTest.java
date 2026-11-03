package br.com.muxe.minigame.bedwars.teamcolor;

import static org.assertj.core.api.Assertions.assertThat;

import br.com.muxe.core.common.testing.DirectTransactions;
import br.com.muxe.core.common.testing.MutableClock;
import br.com.muxe.minigame.bedwars.teamcolor.application.command.SaveFavouriteColorCommand;
import br.com.muxe.minigame.bedwars.teamcolor.application.command.SaveFavouriteColorHandler;
import br.com.muxe.minigame.bedwars.teamcolor.application.query.FindFavouriteColorHandler;
import br.com.muxe.minigame.bedwars.teamcolor.application.query.FindFavouriteColorQuery;
import br.com.muxe.minigame.bedwars.teamcolor.infrastructure.memory.InMemoryFavouriteColorRepository;
import br.com.muxe.minigame.engine.team.TeamColor;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class FavouriteColorHandlersTest {

    private static final Instant NOW = Instant.parse("2026-11-03T03:00:00Z");

    private final InMemoryFavouriteColorRepository repository = new InMemoryFavouriteColorRepository();
    private final DirectTransactions transactions = new DirectTransactions();
    private final MutableClock clock = new MutableClock(NOW);
    private final SaveFavouriteColorHandler save = new SaveFavouriteColorHandler(repository, transactions, clock);
    private final FindFavouriteColorHandler find = new FindFavouriteColorHandler(repository, transactions);
    private final UUID player = new UUID(0, 1);

    @Test
    void aPlayerWhoNeverChoseAColourHasNoneSaved() {
        assertThat(find.handle(new FindFavouriteColorQuery(player))).isEmpty();
    }

    @Test
    void aSavedColourIsFoundInTheNextMatch() {
        assertThat(save.handle(new SaveFavouriteColorCommand(player, Optional.of(TeamColor.ROSA))).isSuccess()).isTrue();

        assertThat(find.handle(new FindFavouriteColorQuery(player))).contains(TeamColor.ROSA);
        assertThat(repository.updatedAt(player)).contains(NOW);
    }

    @Test
    void theLastColourWinsAndOtherPlayersAreNotAffected() {
        save.handle(new SaveFavouriteColorCommand(player, Optional.of(TeamColor.ROSA)));

        save.handle(new SaveFavouriteColorCommand(player, Optional.of(TeamColor.AZUL)));

        assertThat(find.handle(new FindFavouriteColorQuery(player))).contains(TeamColor.AZUL);
        assertThat(find.handle(new FindFavouriteColorQuery(new UUID(0, 2)))).isEmpty();
    }

    @Test
    void choosingNoneForgetsTheColour() {
        save.handle(new SaveFavouriteColorCommand(player, Optional.of(TeamColor.VERDE)));

        assertThat(save.handle(new SaveFavouriteColorCommand(player, Optional.empty())).isSuccess()).isTrue();

        assertThat(find.handle(new FindFavouriteColorQuery(player))).isEmpty();
        assertThat(repository.updatedAt(player)).isEmpty();
    }

    @Test
    void choosingNoneWithNothingSavedIsHarmless() {
        assertThat(save.handle(new SaveFavouriteColorCommand(player, Optional.empty())).isSuccess()).isTrue();

        assertThat(find.handle(new FindFavouriteColorQuery(player))).isEmpty();
    }
}
