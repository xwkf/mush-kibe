package br.com.muxe.core.common.persistence.sql;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class QueryBuilder<R> {

    private final String sql;
    private final List<Column<?>> parameters = new ArrayList<>();
    private final List<Column<?>> columns = new ArrayList<>();
    private RowMapper<?> mapper;

    QueryBuilder(String sql) {
        this.sql = Objects.requireNonNull(sql, "sql").strip();
    }

    public QueryBuilder<R> parameters(Column<?>... inOrder) {
        parameters.addAll(List.of(inOrder));
        return this;
    }

    public QueryBuilder<R> columns(Column<?>... inOrder) {
        columns.addAll(List.of(inOrder));
        return this;
    }

    @SuppressWarnings("unchecked")
    public <U> QueryBuilder<U> map(RowMapper<U> rowMapper) {
        this.mapper = Objects.requireNonNull(rowMapper, "rowMapper");
        return (QueryBuilder<U>) this;
    }

    @SuppressWarnings("unchecked")
    public Select<R> named(String name) {
        if (mapper == null) {
            throw new IllegalStateException("The query '" + name + "' needs a row mapper");
        }
        Placeholders.split(sql, parameters.size());
        List<Binding> bindings = parameters.stream().map(Binding::argument).toList();
        return new Select<>(name, sql, bindings, columns, (RowMapper<R>) mapper);
    }
}
