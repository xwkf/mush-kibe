package br.com.muxe.core.common.persistence.sql.type;

import java.sql.ResultSet;
import java.sql.SQLException;

@FunctionalInterface
public interface ValueReader<T> {

    T read(ResultSet rows, int index) throws SQLException;
}
