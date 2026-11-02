package br.com.muxe.core.common.mission.infrastructure.postgres;

import br.com.muxe.core.common.mission.domain.MissionProgress;
import br.com.muxe.core.common.mission.domain.MissionRepository;
import br.com.muxe.core.common.persistence.PersistenceException;
import br.com.muxe.core.common.persistence.transaction.TransactionContext;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class PostgresMissionRepository implements MissionRepository {

    private static final String FIND = """
        SELECT progress, completed FROM core.mission_progress
        WHERE player_id = ? AND game = ? AND mission = ? AND period_start = ?
        """;

    private static final String SAVE = """
        INSERT INTO core.mission_progress (player_id, game, mission, period_start, progress, completed) VALUES (?, ?, ?, ?, ?, ?)
        ON CONFLICT (player_id, game, mission, period_start) DO UPDATE SET progress = EXCLUDED.progress, completed = EXCLUDED.completed
        """;

    @Override
    public Map<String, MissionProgress> find(String game, UUID playerId, Map<String, LocalDate> missions) {
        Map<String, MissionProgress> found = new HashMap<>();
        try (PreparedStatement statement = TransactionContext.connection().prepareStatement(FIND)) {
            for (Map.Entry<String, LocalDate> mission : missions.entrySet()) {
                statement.setObject(1, playerId);
                statement.setString(2, game);
                statement.setString(3, mission.getKey());
                statement.setObject(4, mission.getValue());
                try (ResultSet rows = statement.executeQuery()) {
                    if (rows.next()) {
                        found.put(mission.getKey(), new MissionProgress(mission.getKey(), mission.getValue(), rows.getLong("progress"), rows.getBoolean("completed")));
                    }
                }
            }
            return found;
        } catch (SQLException exception) {
            throw new PersistenceException("missions.find", exception);
        }
    }

    @Override
    public void save(String game, UUID playerId, MissionProgress progress) {
        try (PreparedStatement statement = TransactionContext.connection().prepareStatement(SAVE)) {
            statement.setObject(1, playerId);
            statement.setString(2, game);
            statement.setString(3, progress.mission());
            statement.setObject(4, progress.start());
            statement.setLong(5, progress.progress());
            statement.setBoolean(6, progress.completed());
            statement.executeUpdate();
        } catch (SQLException exception) {
            throw new PersistenceException("missions.save", exception);
        }
    }
}
