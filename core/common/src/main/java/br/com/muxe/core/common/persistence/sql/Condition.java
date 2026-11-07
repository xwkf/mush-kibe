package br.com.muxe.core.common.persistence.sql;

import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

public final class Condition {

    private final Consumer<SqlBuilder> renderer;

    Condition(Consumer<SqlBuilder> renderer) {
        this.renderer = renderer;
    }

    public static Condition sql(String sql, Column<?>... parameters) {
        List<String> parts = Placeholders.split(sql, parameters.length);
        List<Column<?>> columns = List.of(parameters);
        return new Condition(builder -> {
            builder.text("(").text(parts.getFirst());
            for (int index = 0; index < columns.size(); index++) {
                builder.bind(Binding.argument(columns.get(index))).text(parts.get(index + 1));
            }
            builder.text(")");
        });
    }

    public Condition and(Condition other) {
        Objects.requireNonNull(other, "other");
        return new Condition(builder -> builder.condition(this).text(" AND ").condition(other));
    }

    void render(SqlBuilder builder) {
        renderer.accept(builder);
    }
}
