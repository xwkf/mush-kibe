package br.com.muxe.core.common.persistence.sql;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

public final class InsertBuilder<T> {

    private final Table table;
    private final List<Column<?>> columns = new ArrayList<>();
    private final List<Value> values = new ArrayList<>();
    private final List<Assignment> updates = new ArrayList<>();
    private String alias;
    private List<Column<?>> conflictTarget;
    private boolean doNothing;
    private String updateWhere;

    InsertBuilder(Table table) {
        this.table = Objects.requireNonNull(table, "table");
    }

    public <V> InsertBuilder<T> value(Column<V> column, Function<? super T, ? extends V> getter) {
        return add(column, new Value(Binding.entity(column, getter), null));
    }

    public InsertBuilder<T> value(Column<?> column) {
        return add(column, new Value(Binding.argument(column), null));
    }

    public InsertBuilder<T> valueSql(Column<?> column, String expression) {
        return add(column, new Value(null, Objects.requireNonNull(expression, "expression")));
    }

    public InsertBuilder<T> as(String tableAlias) {
        this.alias = Table.identifier(tableAlias);
        return this;
    }

    public InsertBuilder<T> onConflict(Column<?>... target) {
        for (Column<?> column : target) {
            owned(column);
        }
        this.conflictTarget = List.of(target);
        return this;
    }

    public InsertBuilder<T> doNothing() {
        conflict().doNothing = true;
        return this;
    }

    public InsertBuilder<T> doUpdate(Column<?>... excluded) {
        conflict();
        for (Column<?> column : excluded) {
            updates.add(new Assignment(owned(column), "EXCLUDED." + column.name()));
        }
        return this;
    }

    public InsertBuilder<T> doUpdate(Column<?> column, String expression) {
        conflict();
        updates.add(new Assignment(owned(column), Objects.requireNonNull(expression, "expression")));
        return this;
    }

    public InsertBuilder<T> where(String condition) {
        conflict().updateWhere = Objects.requireNonNull(condition, "condition");
        return this;
    }

    public Write<T> named(String name) {
        if (columns.isEmpty()) {
            throw new IllegalStateException("The insert '" + name + "' has no values");
        }
        if (conflictTarget != null && doNothing != updates.isEmpty()) {
            throw new IllegalStateException("The insert '" + name + "' must either do nothing or update on conflict");
        }
        if (updateWhere != null && updates.isEmpty()) {
            throw new IllegalStateException("The insert '" + name + "' has a conflict condition but updates nothing");
        }
        SqlBuilder sql = new SqlBuilder(false);
        sql.text("INSERT INTO ").text(table.qualifiedName());
        if (alias != null) {
            sql.text(" AS ").text(alias);
        }
        sql.text(" (").columns(columns).text(") VALUES (");
        for (int index = 0; index < values.size(); index++) {
            if (index > 0) {
                sql.text(", ");
            }
            Value value = values.get(index);
            if (value.binding() != null) {
                sql.bind(value.binding());
            } else {
                sql.text(value.expression());
            }
        }
        sql.text(")");
        if (conflictTarget != null) {
            sql.text(" ON CONFLICT");
            if (!conflictTarget.isEmpty()) {
                sql.text(" (").columns(conflictTarget).text(")");
            }
            if (doNothing) {
                sql.text(" DO NOTHING");
            } else {
                sql.text(" DO UPDATE SET ");
                for (int index = 0; index < updates.size(); index++) {
                    if (index > 0) {
                        sql.text(", ");
                    }
                    sql.column(updates.get(index).column()).text(" = ").text(updates.get(index).expression());
                }
                if (updateWhere != null) {
                    sql.text(" WHERE ").text(updateWhere);
                }
            }
        }
        return new Write<>(name, sql.sql(), sql.bindings());
    }

    private InsertBuilder<T> add(Column<?> column, Value value) {
        owned(column);
        if (columns.contains(column)) {
            throw new IllegalArgumentException("The column " + column + " is already inserted");
        }
        columns.add(column);
        values.add(value);
        return this;
    }

    private InsertBuilder<T> conflict() {
        if (conflictTarget == null) {
            throw new IllegalStateException("Call onConflict before choosing what happens on a conflict");
        }
        return this;
    }

    private Column<?> owned(Column<?> column) {
        if (!column.belongsTo(table)) {
            throw new IllegalArgumentException("The column " + column + " does not belong to " + table);
        }
        return column;
    }

    private record Value(Binding binding, String expression) {
    }

    private record Assignment(Column<?> column, String expression) {
    }
}
