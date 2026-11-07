package br.com.muxe.core.common.placement.infrastructure.postgres;

import br.com.muxe.core.common.persistence.sql.Column;
import br.com.muxe.core.common.persistence.sql.InsertBuilder;
import br.com.muxe.core.common.persistence.sql.Sql;
import br.com.muxe.core.common.persistence.sql.Table;
import br.com.muxe.core.common.persistence.sql.Write;
import br.com.muxe.core.common.persistence.sql.type.SqlType;
import br.com.muxe.core.common.placement.domain.Placement;
import br.com.muxe.core.common.placement.domain.Position;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

record PlacementTable(Table table, Column<String> group, Column<String> id, PositionColumns position) {

    static PlacementTable of(String name) {
        Table table = Table.of("core", name);
        return new PlacementTable(table, table.column("server_group", SqlType.TEXT), table.column("id", SqlType.TEXT), PositionColumns.of(table, ""));
    }

    List<Column<?>> keyAndPosition() {
        List<Column<?>> columns = new ArrayList<>(List.of(id));
        columns.addAll(position.all());
        return columns;
    }

    <T extends Placement> InsertBuilder<T> insert() {
        InsertBuilder<T> insert = Sql.<T>insertInto(table)
            .value(group, Placement::group)
            .value(id, Placement::id);
        return position.values(insert, placement -> Optional.<Position>of(placement.position()));
    }

    Write<Void> delete(String name) {
        return Sql.deleteFrom(table).where(group.eq().and(id.eq())).named(name);
    }
}
