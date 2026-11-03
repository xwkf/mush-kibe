package br.com.muxe.minigame.bedwars.teamcolor.infrastructure.postgres;

import br.com.muxe.core.common.persistence.PersistenceException;
import br.com.muxe.core.common.persistence.transaction.TransactionContext;
import br.com.muxe.minigame.bedwars.teamcolor.domain.FavouriteColorRepository;
import br.com.muxe.minigame.engine.team.TeamColor;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public final class PostgresFavouriteColorRepository implements FavouriteColorRepository {

    private static final String FIND = "SELECT team_color FROM bedwars.favourite_team_color WHERE player_id = ?";

    private static final String SAVE = """
        INSERT INTO bedwars.favourite_team_color (player_id, team_color, updated_at) VALUES (?, ?, ?)
        ON CONFLICT (player_id) DO UPDATE SET team_color = EXCLUDED.team_color, updated_at = EXCLUDED.updated_at
        """;

    private static final String CLEAR = "DELETE FROM bedwars.favourite_team_color WHERE player_id = ?";

    @Override
    public Optional<TeamColor> find(UUID playerId) {
        try (PreparedStatement statement = TransactionContext.connection().prepareStatement(FIND)) {
            statement.setObject(1, playerId);
            try (ResultSet rows = statement.executeQuery()) {
                return rows.next() ? TeamColor.find(rows.getString("team_color")) : Optional.empty();
            }
        } catch (SQLException exception) {
            throw new PersistenceException("bedwars.team-color.find", exception);
        }
    }

    @Override
    public void save(UUID playerId, TeamColor color, Instant at) {
        try (PreparedStatement statement = TransactionContext.connection().prepareStatement(SAVE)) {
            statement.setObject(1, playerId);
            statement.setString(2, color.id());
            statement.setTimestamp(3, Timestamp.from(at));
            statement.executeUpdate();
        } catch (SQLException exception) {
            throw new PersistenceException("bedwars.team-color.save", exception);
        }
    }

    @Override
    public void clear(UUID playerId) {
        try (PreparedStatement statement = TransactionContext.connection().prepareStatement(CLEAR)) {
            statement.setObject(1, playerId);
            statement.executeUpdate();
        } catch (SQLException exception) {
            throw new PersistenceException("bedwars.team-color.clear", exception);
        }
    }
}
