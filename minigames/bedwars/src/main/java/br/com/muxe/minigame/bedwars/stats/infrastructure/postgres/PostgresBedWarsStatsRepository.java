package br.com.muxe.minigame.bedwars.stats.infrastructure.postgres;

import br.com.muxe.core.common.persistence.sql.Column;
import br.com.muxe.core.common.persistence.sql.InsertBuilder;
import br.com.muxe.core.common.persistence.sql.Row;
import br.com.muxe.core.common.persistence.sql.Select;
import br.com.muxe.core.common.persistence.sql.Sql;
import br.com.muxe.core.common.persistence.sql.Table;
import br.com.muxe.core.common.persistence.sql.Write;
import br.com.muxe.core.common.persistence.sql.type.SqlType;
import br.com.muxe.minigame.bedwars.stats.domain.BedWarsStatsRepository;
import br.com.muxe.minigame.bedwars.stats.domain.MatchOutcome;
import br.com.muxe.minigame.bedwars.stats.domain.PlayerStats;
import br.com.muxe.minigame.bedwars.stats.domain.StatCounters;
import br.com.muxe.minigame.bedwars.stats.domain.StatsPeriod;
import br.com.muxe.minigame.bedwars.stats.domain.StatsSlot;
import br.com.muxe.minigame.bedwars.stats.domain.TopCategory;
import br.com.muxe.minigame.bedwars.stats.domain.TopEntry;
import java.sql.SQLException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.ToLongFunction;

public final class PostgresBedWarsStatsRepository implements BedWarsStatsRepository {

    private static final String STREAK = "CASE WHEN EXCLUDED.wins > 0 THEN s.winstreak + 1 WHEN EXCLUDED.losses > 0 THEN 0 ELSE s.winstreak END";

    private static final Table STATS = Table.of("bedwars", "player_stats");
    private static final Column<UUID> PLAYER_ID = STATS.column("player_id", SqlType.UUID);
    private static final Column<String> MODE = STATS.column("mode", SqlType.TEXT);
    private static final Column<String> PERIOD = STATS.column("period", SqlType.TEXT);
    private static final Column<LocalDate> PERIOD_START = STATS.column("period_start", SqlType.DATE);
    private static final Column<Long> GAMES = STATS.column("games", SqlType.BIGINT);
    private static final Column<Long> BEDS_BROKEN = STATS.column("beds_broken", SqlType.BIGINT);
    private static final Column<Long> BEDS_LOST = STATS.column("beds_lost", SqlType.BIGINT);
    private static final Column<Long> KILLS = STATS.column("kills", SqlType.BIGINT);
    private static final Column<Long> DEATHS = STATS.column("deaths", SqlType.BIGINT);
    private static final Column<Long> ASSISTS = STATS.column("assists", SqlType.BIGINT);
    private static final Column<Long> FINAL_KILLS = STATS.column("final_kills", SqlType.BIGINT);
    private static final Column<Long> FINAL_DEATHS = STATS.column("final_deaths", SqlType.BIGINT);
    private static final Column<Long> FINAL_ASSISTS = STATS.column("final_assists", SqlType.BIGINT);
    private static final Column<Long> WINS = STATS.column("wins", SqlType.BIGINT);
    private static final Column<Long> LOSSES = STATS.column("losses", SqlType.BIGINT);
    private static final Column<Long> WINSTREAK = STATS.column("winstreak", SqlType.BIGINT);
    private static final Column<Long> BEST_WINSTREAK = STATS.column("best_winstreak", SqlType.BIGINT);
    private static final Column<Long> XP = STATS.column("xp", SqlType.BIGINT);
    private static final Column<Instant> UPDATED_AT = STATS.column("updated_at", SqlType.TIMESTAMPTZ);

