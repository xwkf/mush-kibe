package br.com.muxe.core.common.persistence.sql;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class SelectBuilder<R> {

    private final boolean distinct;
    private final List<Column<?>> columns;
    private final List<Join> joins = new ArrayList<>();
    private final List<Order> orders = new ArrayList<>();
    private Table from;
    private Condition where;
    private Column<Integer> limit;
    private RowMapper<?> mapper;

    SelectBuilder(boolean distinct, List<Column<?>> columns) {
        if (columns.isEmpty()) {
            throw new IllegalArgumentException("A select needs at least one column");
        }
        this.distinct = distinct;
        this.columns = List.copyOf(columns);
    }

    public SelectBuilder<R> from(Table table) {
        this.from = Objects.requireNonNull(table, "table");
        return this;
    }

    public SelectBuilder<R> join(Table table, Condition on) {
        joins.add(new Join("JOIN", table, on));
        return this;
    }

    public SelectBuilder<R> leftJoin(Table table, Condition on) {
        joins.add(new Join("LEFT JOIN", table, on));
        return this;
    }

    public SelectBuilder<R> where(Condition condition) {
        where = where == null ? condition : where.and(condition);
        return this;
    }

    public SelectBuilder<R> orderBy(Order... order) {
        orders.addAll(List.of(order));
        return this;
    }

    public SelectBuilder<R> limit(Column<Integer> parameter) {
        this.limit = Objects.requireNonNull(parameter, "parameter");
        return this;
    }

    @SuppressWarnings("unchecked")
    public <U> SelectBuilder<U> map(RowMapper<U> rowMapper) {
        this.mapper = Objects.requireNonNull(rowMapper, "rowMapper");
        return (SelectBuilder<U>) this;
    }

    @SuppressWarnings("unchecked")
    public Select<R> named(String name) {
        if (from == null) {
            throw new IllegalStateException("The select '" + name + "' needs a table");
        }
        if (mapper == null) {
            throw new IllegalStateException("The select '" + name + "' needs a row mapper");
        }
        SqlBuilder sql = new SqlBuilder(!joins.isEmpty());
        sql.text(distinct ? "SELECT DISTINCT " : "SELECT ").columns(columns).text(" FROM ").text(from.qualifiedName());
        for (Join join : joins) {
            sql.text(" " + join.kind() + " ").text(join.table().qualifiedName()).text(" ON ").condition(join.on());
        }
        if (where != null) {
            sql.text(" WHERE ").condition(where);
        }
        if (!orders.isEmpty()) {
            sql.text(" ORDER BY ");
            for (int index = 0; index < orders.size(); index++) {
                if (index > 0) {
                    sql.text(", ");
                }
                orders.get(index).render(sql);
            }
        }
        if (limit != null) {
            sql.text(" LIMIT ").bind(Binding.argument(limit));
        }
        return new Select<>(name, sql.sql(), sql.bindings(), columns, (RowMapper<R>) mapper);
    }

    private record Join(String kind, Table table, Condition on) {
    }
}
