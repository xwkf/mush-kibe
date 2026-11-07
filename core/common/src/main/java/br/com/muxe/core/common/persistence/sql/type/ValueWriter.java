package br.com.muxe.core.common.persistence.sql.type;

import java.sql.PreparedStatement;
import java.sql.SQLException;

@FunctionalInterface
public interface ValueWriter<T> {

    void write(PreparedStatement statement, int index, T value) throws SQLException;
}
