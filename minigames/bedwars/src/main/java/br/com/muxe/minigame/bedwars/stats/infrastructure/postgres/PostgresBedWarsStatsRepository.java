package br.com.muxe.minigame.bedwars.stats.infrastructure.postgres;

import br.com.muxe.core.common.persistence.PersistenceException;
import br.com.muxe.core.common.persistence.transaction.TransactionContext;
import br.com.muxe.minigame.bedwars.stats.domain.BedWarsStatsRepository;
import br.com.muxe.minigame.bedwars.stats.domain.MatchOutcome;
import br.com.muxe.minigame.bedwars.stats.domain.PlayerStats;
import br.com.muxe.minigame.bedwars.stats.domain.StatCounters;
import br.com.muxe.minigame.bedwars.stats.domain.StatsPeriod;
import br.com.muxe.minigame.bedwars.stats.domain.StatsSlot;
import br.com.muxe.minigame.bedwars.stats.domain.TopCategory;
import br.com.muxe.minigame.bedwars.stats.domain.TopEntry;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public final class PostgresBedWarsStatsRepository implements BedWarsStatsRepository {

    private static final String STREAK = "CASE WHEN EXCLUDED.wins > 0 THEN s.winstreak + 1 WHEN EXCLUDED.losses > 0 THEN 0 ELSE s.winstreak END";

    private static final String RECORD = """
        INSERT INTO bedwars.player_stats AS s (
            player_id, mode, period, period_start, games, beds_broken, beds_lost, kills, deaths, assists,
            final_kills, final_deaths, final_assists, wins, losses, winstreak, best_winstreak, xp, updated_at
        ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        ON CONFLICT (player_id, mode, period, period_start) DO UPDATE SET
            games = s.games + EXCLUDED.games,
            beds_broken = s.beds_broken + EXCLUDED.beds_broken,
            beds_lost = s.beds_lost + EXCLUDED.beds_lost,
            kills = s.kills + EXCLUDED.kills,
            deaths = s.deaths + EXCLUDED.deaths,
            assists = s.assists + EXCLUDED.assists,
            final_kills = s.final_kills + EXCLUDED.final_kills,
            final_deaths = s.final_deaths + EXCLUDED.final_deaths,
            final_assists = s.final_assists + EXCLUDED.final_assists,
            wins = s.wins + EXCLUDED.wins,
            losses = s.losses + EXCLUDED.losses,
            winstreak = %1$s,
            best_winstreak = GREATEST(s.best_winstreak, %1$s),
            xp = s.xp + EXCLUDED.xp,
            updated_at = EXCLUDED.updated_at
        """.formatted(STREAK);

    private static final String ADD_XP = """
        INSERT INTO bedwars.player_stats AS s (player_id, mode, period, period_start, xp, updated_at) VALUES (?, ?, ?, ?, ?, ?)
        ON CONFLICT (player_id, mode, period, period_start) DO UPDATE SET xp = s.xp + EXCLUDED.xp, updated_at = EXCLUDED.updated_at
        """;

    private static final String STATS = """
        SELECT mode, games, beds_broken, beds_lost, kills, deaths, assists, final_kills, final_deaths, final_assists,
            wins, losses, winstreak, best_winstreak, xp
        FROM bedwars.player_stats
        WHERE player_id = ? AND period = ? AND period_start = ?
        """;

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

    private static final Map<TopCategory, String> TOPS = tops();

    private static final String FAVOURITES = "SELECT arena FROM bedwars.favourite_maps WHERE player_id = ? ORDER BY arena";

    private static final String UNFAVOURITE = "DELETE FROM bedwars.favourite_maps WHERE player_id = ? AND arena = ?";

    private static final String FAVOURITE = """
        INSERT INTO bedwars.favourite_maps (player_id, arena, created_at) VALUES (?, ?, ?)
        ON CONFLICT (player_id, arena) DO NOTHING
        """;

    @Override
    public void record(MatchOutcome outcome, Instant at, ZoneId zone) {
        StatCounters added = StatCounters.ZERO.plus(outcome);
        try (PreparedStatement statement = TransactionContext.connection().prepareStatement(RECORD)) {
            for (StatsSlot slot : StatsSlot.touchedBy(outcome, at, zone)) {
                statement.setObject(1, outcome.playerId());
                statement.setString(2, slot.mode());
                statement.setString(3, slot.period().id());
                statement.setObject(4, slot.start());
                statement.setLong(5, added.games());
                statement.setLong(6, added.bedsBroken());
                statement.setLong(7, added.bedsLost());
                statement.setLong(8, added.kills());
                statement.setLong(9, added.deaths());
                statement.setLong(10, added.assists());
                statement.setLong(11, added.finalKills());
                statement.setLong(12, added.finalDeaths());
                statement.setLong(13, added.finalAssists());
                statement.setLong(14, added.wins());
                statement.setLong(15, added.losses());
                statement.setLong(16, added.winstreak());
                statement.setLong(17, added.bestWinstreak());
                statement.setLong(18, added.xp());
                statement.setTimestamp(19, Timestamp.from(at));
                statement.addBatch();
            }
            statement.executeBatch();
        } catch (SQLException exception) {
            throw new PersistenceException("bedwars.stats.record", exception);
        }
    }

    @Override
    public void addXp(UUID playerId, long xp, Instant at, ZoneId zone) {
        try (PreparedStatement statement = TransactionContext.connection().prepareStatement(ADD_XP)) {
            for (StatsPeriod period : StatsPeriod.values()) {
                statement.setObject(1, playerId);
                statement.setString(2, PlayerStats.GENERAL);
                statement.setString(3, period.id());
                statement.setObject(4, period.start(at, zone));
                statement.setLong(5, xp);
                statement.setTimestamp(6, Timestamp.from(at));
                statement.addBatch();
            }
            statement.executeBatch();
        } catch (SQLException exception) {
            throw new PersistenceException("bedwars.stats.add_xp", exception);
        }
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
        try (PreparedStatement statement = TransactionContext.connection().prepareStatement(TOPS.get(category))) {
            statement.setString(1, PlayerStats.GENERAL);
            statement.setString(2, StatsPeriod.TOTAL.id());
            statement.setObject(3, StatsPeriod.TOTAL_START);
            statement.setString(4, mode);
            statement.setString(5, period.id());
            statement.setObject(6, start);
            statement.setInt(7, limit);
            try (ResultSet rows = statement.executeQuery()) {
                List<TopEntry> entries = new ArrayList<>();
                while (rows.next()) {
                    entries.add(new TopEntry(
                        entries.size() + 1,
                        rows.getObject("player_id", UUID.class),
                        rows.getString("name"),
                        rows.getLong("value"),
                        rows.getLong("total_xp")
                    ));
                }
                return List.copyOf(entries);
            }
        } catch (SQLException exception) {
            throw new PersistenceException("bedwars.stats.top", exception);
        }
    }

    @Override
    public Set<String> favouriteMaps(UUID playerId) {
        try (PreparedStatement statement = TransactionContext.connection().prepareStatement(FAVOURITES)) {
            statement.setObject(1, playerId);
            try (ResultSet rows = statement.executeQuery()) {
                Set<String> arenas = new LinkedHashSet<>();
                while (rows.next()) {
                    arenas.add(rows.getString("arena"));
                }
                return Collections.unmodifiableSet(arenas);
            }
        } catch (SQLException exception) {
            throw new PersistenceException("bedwars.favourites.find", exception);
        }
    }

    @Override
    public boolean toggleFavouriteMap(UUID playerId, String arena, Instant at) {
        try (PreparedStatement remove = TransactionContext.connection().prepareStatement(UNFAVOURITE)) {
            remove.setObject(1, playerId);
            remove.setString(2, arena);
            if (remove.executeUpdate() > 0) {
                return false;
            }
            try (PreparedStatement add = TransactionContext.connection().prepareStatement(FAVOURITE)) {
                add.setObject(1, playerId);
                add.setString(2, arena);
                add.setTimestamp(3, Timestamp.from(at));
                add.executeUpdate();
                return true;
            }
        } catch (SQLException exception) {
            throw new PersistenceException("bedwars.favourites.toggle", exception);
        }
    }

    private static PlayerStats stats(UUID playerId, StatsPeriod period, LocalDate start) {
        try (PreparedStatement statement = TransactionContext.connection().prepareStatement(STATS)) {
            statement.setObject(1, playerId);
            statement.setString(2, period.id());
            statement.setObject(3, start);
            try (ResultSet rows = statement.executeQuery()) {
                Map<String, StatCounters> byMode = new HashMap<>();
                while (rows.next()) {
                    byMode.put(rows.getString("mode"), counters(rows));
                }
                return new PlayerStats(byMode);
            }
        } catch (SQLException exception) {
            throw new PersistenceException("bedwars.stats.find", exception);
        }
    }

    private static StatCounters counters(ResultSet rows) throws SQLException {
        return new StatCounters(
            rows.getLong("games"),
            rows.getLong("beds_broken"),
            rows.getLong("beds_lost"),
            rows.getLong("kills"),
            rows.getLong("deaths"),
            rows.getLong("assists"),
            rows.getLong("final_kills"),
            rows.getLong("final_deaths"),
            rows.getLong("final_assists"),
            rows.getLong("wins"),
            rows.getLong("losses"),
            rows.getLong("winstreak"),
            rows.getLong("best_winstreak"),
            rows.getLong("xp")
        );
    }

    private static Map<TopCategory, String> tops() {
        Map<TopCategory, String> tops = new EnumMap<>(TopCategory.class);
        for (TopCategory category : TopCategory.values()) {
            String column = switch (category) {
                case FINAL_KILLS -> "final_kills";
                case WINS -> "wins";
                case WINSTREAK -> "winstreak";
                case LEVEL -> "xp";
            };
            tops.put(category, TOP.formatted(column));
        }
        return Collections.unmodifiableMap(tops);
    }
}
