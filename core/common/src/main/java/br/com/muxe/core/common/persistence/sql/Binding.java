package br.com.muxe.core.common.persistence.sql;

import java.util.function.Function;

record Binding(Column<?> column, Function<Object, ?> getter, boolean many) {

    static Binding argument(Column<?> column) {
        return new Binding(column, null, false);
    }

    static Binding many(Column<?> column) {
        return new Binding(column, null, true);
    }

    @SuppressWarnings("unchecked")
    static <T> Binding entity(Column<?> column, Function<? super T, ?> getter) {
        return new Binding(column, (Function<Object, ?>) getter, false);
    }

    boolean fromEntity() {
        return getter != null;
    }

    boolean accepts(Argument argument) {
        return !fromEntity() && argument.column() == column && argument.many() == many;
    }
}
