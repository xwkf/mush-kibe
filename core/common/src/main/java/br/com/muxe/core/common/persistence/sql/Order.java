package br.com.muxe.core.common.persistence.sql;

public final class Order {

    private final Column<?> column;
    private final boolean descending;

    Order(Column<?> column, boolean descending) {
        this.column = column;
        this.descending = descending;
    }

    void render(SqlBuilder builder) {
        builder.column(column);
        if (descending) {
            builder.text(" DESC");
        }
    }
}
