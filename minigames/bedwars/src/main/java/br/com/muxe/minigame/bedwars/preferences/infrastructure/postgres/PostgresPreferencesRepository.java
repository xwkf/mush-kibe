package br.com.muxe.minigame.bedwars.preferences.infrastructure.postgres;

import br.com.muxe.core.common.persistence.PersistenceException;
import br.com.muxe.core.common.persistence.transaction.TransactionContext;
import br.com.muxe.minigame.bedwars.preferences.domain.Preferences;
import br.com.muxe.minigame.bedwars.preferences.domain.Preferences.ChestDeposit;
import br.com.muxe.minigame.bedwars.preferences.domain.PreferencesRepository;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public final class PostgresPreferencesRepository implements PreferencesRepository {

    private static final String FIND = "SELECT show_level, tnt_timer, chest_deposit FROM bedwars.preferences WHERE player_id = ?";

    private static final String SAVE = """
        INSERT INTO bedwars.preferences (player_id, show_level, tnt_timer, chest_deposit, updated_at) VALUES (?, ?, ?, ?, ?)
        ON CONFLICT (player_id) DO UPDATE SET show_level = EXCLUDED.show_level, tnt_timer = EXCLUDED.tnt_timer,
            chest_deposit = EXCLUDED.chest_deposit, updated_at = EXCLUDED.updated_at
        """;

    @Override
    public Optional<Preferences> find(UUID playerId) {
        try (PreparedStatement statement = TransactionContext.connection().prepareStatement(FIND)) {
            statement.setObject(1, playerId);
            try (ResultSet rows = statement.executeQuery()) {
                if (!rows.next()) {
                    return Optional.empty();
                }
                return Optional.of(new Preferences(rows.getBoolean("show_level"), rows.getBoolean("tnt_timer"),
                    ChestDeposit.valueOf(rows.getString("chest_deposit"))));
            }
        } catch (SQLException exception) {
            throw new PersistenceException("bedwars.preferences.find", exception);
        }
    }

    @Override
    public void save(UUID playerId, Preferences preferences, Instant at) {
        try (PreparedStatement statement = TransactionContext.connection().prepareStatement(SAVE)) {
            statement.setObject(1, playerId);
            statement.setBoolean(2, preferences.showLevel());
            statement.setBoolean(3, preferences.tntTimer());
            statement.setString(4, preferences.chestDeposit().name());
            statement.setTimestamp(5, Timestamp.from(at));
            statement.executeUpdate();
        } catch (SQLException exception) {
            throw new PersistenceException("bedwars.preferences.save", exception);
        }
    }
}
