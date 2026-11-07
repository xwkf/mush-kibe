package br.com.muxe.core.common.persistence.sql;

import br.com.muxe.core.common.persistence.sql.type.SqlType;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

public final class Table {

    private static final Pattern IDENTIFIER = Pattern.compile("[a-z_][a-z0-9_]*");

    private final String schema;
    private final String name;
    private final List<Column<?>> columns = new ArrayList<>();

    private Table(String schema, String name) {
        this.schema = identifier(schema);
        this.name = identifier(name);
    }

    public static Table of(String schema, String name) {
        return new Table(schema, name);
    }

    public synchronized <T> Column<T> column(String name, SqlType<T> type) {
        String column = identifier(name);
        if (columns.stream().anyMatch(existing -> existing.name().equals(column))) {
            throw new IllegalArgumentException("The table " + qualifiedName() + " already has a column " + column);
        }
        Column<T> created = new Column<>(this, column, Objects.requireNonNull(type, "type"));
        columns.add(created);
        return created;
    }

    public String schema() {
        return schema;
    }

    public String name() {
        return name;
    }

    public String qualifiedName() {
        return schema + "." + name;
    }

    public synchronized List<Column<?>> columns() {
        return List.copyOf(columns);
    }

    @Override
    public String toString() {
        return qualifiedName();
    }

    static String identifier(String value) {
        Objects.requireNonNull(value, "identifier");
        if (!IDENTIFIER.matcher(value).matches()) {
            throw new IllegalArgumentException("Invalid SQL identifier: " + value);
        }
        return value;
    }
}