    private static final List<Counter> COUNTERS = List.of(
        new Counter(GAMES, StatCounters::games),
        new Counter(BEDS_BROKEN, StatCounters::bedsBroken),
        new Counter(BEDS_LOST, StatCounters::bedsLost),
        new Counter(KILLS, StatCounters::kills),
        new Counter(DEATHS, StatCounters::deaths),
        new Counter(ASSISTS, StatCounters::assists),
        new Counter(FINAL_KILLS, StatCounters::finalKills),
        new Counter(FINAL_DEATHS, StatCounters::finalDeaths),
        new Counter(FINAL_ASSISTS, StatCounters::finalAssists),
        new Counter(WINS, StatCounters::wins),
        new Counter(LOSSES, StatCounters::losses),
        new Counter(WINSTREAK, StatCounters::winstreak),
        new Counter(BEST_WINSTREAK, StatCounters::bestWinstreak),
        new Counter(XP, StatCounters::xp)
    );

    private static final List<Column<Long>> SUMMED = List.of(GAMES, BEDS_BROKEN, BEDS_LOST, KILLS, DEATHS, ASSISTS,
        FINAL_KILLS, FINAL_DEATHS, FINAL_ASSISTS, WINS, LOSSES);

    private static final Table RECORDED = Table.of("bedwars", "recorded_matches");
    private static final Column<UUID> MATCH_ID = RECORDED.column("match_id", SqlType.UUID);
    private static final Column<Instant> RECORDED_AT = RECORDED.column("recorded_at", SqlType.TIMESTAMPTZ);

    private static final Table FAVOURITES = Table.of("bedwars", "favourite_maps");
    private static final Column<UUID> FAVOURITE_PLAYER = FAVOURITES.column("player_id", SqlType.UUID);
    private static final Column<String> ARENA = FAVOURITES.column("arena", SqlType.TEXT);
    private static final Column<Instant> CREATED_AT = FAVOURITES.column("created_at", SqlType.TIMESTAMPTZ);

    private static final Column<String> TOTAL_MODE = Sql.parameter("total_mode", SqlType.TEXT);
    private static final Column<String> TOTAL_PERIOD = Sql.parameter("total_period", SqlType.TEXT);
    private static final Column<LocalDate> TOTAL_START = Sql.parameter("total_start", SqlType.DATE);
    private static final Column<Integer> LIMIT = Sql.parameter("limit", SqlType.INTEGER);
    private static final Column<UUID> TOP_PLAYER = Sql.parameter("player_id", SqlType.UUID);
    private static final Column<String> TOP_NAME = Sql.parameter("name", SqlType.TEXT);
    private static final Column<Long> TOP_VALUE = Sql.parameter("value", SqlType.BIGINT);
    private static final Column<Long> TOP_TOTAL_XP = Sql.parameter("total_xp", SqlType.BIGINT);

    private static final String TOP = """
        SELECT s.player_id, COALESCE(a.username, s.player_id::text) AS name, s.%1$s AS value, COALESCE(t.xp, 0) AS total_xp
        FROM bedwars.player_stats s
        LEFT JOIN core.accounts a ON a.id = s.player_id
        LEFT JOIN bedwars.player_stats t
            ON t.player_id = s.player_id AND t.mode = ? AND t.period = ? AND t.period_start = ?
        WHERE s.mode = ? AND s.period = ? AND s.period_start = ? AND s.%1$s > 0
        ORDER BY s.%1$s DESC, s.player_id
        LIMIT ?
        """;

    private static final Write<Recorded> RECORD = record();

    private static final Write<Void> CLAIM = Sql.<Void>insertInto(RECORDED)
        .value(MATCH_ID)
        .value(RECORDED_AT)
        .onConflict(MATCH_ID).doNothing()
        .named("bedwars.stats.claim_match");

    private static final Write<EarnedXp> ADD_XP = Sql.<EarnedXp>insertInto(STATS)
        .as("s")
        .value(PLAYER_ID, EarnedXp::playerId)
        .value(MODE, earned -> PlayerStats.GENERAL)
        .value(PERIOD, earned -> earned.period().id())
        .value(PERIOD_START, EarnedXp::start)
        .value(XP, EarnedXp::xp)
        .value(UPDATED_AT, EarnedXp::at)
        .onConflict(PLAYER_ID, MODE, PERIOD, PERIOD_START)
        .doUpdate(XP, "s.xp + EXCLUDED.xp")
        .doUpdate(UPDATED_AT)
        .named("bedwars.stats.add_xp");

