package br.com.muxe.core.common.persistence.sql;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

public final class Row {

    private final ResultSet rows;
    private final Map<Column<?>, Integer> indexes;

    Row(ResultSet rows, Map<Column<?>, Integer> indexes) {
        this.rows = rows;
        this.indexes = indexes;
    }

    public <T> T get(Column<T> column) throws SQLException {
        return column.type().read(rows, index(column));
    }

    public boolean isNull(Column<?> column) throws SQLException {
        rows.getObject(index(column));
        return rows.wasNull();
    }

    private int index(Column<?> column) {
        Integer index = indexes.get(column);
        if (index == null) {
            throw new IllegalArgumentException("The column " + column + " is not part of this result");
        }
        return index;
    }
}
