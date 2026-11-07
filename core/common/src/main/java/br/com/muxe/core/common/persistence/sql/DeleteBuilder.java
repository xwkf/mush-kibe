package br.com.muxe.core.common.persistence.sql;

import java.util.Objects;

public final class DeleteBuilder {

    private final Table table;
    private Condition where;

    DeleteBuilder(Table table) {
        this.table = Objects.requireNonNull(table, "table");
    }

    public DeleteBuilder where(Condition condition) {
        where = where == null ? condition : where.and(condition);
        return this;
    }

    public Write<Void> named(String name) {
        if (where == null) {
            throw new IllegalStateException("The delete '" + name + "' needs a condition");
        }
        SqlBuilder sql = new SqlBuilder(false);
        sql.text("DELETE FROM ").text(table.qualifiedName()).text(" WHERE ").condition(where);
        return new Write<>(name, sql.sql(), sql.bindings());
    }
}
