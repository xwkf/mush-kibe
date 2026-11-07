package br.com.muxe.core.common.persistence.sql;

import br.com.muxe.core.common.persistence.sql.type.SqlType;
import java.util.List;
import java.util.Objects;

public final class Sql {

    private Sql() {
    }

    public static SelectBuilder<Object> select(Column<?>... columns) {
        return new SelectBuilder<>(false, List.of(columns));
    }

    public static SelectBuilder<Object> select(List<? extends Column<?>> columns) {
        return new SelectBuilder<>(false, List.copyOf(columns));
    }

    public static SelectBuilder<Object> selectDistinct(Column<?>... columns) {
        return new SelectBuilder<>(true, List.of(columns));
    }

    public static QueryBuilder<Object> query(String sql) {
        return new QueryBuilder<>(sql);
    }

    public static <T> InsertBuilder<T> insertInto(Table table) {
        return new InsertBuilder<>(table);
    }

    public static <T> UpdateBuilder<T> update(Table table) {
        return new UpdateBuilder<>(table);
    }

    public static DeleteBuilder deleteFrom(Table table) {
        return new DeleteBuilder(table);
    }

    public static <T> StatementBuilder<T> statement(String sql) {
        return new StatementBuilder<>(sql);
    }

    public static <T> Column<T> parameter(String name, SqlType<T> type) {
        return new Column<>(null, Table.identifier(name), Objects.requireNonNull(type, "type"));
    }
}
