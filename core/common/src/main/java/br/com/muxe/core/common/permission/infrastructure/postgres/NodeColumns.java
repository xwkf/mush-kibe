package br.com.muxe.core.common.permission.infrastructure.postgres;

import br.com.muxe.core.common.permission.domain.Contexts;
import br.com.muxe.core.common.permission.domain.PermissionNode;
import br.com.muxe.core.common.persistence.sql.Column;
import br.com.muxe.core.common.persistence.sql.InsertBuilder;
import br.com.muxe.core.common.persistence.sql.Row;
import br.com.muxe.core.common.persistence.sql.Table;
import br.com.muxe.core.common.persistence.sql.type.SqlType;
import java.sql.SQLException;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

record NodeColumns(
    Column<String> permission,
    Column<Boolean> value,
    Column<Contexts> contexts,
    Column<Optional<Instant>> expiresAt,
    Column<Instant> addedAt,
    Column<Instant> updatedAt
) {

    static NodeColumns of(Table table) {
        return new NodeColumns(
            table.column("permission", SqlType.TEXT),
            table.column("value", SqlType.BOOLEAN),
            table.column("contexts", SqlType.TEXT.map(Contexts::parse, Contexts::encode)),
            table.column("expires_at", SqlType.TIMESTAMPTZ.optional()),
            table.column("added_at", SqlType.TIMESTAMPTZ),
            table.column("updated_at", SqlType.TIMESTAMPTZ)
        );
    }

    List<Column<?>> all() {
        return List.of(permission, value, contexts, expiresAt, addedAt, updatedAt);
    }

    PermissionNode read(Row row) throws SQLException {
        return new PermissionNode(row.get(permission), row.get(value), row.get(contexts), row.get(expiresAt), row.get(addedAt), row.get(updatedAt));
    }

    <T> InsertBuilder<T> values(InsertBuilder<T> insert, Function<? super T, PermissionNode> node) {
        return insert
            .value(permission, row -> node.apply(row).key())
            .value(value, row -> node.apply(row).value())
            .value(contexts, row -> node.apply(row).contexts())
            .value(expiresAt, row -> node.apply(row).expiresAt())
            .value(addedAt, row -> node.apply(row).addedAt())
            .value(updatedAt, row -> node.apply(row).updatedAt());
    }
}
