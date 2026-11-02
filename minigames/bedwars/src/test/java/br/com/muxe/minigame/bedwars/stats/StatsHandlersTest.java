package br.com.muxe.minigame.bedwars.stats;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import br.com.muxe.core.common.cqrs.bus.SimpleCommandBus;
import br.com.muxe.core.common.cqrs.bus.SimpleQueryBus;
import br.com.muxe.core.common.result.Result;
import br.com.muxe.core.common.testing.DirectTransactions;
import br.com.muxe.core.common.testing.MutableClock;
import br.com.muxe.minigame.bedwars.stats.application.command.RecordMatchCommand;
import br.com.muxe.minigame.bedwars.stats.application.command.RecordMatchHandler;
import br.com.muxe.minigame.bedwars.stats.application.command.ToggleFavouriteMapCommand;
import br.com.muxe.minigame.bedwars.stats.application.command.ToggleFavouriteMapHandler;
import br.com.muxe.minigame.bedwars.stats.application.query.FindFavouriteMapsHandler;
import br.com.muxe.minigame.bedwars.stats.application.query.FindFavouriteMapsQuery;
import br.com.muxe.minigame.bedwars.stats.application.query.FindPlayerStatsHandler;
import br.com.muxe.minigame.bedwars.stats.application.query.FindPlayerStatsQuery;
import br.com.muxe.minigame.bedwars.stats.application.query.FindTopHandler;
import br.com.muxe.minigame.bedwars.stats.application.query.FindTopQuery;
import br.com.muxe.minigame.bedwars.stats.domain.MatchOutcome;
import br.com.muxe.minigame.bedwars.stats.domain.MatchResult;
import br.com.muxe.minigame.bedwars.stats.domain.PlayerStats;
import br.com.muxe.minigame.bedwars.stats.domain.StatsPeriod;
import br.com.muxe.minigame.bedwars.stats.domain.TopCategory;
import br.com.muxe.minigame.bedwars.stats.domain.TopEntry;
import br.com.muxe.minigame.bedwars.stats.infrastructure.memory.InMemoryBedWarsStatsRepository;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class StatsHandlersTest {

    private static final ZoneId SAO_PAULO = ZoneId.of("America/Sao_Paulo");

    private final InMemoryBedWarsStatsRepository repository = new InMemoryBedWarsStatsRepository();
    private final DirectTransactions transactions = new DirectTransactions();
    private final MutableClock clock = new MutableClock(Instant.parse("2026-11-04T15:00:00Z"));
    private final RecordMatchHandler record = new RecordMatchHandler(repository, transactions, clock, SAO_PAULO);
    private final ToggleFavouriteMapHandler toggle = new ToggleFavouriteMapHandler(repository, transactions, clock);
    private final FindPlayerStatsHandler stats = new FindPlayerStatsHandler(repository, transactions, clock, SAO_PAULO);
    private final FindTopHandler top = new FindTopHandler(repository, transactions, clock, SAO_PAULO);
    private final FindFavouriteMapsHandler favourites = new FindFavouriteMapsHandler(repository, transactions);
    private final UUID alice = new UUID(0, 1);
    private final UUID bruno = new UUID(0, 2);
    private final UUID carla = new UUID(0, 3);
    private final UUID diego = new UUID(0, 4);

    @Test
    void recordingAMatchUpdatesTheModeAndTheGeneralRowsOfEveryPeriodInOneTransaction() {
        Result<Void> result = record.handle(new RecordMatchCommand(List.of(
            outcome(alice, "solo", 2, MatchResult.WIN, 120),
            outcome(bruno, "solo", 0, MatchResult.LOSS, 40)
        )));

        assertThat(result.isSuccess()).isTrue();
        assertThat(transactions.operations()).containsExactly("bedwars.stats.record");
        assertThat(repository.rowCount()).isEqualTo(16);
        for (StatsPeriod period : StatsPeriod.values()) {
            PlayerStats current = stats.handle(new FindPlayerStatsQuery(alice, period));
            assertThat(current.of("solo").finalKills()).isEqualTo(2);
            assertThat(current.general()).isEqualTo(current.of("solo"));
            assertThat(current.general().wins()).isEqualTo(1);
            assertThat(current.general().xp()).isEqualTo(120);
        }
        assertThat(stats.handle(new FindPlayerStatsQuery(bruno)).general().losses()).isEqualTo(1);
    }

    @Test
    void theGeneralRowAddsUpEveryModeAndTheDailyRowResetsTheNextDay() {
        record.handle(new RecordMatchCommand(List.of(outcome(alice, "solo", 1, MatchResult.WIN, 10))));
        record.handle(new RecordMatchCommand(List.of(outcome(alice, "dupla", 3, MatchResult.WIN, 20))));

        clock.advance(Duration.ofDays(1));
        record.handle(new RecordMatchCommand(List.of(outcome(alice, "solo", 1, MatchResult.LOSS, 5))));

        PlayerStats total = stats.handle(new FindPlayerStatsQuery(alice));
        assertThat(total.general().games()).isEqualTo(3);
        assertThat(total.general().finalKills()).isEqualTo(5);
        assertThat(total.general().bestWinstreak()).isEqualTo(2);
        assertThat(total.general().winstreak()).isZero();
        assertThat(total.of("dupla").games()).isEqualTo(1);

        PlayerStats today = stats.handle(new FindPlayerStatsQuery(alice, StatsPeriod.DAILY));
        assertThat(today.general().games()).isEqualTo(1);
        assertThat(today.of("dupla").games()).isZero();
        assertThat(stats.handle(new FindPlayerStatsQuery(alice, StatsPeriod.WEEKLY)).general().games()).isEqualTo(3);
    }

    @Test
    void topsAreOrderedByValueThenPlayerSkipZerosAndRespectTheLimit() {
        repository.name(alice, "Alice");
        repository.name(bruno, "Bruno");
        record.handle(new RecordMatchCommand(List.of(
            outcome(alice, "solo", 2, MatchResult.LOSS, 30),
            outcome(bruno, "solo", 5, MatchResult.WIN, 70),
            outcome(carla, "solo", 2, MatchResult.LOSS, 10),
            outcome(diego, "solo", 0, MatchResult.LOSS, 90)
        )));

        List<TopEntry> finalKills = top.handle(new FindTopQuery(TopCategory.FINAL_KILLS, "solo", StatsPeriod.TOTAL, 10));

        assertThat(finalKills).containsExactly(
            new TopEntry(1, bruno, "Bruno", 5, 70),
            new TopEntry(2, alice, "Alice", 2, 30),
            new TopEntry(3, carla, carla.toString(), 2, 10)
        );
        assertThat(top.handle(new FindTopQuery(TopCategory.FINAL_KILLS, PlayerStats.GENERAL, StatsPeriod.DAILY, 2)))
            .extracting(TopEntry::playerId)
            .containsExactly(bruno, alice);
        assertThat(top.handle(new FindTopQuery(TopCategory.WINS, "solo", StatsPeriod.WEEKLY, 10)))
            .extracting(TopEntry::playerId)
            .containsExactly(bruno);
        assertThat(top.handle(new FindTopQuery(TopCategory.LEVEL, "solo", StatsPeriod.MONTHLY, 10)))
            .extracting(TopEntry::playerId)
            .containsExactly(diego, bruno, alice, carla);
        assertThat(top.handle(new FindTopQuery(TopCategory.FINAL_KILLS, "dupla", StatsPeriod.TOTAL, 10))).isEmpty();
    }

    @Test
    void levelTopsUseThePeriodXpAndReportTheTotalXp() {
        record.handle(new RecordMatchCommand(List.of(outcome(alice, "solo", 0, MatchResult.WIN, 500))));
        clock.advance(Duration.ofDays(1));
        record.handle(new RecordMatchCommand(List.of(
            outcome(alice, "solo", 0, MatchResult.WIN, 10),
            outcome(bruno, "solo", 0, MatchResult.WIN, 50)
        )));

        assertThat(top.handle(new FindTopQuery(TopCategory.LEVEL, "solo", StatsPeriod.DAILY, 10))).containsExactly(
            new TopEntry(1, bruno, bruno.toString(), 50, 50),
            new TopEntry(2, alice, alice.toString(), 10, 510)
        );
        assertThat(top.handle(new FindTopQuery(TopCategory.WINSTREAK, "solo", StatsPeriod.TOTAL, 10)))
            .extracting(TopEntry::value)
            .containsExactly(2L, 1L);
    }

    @Test
    void favouriteMapsToggle() {
        assertThat(toggle.handle(new ToggleFavouriteMapCommand(alice, "Lighthouse"))).isEqualTo(Result.success(true));
        assertThat(toggle.handle(new ToggleFavouriteMapCommand(alice, "Aquarium"))).isEqualTo(Result.success(true));

        assertThat(favourites.handle(new FindFavouriteMapsQuery(alice))).containsExactly("Aquarium", "Lighthouse");

        assertThat(toggle.handle(new ToggleFavouriteMapCommand(alice, "Lighthouse"))).isEqualTo(Result.success(false));
        assertThat(favourites.handle(new FindFavouriteMapsQuery(alice))).containsExactly("Aquarium");
        assertThat(favourites.handle(new FindFavouriteMapsQuery(bruno))).isEmpty();
    }

    @Test
    void invalidRequestsAreRejected() {
        MatchOutcome twice = outcome(alice, "solo", 0, MatchResult.WIN, 0);

        assertThatThrownBy(() -> new RecordMatchCommand(List.of(twice, twice))).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new ToggleFavouriteMapCommand(alice, " ")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new FindTopQuery(TopCategory.WINS, "solo", StatsPeriod.TOTAL, 0)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new FindTopQuery(TopCategory.WINS, "solo", StatsPeriod.TOTAL, FindTopQuery.MAX_LIMIT + 1))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void theModuleRegistersEveryCommandAndQuery() {
        SimpleCommandBus commands = new SimpleCommandBus(Runnable::run, List.of());
        SimpleQueryBus queries = new SimpleQueryBus(Runnable::run, List.of());
        StatsModule.register(commands, queries, repository, transactions, clock, SAO_PAULO);

        commands.dispatch(new RecordMatchCommand(List.of(outcome(alice, "solo", 1, MatchResult.WIN, 10)))).join();
        commands.dispatch(new ToggleFavouriteMapCommand(alice, "Lighthouse")).join();

        assertThat(queries.ask(new FindPlayerStatsQuery(alice)).join().general().finalKills()).isEqualTo(1);
        assertThat(queries.ask(new FindTopQuery(TopCategory.WINS, "solo", StatsPeriod.TOTAL, 5)).join()).hasSize(1);
        assertThat(queries.ask(new FindFavouriteMapsQuery(alice)).join()).containsExactly("Lighthouse");
    }

    private static MatchOutcome outcome(UUID player, String mode, long finalKills, MatchResult result, long xp) {
        return new MatchOutcome(player, mode, 0, 0, 1, 1, 0, finalKills, 0, 0, result, xp);
    }
}
