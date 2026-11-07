package br.com.muxe.core.common.persistence.sql;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

public final class StatementBuilder<T> {

    private final String sql;
    private final List<Binding> bindings = new ArrayList<>();

    StatementBuilder(String sql) {
        this.sql = Objects.requireNonNull(sql, "sql").strip();
    }

    public <V> StatementBuilder<T> parameter(Column<V> column, Function<? super T, ? extends V> getter) {
        bindings.add(Binding.entity(column, getter));
        return this;
    }

    public StatementBuilder<T> parameter(Column<?> column) {
        bindings.add(Binding.argument(column));
        return this;
    }

    public Write<T> named(String name) {
        Placeholders.split(sql, bindings.size());
        return new Write<>(name, sql, bindings);
    }
}
