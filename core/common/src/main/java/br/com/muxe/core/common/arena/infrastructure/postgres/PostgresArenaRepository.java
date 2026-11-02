package br.com.muxe.core.common.arena.infrastructure.postgres;

import br.com.muxe.core.common.arena.domain.ArenaRecord;
import br.com.muxe.core.common.arena.domain.ArenaRepository;
import br.com.muxe.core.common.persistence.PersistenceException;
import br.com.muxe.core.common.persistence.transaction.TransactionContext;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class PostgresArenaRepository implements ArenaRepository {

    private static final String NAMES = "SELECT name FROM core.arenas WHERE game = ? ORDER BY name";

    private static final String FIND = "SELECT mode, ready, definition, map, updated_at FROM core.arenas WHERE game = ? AND name = ?";

    private static final String SAVE = """
        INSERT INTO core.arenas (game, name, mode, ready, definition, map, updated_at) VALUES (?, ?, ?, ?, ?, ?, ?)
        ON CONFLICT (game, name) DO UPDATE SET mode = EXCLUDED.mode, ready = EXCLUDED.ready, definition = EXCLUDED.definition,
            map = EXCLUDED.map, updated_at = EXCLUDED.updated_at
        """;

    private static final String SAVE_DEFINITION = """
        INSERT INTO core.arenas (game, name, mode, ready, definition, map, updated_at) VALUES (?, ?, ?, ?, ?, ?, ?)
        ON CONFLICT (game, name) DO UPDATE SET mode = EXCLUDED.mode, ready = EXCLUDED.ready, definition = EXCLUDED.definition,
            updated_at = EXCLUDED.updated_at
        """;

    private static final String DELETE = "DELETE FROM core.arenas WHERE game = ? AND name = ?";

    @Override
    public List<String> names(String game) {
        try (PreparedStatement statement = TransactionContext.connection().prepareStatement(NAMES)) {
            statement.setString(1, game);
            try (ResultSet rows = statement.executeQuery()) {
                List<String> names = new ArrayList<>();
                while (rows.next()) {
                    names.add(rows.getString("name"));
                }
                return List.copyOf(names);
            }
        } catch (SQLException exception) {
            throw new PersistenceException("arenas.names", exception);
        }
    }

    @Override
    public Optional<ArenaRecord> find(String game, String name) {
        try (PreparedStatement statement = TransactionContext.connection().prepareStatement(FIND)) {
            statement.setString(1, game);
            statement.setString(2, name);
            try (ResultSet rows = statement.executeQuery()) {
                if (!rows.next()) {
                    return Optional.empty();
                }
                return Optional.of(new ArenaRecord(game, name, rows.getString("mode"), rows.getBoolean("ready"), rows.getString("definition"),
                    rows.getBytes("map"), rows.getTimestamp("updated_at").toInstant()));
            }
        } catch (SQLException exception) {
            throw new PersistenceException("arenas.find", exception);
        }
    }

    @Override
    public void save(ArenaRecord arena, boolean keepMap) {
        try (PreparedStatement statement = TransactionContext.connection().prepareStatement(keepMap ? SAVE_DEFINITION : SAVE)) {
            statement.setString(1, arena.game());
            statement.setString(2, arena.name());
            statement.setString(3, arena.mode());
            statement.setBoolean(4, arena.ready());
            statement.setString(5, arena.definition());
            statement.setBytes(6, arena.map());
            statement.setTimestamp(7, Timestamp.from(arena.updatedAt()));
            statement.executeUpdate();
        } catch (SQLException exception) {
            throw new PersistenceException("arenas.save", exception);
        }
    }

    @Override
    public boolean delete(String game, String name) {
        try (PreparedStatement statement = TransactionContext.connection().prepareStatement(DELETE)) {
            statement.setString(1, game);
            statement.setString(2, name);
            return statement.executeUpdate() > 0;
        } catch (SQLException exception) {
            throw new PersistenceException("arenas.delete", exception);
        }
    }
}
