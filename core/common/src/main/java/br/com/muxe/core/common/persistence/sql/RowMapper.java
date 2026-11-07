package br.com.muxe.core.common.persistence.sql;

import java.sql.SQLException;

@FunctionalInterface
public interface RowMapper<R> {

    R map(Row row) throws SQLException;
}
