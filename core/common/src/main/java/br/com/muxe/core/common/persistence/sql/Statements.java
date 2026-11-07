package br.com.muxe.core.common.persistence.sql;

import br.com.muxe.core.common.persistence.PersistenceException;
import br.com.muxe.core.common.persistence.sql.observe.StatementObserver;
import br.com.muxe.core.common.persistence.sql.type.SqlType;
import br.com.muxe.core.common.persistence.transaction.TransactionContext;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;

final class Statements {

    private Statements() {
    }

    static String name(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Every statement needs a name for its metrics");
        }
        return name;
    }

    static <R> R run(String name, String sql, Work<R> work) {
        Connection connection = TransactionContext.connection();
        StatementObserver observer = TransactionContext.observer();
        long start = System.nanoTime();
        boolean success = false;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            R result = work.run(statement);
            success = true;
            return result;
        } catch (SQLException exception) {
            throw new PersistenceException(name, exception);
        } finally {
            observer.executed(name, System.nanoTime() - start, success);
        }
    }

    static void bind(String name, PreparedStatement statement, List<Binding> bindings, Object entity, Argument[] arguments) throws SQLException {
        for (Argument argument : arguments) {
            if (bindings.stream().noneMatch(binding -> binding.accepts(argument))) {
                throw new IllegalArgumentException("The statement '" + name + "' has no parameter for " + argument.column() + (argument.many() ? " (ANY)" : ""));
            }
        }
        for (int position = 0; position < bindings.size(); position++) {
            Binding binding = bindings.get(position);
            int index = position + 1;
            if (binding.fromEntity()) {
                if (entity == null) {
                    throw new IllegalStateException("The statement '" + name + "' reads " + binding.column() + " from a value; execute it with one");
                }
                write(statement, index, binding.column(), binding.getter().apply(entity));
            } else {
                Argument argument = argument(name, binding, arguments);
                if (binding.many()) {
                    writeArray(statement, index, binding.column(), (Collection<?>) argument.value());
                } else {
                    write(statement, index, binding.column(), argument.value());
                }
            }
        }
    }

    private static Argument argument(String name, Binding binding, Argument[] arguments) {
        for (Argument argument : arguments) {
            if (binding.accepts(argument)) {
                return argument;
            }
        }
        throw new IllegalArgumentException("The statement '" + name + "' needs a value for " + binding.column() + (binding.many() ? " (ANY)" : ""));
    }

    @SuppressWarnings("unchecked")
    private static void write(PreparedStatement statement, int index, Column<?> column, Object value) throws SQLException {
        ((SqlType<Object>) column.type()).write(statement, index, value);
    }

    @SuppressWarnings("unchecked")
    private static void writeArray(PreparedStatement statement, int index, Column<?> column, Collection<?> values) throws SQLException {
        ((SqlType<Object>) column.type()).writeArray(statement, index, values);
    }

    @FunctionalInterface
    interface Work<R> {
        R run(PreparedStatement statement) throws SQLException;
    }
}
