package br.com.muxe.core.common.persistence.sql;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class Select<R> {

    private final String name;
    private final String sql;
    private final List<Binding> bindings;
    private final Map<Column<?>, Integer> indexes;
    private final RowMapper<R> mapper;

    Select(String name, String sql, List<Binding> bindings, List<? extends Column<?>> columns, RowMapper<R> mapper) {
        this.name = Statements.name(name);
        this.sql = sql;
        this.bindings = List.copyOf(bindings);
        this.indexes = indexes(columns);
        this.mapper = mapper;
    }

    public Optional<R> first(Argument... arguments) {
        return Statements.run(name, sql, statement -> {
            Statements.bind(name, statement, bindings, null, arguments);
            try (ResultSet rows = statement.executeQuery()) {
                return rows.next() ? Optional.of(mapper.map(new Row(rows, indexes))) : Optional.empty();
            }
        });
    }

    public List<R> list(Argument... arguments) {
        return Statements.run(name, sql, statement -> {
            Statements.bind(name, statement, bindings, null, arguments);
            try (ResultSet rows = statement.executeQuery()) {
                Row row = new Row(rows, indexes);
                List<R> results = new ArrayList<>();
                while (rows.next()) {
                    results.add(mapper.map(row));
                }
                return Collections.unmodifiableList(results);
            }
        });
    }

    public boolean exists(Argument... arguments) {
        return Statements.run(name, sql, statement -> {
            Statements.bind(name, statement, bindings, null, arguments);
            try (ResultSet rows = statement.executeQuery()) {
                return rows.next();
            }
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

    private static Map<Column<?>, Integer> indexes(List<? extends Column<?>> columns) {
        Map<Column<?>, Integer> indexes = new HashMap<>();
        for (int index = 0; index < columns.size(); index++) {
            indexes.putIfAbsent(columns.get(index), index + 1);
        }
        return Map.copyOf(indexes);
    }
}
