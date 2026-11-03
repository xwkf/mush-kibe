package br.com.muxe.minigame.bedwars.quickbuy.infrastructure.postgres;

import br.com.muxe.core.common.persistence.PersistenceException;
import br.com.muxe.core.common.persistence.transaction.TransactionContext;
import br.com.muxe.minigame.bedwars.quickbuy.domain.QuickBuy;
import br.com.muxe.minigame.bedwars.quickbuy.domain.QuickBuyRepository;
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

public final class PostgresQuickBuyRepository implements QuickBuyRepository {

    private static final String FIND = "SELECT slots FROM bedwars.quick_buy WHERE player_id = ?";

    private static final String SAVE = """
        INSERT INTO bedwars.quick_buy (player_id, slots, updated_at) VALUES (?, ?, ?)
        ON CONFLICT (player_id) DO UPDATE SET slots = EXCLUDED.slots, updated_at = EXCLUDED.updated_at
        """;

    @Override
    public Optional<QuickBuy> find(UUID playerId) {
        try (PreparedStatement statement = TransactionContext.connection().prepareStatement(FIND)) {
            statement.setObject(1, playerId);
            try (ResultSet rows = statement.executeQuery()) {
                if (!rows.next()) {
                    return Optional.empty();
                }
                Array slots = rows.getArray("slots");
                return Optional.of(QuickBuy.of(List.of((String[]) slots.getArray())));
            }
        } catch (SQLException exception) {
            throw new PersistenceException("bedwars.quick_buy.find", exception);
        }
    }

    @Override
    public void save(UUID playerId, QuickBuy quickBuy, Instant at) {
        Connection connection = TransactionContext.connection();
        try (PreparedStatement statement = connection.prepareStatement(SAVE)) {
            statement.setObject(1, playerId);
            statement.setArray(2, connection.createArrayOf("text", quickBuy.slots().toArray(String[]::new)));
            statement.setTimestamp(3, Timestamp.from(at));
            statement.executeUpdate();
        } catch (SQLException exception) {
            throw new PersistenceException("bedwars.quick_buy.save", exception);
        }
    }
}