    private static final Select<ModeCounters> FIND_STATS = Sql.select(withMode()).from(STATS)
        .where(PLAYER_ID.eq().and(PERIOD.eq()).and(PERIOD_START.eq()))
        .map(row -> new ModeCounters(row.get(MODE), counters(row)))
        .named("bedwars.stats.find");

    private static final Map<TopCategory, Select<TopEntry>> TOPS = tops();

    private static final Select<String> FIND_FAVOURITES = Sql.select(ARENA).from(FAVOURITES)
        .where(FAVOURITE_PLAYER.eq())
        .orderBy(ARENA.asc())
        .map(row -> row.get(ARENA))
        .named("bedwars.favourites.find");

    private static final Write<Void> UNFAVOURITE = Sql.deleteFrom(FAVOURITES)
        .where(FAVOURITE_PLAYER.eq().and(ARENA.eq()))
        .named("bedwars.favourites.remove");

    private static final Write<Void> FAVOURITE = Sql.<Void>insertInto(FAVOURITES)
        .value(FAVOURITE_PLAYER)
        .value(ARENA)
        .value(CREATED_AT)
        .onConflict(FAVOURITE_PLAYER, ARENA).doNothing()
        .named("bedwars.favourites.add");

    @Override
    public void record(MatchOutcome outcome, Instant at, ZoneId zone) {
        StatCounters added = StatCounters.ZERO.plus(outcome);
        RECORD.batch(StatsSlot.touchedBy(outcome, at, zone).stream().map(slot -> new Recorded(outcome.playerId(), slot, added, at)).toList());
    }

    @Override
    public boolean claimMatch(UUID matchId, Instant at) {
        return CLAIM.execute(MATCH_ID.is(matchId), RECORDED_AT.is(at)) == 1;
    }

    @Override
    public void addXp(UUID playerId, long xp, Instant at, ZoneId zone) {
        ADD_XP.batch(Arrays.stream(StatsPeriod.values()).map(period -> new EarnedXp(playerId, period, period.start(at, zone), xp, at)).toList());
    }

    @Override
    public PlayerStats totals(UUID playerId) {
        return stats(playerId, StatsPeriod.TOTAL, StatsPeriod.TOTAL_START);
    }

    @Override
    public PlayerStats period(UUID playerId, StatsPeriod period, Instant at, ZoneId zone) {
        return stats(playerId, period, period.start(at, zone));
    }

    @Override
    public List<TopEntry> top(TopCategory category, String mode, StatsPeriod period, LocalDate start, int limit) {
        List<TopEntry> found = TOPS.get(category).list(
            TOTAL_MODE.is(PlayerStats.GENERAL),
            TOTAL_PERIOD.is(StatsPeriod.TOTAL.id()),
            TOTAL_START.is(StatsPeriod.TOTAL_START),
            MODE.is(mode),
            PERIOD.is(period.id()),
            PERIOD_START.is(start),
            LIMIT.is(limit)
        );
        List<TopEntry> ranked = new ArrayList<>(found.size());
        for (TopEntry entry : found) {
            ranked.add(new TopEntry(ranked.size() + 1, entry.playerId(), entry.name(), entry.value(), entry.totalXp()));
        }
        return List.copyOf(ranked);
    }

    @Override
    public Set<String> favouriteMaps(UUID playerId) {
        return Collections.unmodifiableSet(new LinkedHashSet<>(FIND_FAVOURITES.list(FAVOURITE_PLAYER.is(playerId))));
    }

