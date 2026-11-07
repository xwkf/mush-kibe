package br.com.muxe.core.common.persistence.sql;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

public final class UpdateBuilder<T> {

    private final Table table;
    private final List<Condition> assignments = new ArrayList<>();
    private Condition where;

    UpdateBuilder(Table table) {
        this.table = Objects.requireNonNull(table, "table");
    }

    public <V> UpdateBuilder<T> set(Column<V> column, Function<? super T, ? extends V> getter) {
        owned(column);
        assignments.add(new Condition(sql -> sql.column(column).text(" = ").bind(Binding.entity(column, getter))));
        return this;
    }

    public UpdateBuilder<T> set(Column<?> column) {
        owned(column);
        assignments.add(new Condition(sql -> sql.column(column).text(" = ").bind(Binding.argument(column))));
        return this;
    }

    public UpdateBuilder<T> setSql(Column<?> column, String expression) {
        owned(column);
        Objects.requireNonNull(expression, "expression");
        assignments.add(new Condition(sql -> sql.column(column).text(" = ").text(expression)));
        return this;
    }

    public <V> UpdateBuilder<T> where(Column<V> column, Function<? super T, ? extends V> getter) {
        return where(new Condition(sql -> sql.column(column).text(" = ").bind(Binding.entity(column, getter))));
    }

    public UpdateBuilder<T> where(Condition condition) {
        where = where == null ? condition : where.and(condition);
        return this;
    }

    public Write<T> named(String name) {
        if (assignments.isEmpty()) {
            throw new IllegalStateException("The update '" + name + "' sets nothing");
        }
        if (where == null) {
            throw new IllegalStateException("The update '" + name + "' needs a condition");
        }
        SqlBuilder sql = new SqlBuilder(false);
        sql.text("UPDATE ").text(table.qualifiedName()).text(" SET ");
        for (int index = 0; index < assignments.size(); index++) {
            if (index > 0) {
                sql.text(", ");
            }
            sql.condition(assignments.get(index));
        }
        sql.text(" WHERE ").condition(where);
        return new Write<>(name, sql.sql(), sql.bindings());
    }

    private void owned(Column<?> column) {
        if (!column.belongsTo(table)) {
            throw new IllegalArgumentException("The column " + column + " does not belong to " + table);
        }
    }
}
