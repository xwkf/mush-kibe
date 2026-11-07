package br.com.muxe.core.common.persistence.sql;

import br.com.muxe.core.common.persistence.sql.type.SqlType;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public final class Column<T> {

    private final Table table;
    private final String name;
    private final SqlType<T> type;

    Column(Table table, String name, SqlType<T> type) {
        this.table = table;
        this.name = name;
        this.type = type;
    }

    public String name() {
        return name;
    }

    public SqlType<T> type() {
        return type;
    }

    public Optional<Table> table() {
        return Optional.ofNullable(table);
    }

    public Condition eq() {
        return new Condition(sql -> sql.column(this).text(" = ").bind(Binding.argument(this)));
    }

    public Condition eq(Column<T> other) {
        return new Condition(sql -> sql.column(this).text(" = ").column(other));
    }

    public Condition eqIgnoreCase() {
        return new Condition(sql -> sql.text("LOWER(").column(this).text(") = LOWER(").bind(Binding.argument(this)).text(")"));
    }

    public Condition in() {
        return new Condition(sql -> sql.column(this).text(" = ANY(").bind(Binding.many(this)).text(")"));
    }

    public Condition isNull() {
        return new Condition(sql -> sql.column(this).text(" IS NULL"));
    }

    public Condition isNotNull() {
        return new Condition(sql -> sql.column(this).text(" IS NOT NULL"));
    }

    public Argument is(T value) {
        return new Argument(this, value, false);
    }

    public Argument in(Collection<? extends T> values) {
        return new Argument(this, List.copyOf(Objects.requireNonNull(values, "values")), true);
    }

    public Order asc() {
        return new Order(this, false);
    }

    public Order desc() {
        return new Order(this, true);
    }

    String reference(boolean qualified) {
        return qualified && table != null ? table.name() + "." + name : name;
    }

    boolean belongsTo(Table other) {
        return table == other;
    }

    @Override
    public String toString() {
        return reference(true);
    }
}