    @Override
    public boolean toggleFavouriteMap(UUID playerId, String arena, Instant at) {
        if (UNFAVOURITE.execute(FAVOURITE_PLAYER.is(playerId), ARENA.is(arena)) > 0) {
            return false;
        }
        FAVOURITE.execute(FAVOURITE_PLAYER.is(playerId), ARENA.is(arena), CREATED_AT.is(at));
        return true;
    }

    private static PlayerStats stats(UUID playerId, StatsPeriod period, LocalDate start) {
        Map<String, StatCounters> byMode = new HashMap<>();
        for (ModeCounters counters : FIND_STATS.list(PLAYER_ID.is(playerId), PERIOD.is(period.id()), PERIOD_START.is(start))) {
            byMode.put(counters.mode(), counters.counters());
        }
        return new PlayerStats(byMode);
    }

    private static Write<Recorded> record() {
        InsertBuilder<Recorded> insert = Sql.<Recorded>insertInto(STATS)
            .as("s")
            .value(PLAYER_ID, Recorded::playerId)
            .value(MODE, recorded -> recorded.slot().mode())
            .value(PERIOD, recorded -> recorded.slot().period().id())
            .value(PERIOD_START, recorded -> recorded.slot().start());
        for (Counter counter : COUNTERS) {
            insert.value(counter.column(), recorded -> counter.value().applyAsLong(recorded.added()));
        }
        insert.value(UPDATED_AT, Recorded::at).onConflict(PLAYER_ID, MODE, PERIOD, PERIOD_START);
        for (Column<Long> summed : SUMMED) {
            insert.doUpdate(summed, "s." + summed.name() + " + EXCLUDED." + summed.name());
        }
        return insert
            .doUpdate(WINSTREAK, STREAK)
            .doUpdate(BEST_WINSTREAK, "GREATEST(s.best_winstreak, " + STREAK + ")")
            .doUpdate(XP, "s.xp + EXCLUDED.xp")
            .doUpdate(UPDATED_AT)
            .named("bedwars.stats.record");
    }

    private static Map<TopCategory, Select<TopEntry>> tops() {
        Map<TopCategory, Select<TopEntry>> tops = new EnumMap<>(TopCategory.class);
        for (TopCategory category : TopCategory.values()) {
            Column<Long> ranked = switch (category) {
                case FINAL_KILLS -> FINAL_KILLS;
                case WINS -> WINS;
                case WINSTREAK -> WINSTREAK;
                case LEVEL -> XP;
            };
            tops.put(category, Sql.query(TOP.formatted(ranked.name()))
                .parameters(TOTAL_MODE, TOTAL_PERIOD, TOTAL_START, MODE, PERIOD, PERIOD_START, LIMIT)
                .columns(TOP_PLAYER, TOP_NAME, TOP_VALUE, TOP_TOTAL_XP)
                .map(row -> new TopEntry(1, row.get(TOP_PLAYER), row.get(TOP_NAME), row.get(TOP_VALUE), row.get(TOP_TOTAL_XP)))
                .named("bedwars.stats.top"));
        }
        return Collections.unmodifiableMap(tops);
    }

    private static List<Column<?>> withMode() {
        List<Column<?>> columns = new ArrayList<>(List.of(MODE));
        COUNTERS.forEach(counter -> columns.add(counter.column()));
        return columns;
    }

    private static StatCounters counters(Row row) throws SQLException {
        long[] values = new long[COUNTERS.size()];
        for (int index = 0; index < values.length; index++) {
            values[index] = row.get(COUNTERS.get(index).column());
        }
        return new StatCounters(values[0], values[1], values[2], values[3], values[4], values[5], values[6],
            values[7], values[8], values[9], values[10], values[11], values[12], values[13]);
    }

    private record Counter(Column<Long> column, ToLongFunction<StatCounters> value) {
    }

    private record Recorded(UUID playerId, StatsSlot slot, StatCounters added, Instant at) {
    }

    private record EarnedXp(UUID playerId, StatsPeriod period, LocalDate start, long xp, Instant at) {
    }

    private record ModeCounters(String mode, StatCounters counters) {
    }
}
