package br.com.muxe.core.common.placement.infrastructure.postgres;

import br.com.muxe.core.common.persistence.sql.Column;
import br.com.muxe.core.common.persistence.sql.InsertBuilder;
import br.com.muxe.core.common.persistence.sql.Row;
import br.com.muxe.core.common.persistence.sql.Table;
import br.com.muxe.core.common.persistence.sql.type.SqlType;
import br.com.muxe.core.common.placement.domain.Position;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

record PositionColumns(
    Column<String> world,
    Column<Double> x,
    Column<Double> y,
    Column<Double> z,
    Column<Float> yaw,
    Column<Float> pitch
) {

    static PositionColumns of(Table table, String prefix) {
        return new PositionColumns(
            table.column(prefix + "world", SqlType.TEXT),
            table.column(prefix + "x", SqlType.DOUBLE),
            table.column(prefix + "y", SqlType.DOUBLE),
            table.column(prefix + "z", SqlType.DOUBLE),
            table.column(prefix + "yaw", SqlType.REAL),
            table.column(prefix + "pitch", SqlType.REAL)
        );
    }

    List<Column<?>> all() {
        return List.of(world, x, y, z, yaw, pitch);
    }

    Position read(Row row) throws SQLException {
        return new Position(row.get(world), row.get(x), row.get(y), row.get(z), row.get(yaw), row.get(pitch));
    }

    Optional<Position> readOptional(Row row) throws SQLException {
        return row.get(world) == null ? Optional.empty() : Optional.of(read(row));
    }

    <T> InsertBuilder<T> values(InsertBuilder<T> insert, Function<? super T, Optional<Position>> position) {
        return insert
            .value(world, value -> position.apply(value).map(Position::world).orElse(null))
            .value(x, value -> position.apply(value).map(Position::x).orElse(null))
            .value(y, value -> position.apply(value).map(Position::y).orElse(null))
            .value(z, value -> position.apply(value).map(Position::z).orElse(null))
            .value(yaw, value -> position.apply(value).map(Position::yaw).orElse(null))
            .value(pitch, value -> position.apply(value).map(Position::pitch).orElse(null));
    }
}
