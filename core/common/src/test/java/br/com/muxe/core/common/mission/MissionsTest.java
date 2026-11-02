package br.com.muxe.core.common.mission;

import static org.assertj.core.api.Assertions.assertThat;

import br.com.muxe.core.common.cqrs.command.CommandBus;
import br.com.muxe.core.common.cqrs.bus.SimpleCommandBus;
import br.com.muxe.core.common.cqrs.bus.SimpleQueryBus;
import br.com.muxe.core.common.cqrs.query.QueryBus;
import br.com.muxe.core.common.mission.application.command.RecordMissionProgressCommand;
import br.com.muxe.core.common.mission.application.command.RecordMissionProgressHandler;
import br.com.muxe.core.common.mission.application.query.FindMissionsQuery;
import br.com.muxe.core.common.mission.domain.Mission;
import br.com.muxe.core.common.mission.domain.MissionBoard;
import br.com.muxe.core.common.mission.domain.MissionCadence;
import br.com.muxe.core.common.mission.domain.MissionCatalog;
import br.com.muxe.core.common.mission.domain.MissionGames;
import br.com.muxe.core.common.mission.domain.MissionGoal;
import br.com.muxe.core.common.mission.domain.MissionSession;
import br.com.muxe.core.common.result.Result;
import br.com.muxe.core.common.testing.DirectTransactions;
import br.com.muxe.core.common.testing.InMemoryCreditRepository;
import br.com.muxe.core.common.testing.InMemoryMissionRepository;
import br.com.muxe.core.common.testing.MutableClock;
import java.time.Duration;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class MissionsTest {

    private static final ZoneId ZONE = ZoneId.of("America/Sao_Paulo");
    private static final String GAME = "bedwars";
    private static final MissionGoal STREAK = new MissionGoal("KILL_STREAK", true);
    private static final MissionGoal FINALS = new MissionGoal("FINAL_KILLS", false);
    private static final MissionGoal FAST = new MissionGoal("FAST_WIN", false);
    private static final Mission STREAK_DAILY = new Mission("ninguem-me-para", MissionCadence.DAILY, STREAK, 2, 625, 0);
    private static final Mission FINALS_WEEKLY = new Mission("fim-da-linha", MissionCadence.WEEKLY, FINALS, 35, 2500, 15);
    private static final Mission FAST_WEEKLY = new Mission("ansiedade", MissionCadence.WEEKLY, FAST, 1, 2500, 15);
    private static final Mission ALL_WEEKLY = new Mission("complecionista", MissionCadence.CREDIT_PATH, MissionGoal.WEEKLY_MISSIONS, 2, 0, 15);
    private static final MissionCatalog CATALOG = new MissionCatalog(List.of(STREAK_DAILY), List.of(FINALS_WEEKLY, FAST_WEEKLY), List.of(ALL_WEEKLY), 3, 5);

    private final MutableClock clock = MutableClock.startingAt("2026-11-04T15:00:00Z");
    private final InMemoryMissionRepository missions = new InMemoryMissionRepository();
    private final InMemoryCreditRepository credits = new InMemoryCreditRepository();
    private final MissionGames games = new MissionGames();
    private final List<Long> grantedXp = new ArrayList<>();
    private final CommandBus commands = new SimpleCommandBus(Runnable::run, List.of());
    private final QueryBus queries = new SimpleQueryBus(Runnable::run, List.of());
    private final UUID player = UUID.randomUUID();

    MissionsTest() {
        games.register(GAME, CATALOG, (playerId, completed, at) -> grantedXp.add(completed.stream().mapToLong(Mission::xp).sum()));
        MissionModule.register(commands, queries, games, missions, credits, new DirectTransactions(), clock, ZONE);
    }

    @Test
    void aSessionTurnsMatchTotalsIntoProgressAndCompletesOnTheSpot() {
        MissionSession session = new MissionSession(board());

        assertThat(session.update(Map.of("KILL_STREAK", 1L, "FINAL_KILLS", 2L))).isEmpty();
        assertThat(session.update(Map.of("KILL_STREAK", 1L, "FINAL_KILLS", 3L))).isEmpty();
        assertThat(session.update(Map.of("KILL_STREAK", 2L, "FINAL_KILLS", 3L))).containsExactly(STREAK_DAILY);
        assertThat(session.drain()).containsOnly(Map.entry("KILL_STREAK", 2L), Map.entry("FINAL_KILLS", 3L));
        assertThat(session.drain()).isEmpty();
        assertThat(session.changes()).extracting(change -> change.entry().mission().id() + "+" + change.added())
            .containsExactly("ninguem-me-para+2", "fim-da-linha+3");
    }

    @Test
    void completingEveryWeeklyMissionAdvancesTheCreditPath() {
        MissionBoard.Update update = board().apply(Map.of("FINAL_KILLS", 35L, "FAST_WIN", 1L));

        assertThat(update.completed()).containsExactly(FINALS_WEEKLY, FAST_WEEKLY, ALL_WEEKLY);
        assertThat(update.board().completed(MissionCadence.CREDIT_PATH)).isEqualTo(1);
    }

    @Test
    void recordingSavesProgressAndPaysCreditsAndTheGameReward() {
        Result<List<Mission>> first = commands.dispatch(new RecordMissionProgressCommand(GAME, player, Map.of("FINAL_KILLS", 30L))).join();
        Result<List<Mission>> second = commands.dispatch(new RecordMissionProgressCommand(GAME, player, Map.of("FINAL_KILLS", 10L, "KILL_STREAK", 2L))).join();

        assertThat(first.orElseThrow()).isEmpty();
        assertThat(second.orElseThrow()).containsExactly(STREAK_DAILY, FINALS_WEEKLY);
        assertThat(credits.balance(player)).isEqualTo(15);
        assertThat(credits.changes().getFirst().reason()).isEqualTo("missions:bedwars");
        assertThat(grantedXp).containsExactly(3125L);
        MissionBoard saved = queries.ask(new FindMissionsQuery(GAME, player)).join();
        assertThat(saved.entries()).extracting(entry -> entry.progress().progress()).containsExactly(2L, 35L, 0L, 1L);
    }

    @Test
    void progressStartsAgainOnTheNextDayAndWeek() {
        commands.dispatch(new RecordMissionProgressCommand(GAME, player, Map.of("KILL_STREAK", 2L, "FINAL_KILLS", 5L))).join();

        clock.advance(Duration.ofDays(7));

        assertThat(queries.ask(new FindMissionsQuery(GAME, player)).join().entries()).allMatch(entry -> entry.progress().progress() == 0);
    }

    @Test
    void anUnknownGameIsAnExpectedFailure() {
        Result<List<Mission>> result = commands.dispatch(new RecordMissionProgressCommand("skywars", player, Map.of("FINAL_KILLS", 1L))).join();

        assertThat(result).isInstanceOfSatisfying(Result.Failure.class,
            failure -> assertThat(failure.problem().code()).isEqualTo(RecordMissionProgressHandler.UNKNOWN_GAME));
    }

    @Test
    void dailyMissionsAreDrawnPerPlayerAndDayButStayTheSameWithinTheDay() {
        List<Mission> pool = new ArrayList<>();
        for (int index = 0; index < 6; index++) {
            pool.add(new Mission("diaria-" + index, MissionCadence.DAILY, FINALS, 1, 625, 0));
        }
        MissionCatalog catalog = new MissionCatalog(pool, List.of(), List.of(), 3, 0);

        List<Mission> morning = catalog.active(player, clock.instant(), ZONE);
        List<Mission> evening = catalog.active(player, clock.instant().plus(Duration.ofHours(6)), ZONE);

        assertThat(morning).hasSize(3).isEqualTo(evening);
        assertThat(pool).containsAll(morning);
    }

    private MissionBoard board() {
        List<Mission> active = CATALOG.active(player, clock.instant(), ZONE);
        return MissionBoard.of(active, Map.of(), MissionBoard.starts(active, clock.instant(), ZONE));
    }
}
