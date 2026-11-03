package br.com.muxe.minigame.bedwars.hotbar.infrastructure.postgres;

import br.com.muxe.core.common.persistence.PersistenceException;
import br.com.muxe.core.common.persistence.transaction.TransactionContext;
import br.com.muxe.minigame.bedwars.hotbar.domain.HotbarLayout;
import br.com.muxe.minigame.bedwars.hotbar.domain.HotbarRepository;
import java.sql.Array;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public final class PostgresHotbarRepository implements HotbarRepository {

    private static final String FIND = "SELECT slots FROM bedwars.hotbar_layout WHERE player_id = ?";

    private static final String SAVE = """
        INSERT INTO bedwars.hotbar_layout (player_id, slots, updated_at) VALUES (?, ?, ?)
        ON CONFLICT (player_id) DO UPDATE SET slots = EXCLUDED.slots, updated_at = EXCLUDED.updated_at
        """;

    @Override
    public Optional<HotbarLayout> find(UUID playerId) {
        try (PreparedStatement statement = TransactionContext.connection().prepareStatement(FIND)) {
            statement.setObject(1, playerId);
            try (ResultSet rows = statement.executeQuery()) {
                if (!rows.next()) {
                    return Optional.empty();
                }
                Array slots = rows.getArray("slots");
                return Optional.of(HotbarLayout.decode(List.of((String[]) slots.getArray())));
            }
        } catch (SQLException exception) {
            throw new PersistenceException("bedwars.hotbar.find", exception);
        }
    }

    @Override
    public void save(UUID playerId, HotbarLayout layout, Instant at) {
        Connection connection = TransactionContext.connection();
        try (PreparedStatement statement = connection.prepareStatement(SAVE)) {
            statement.setObject(1, playerId);
            statement.setArray(2, connection.createArrayOf("text", layout.encode().toArray(String[]::new)));
            statement.setTimestamp(3, Timestamp.from(at));
            statement.executeUpdate();
        } catch (SQLException exception) {
            throw new PersistenceException("bedwars.hotbar.save", exception);
        }
    }
}
