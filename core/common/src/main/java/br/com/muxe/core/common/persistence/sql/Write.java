package br.com.muxe.core.common.persistence.sql;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;

public final class Write<T> {

    private static final Argument[] NO_ARGUMENTS = new Argument[0];

    private final String name;
    private final String sql;
    private final List<Binding> bindings;

    Write(String name, String sql, List<Binding> bindings) {
        this.name = Statements.name(name);
        this.sql = sql;
        this.bindings = List.copyOf(bindings);
    }

    public int execute(T value) {
        return Statements.run(name, sql, statement -> {
            Statements.bind(name, statement, bindings, value, NO_ARGUMENTS);
            return count(statement);
        });
    }

    public int execute(Argument... arguments) {
        return Statements.run(name, sql, statement -> {
            Statements.bind(name, statement, bindings, null, arguments);
            return count(statement);
        });
    }

    public void batch(Collection<? extends T> values) {
        if (values.isEmpty()) {
            return;
        }
        Statements.run(name, sql, statement -> {
            for (T value : values) {
                Statements.bind(name, statement, bindings, value, NO_ARGUMENTS);
                statement.addBatch();
            }
            statement.executeBatch();
            return null;
        });
    }

    public String name() {
        return name;
    }

    public String sql() {
        return sql;
    }

    @Override
    public String toString() {
        return name + ": " + sql;
    }

    private static int count(PreparedStatement statement) throws SQLException {
        if (statement.execute()) {
            return 0;
        }
        return Math.max(0, statement.getUpdateCount());
    }
}
