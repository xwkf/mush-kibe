package br.com.muxe.core.common.persistence.sql;

public final class Argument {

    private final Column<?> column;
    private final Object value;
    private final boolean many;

    Argument(Column<?> column, Object value, boolean many) {
        this.column = column;
        this.value = value;
        this.many = many;
    }

    public Column<?> column() {
        return column;
    }

    public Object value() {
        return value;
    }

    public boolean many() {
        return many;
    }

    @Override
    public String toString() {
        return column + (many ? " in " : " = ") + value;
    }
}
