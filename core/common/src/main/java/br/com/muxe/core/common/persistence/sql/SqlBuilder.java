package br.com.muxe.core.common.persistence.sql;

import java.util.ArrayList;
import java.util.List;

final class SqlBuilder {

    private final StringBuilder sql = new StringBuilder();
    private final List<Binding> bindings = new ArrayList<>();
    private final boolean qualified;

    SqlBuilder(boolean qualified) {
        this.qualified = qualified;
    }

    SqlBuilder text(String text) {
        sql.append(text);
        return this;
    }

    SqlBuilder column(Column<?> column) {
        sql.append(column.reference(qualified));
        return this;
    }

    SqlBuilder columns(List<? extends Column<?>> columns) {
        for (int index = 0; index < columns.size(); index++) {
            if (index > 0) {
                sql.append(", ");
            }
            column(columns.get(index));
        }
        return this;
    }

    SqlBuilder bind(Binding binding) {
        sql.append('?');
        bindings.add(binding);
        return this;
    }

    SqlBuilder condition(Condition condition) {
        condition.render(this);
        return this;
    }

    String sql() {
        return sql.toString();
    }

    List<Binding> bindings() {
        return List.copyOf(bindings);
    }
}
