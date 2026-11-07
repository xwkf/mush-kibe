package br.com.muxe.core.common.persistence.sql.type;

import java.sql.Array;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;

public final class SqlType<T> {

    public static final SqlType<UUID> UUID = new SqlType<>("uuid", Types.OTHER,
        PreparedStatement::setObject,
        (rows, index) -> rows.getObject(index, UUID.class),
        "uuid", value -> value);

    public static final SqlType<String> TEXT = new SqlType<>("text", Types.VARCHAR,
        PreparedStatement::setString,
        ResultSet::getString,
        "text", value -> value);

    public static final SqlType<Boolean> BOOLEAN = new SqlType<>("boolean", Types.BOOLEAN,
        PreparedStatement::setBoolean,
        (rows, index) -> nullable(rows, rows.getBoolean(index)),
        "bool", value -> value);

    public static final SqlType<Integer> SMALLINT = new SqlType<>("smallint", Types.SMALLINT,
        (statement, index, value) -> statement.setShort(index, value.shortValue()),
        (rows, index) -> nullable(rows, rows.getInt(index)),
        "int2", Integer::shortValue);

    public static final SqlType<Integer> INTEGER = new SqlType<>("integer", Types.INTEGER,
        PreparedStatement::setInt,
        (rows, index) -> nullable(rows, rows.getInt(index)),
        "int4", value -> value);

    public static final SqlType<Long> BIGINT = new SqlType<>("bigint", Types.BIGINT,
        PreparedStatement::setLong,
        (rows, index) -> nullable(rows, rows.getLong(index)),
        "int8", value -> value);

    public static final SqlType<Double> DOUBLE = new SqlType<>("double precision", Types.DOUBLE,
        PreparedStatement::setDouble,
        (rows, index) -> nullable(rows, rows.getDouble(index)),
        "float8", value -> value);

    public static final SqlType<Float> REAL = new SqlType<>("real", Types.REAL,
        PreparedStatement::setFloat,
        (rows, index) -> nullable(rows, rows.getFloat(index)),
        "float4", value -> value);

    public static final SqlType<Instant> TIMESTAMPTZ = new SqlType<>("timestamptz", Types.TIMESTAMP_WITH_TIMEZONE,
        (statement, index, value) -> statement.setTimestamp(index, Timestamp.from(value)),
        (rows, index) -> {
            Timestamp timestamp = rows.getTimestamp(index);
            return timestamp == null ? null : timestamp.toInstant();
        },
        "timestamptz", Instant::toString);

    public static final SqlType<LocalDate> DATE = new SqlType<>("date", Types.DATE,
        PreparedStatement::setObject,
        (rows, index) -> rows.getObject(index, LocalDate.class),
        "date", LocalDate::toString);

    public static final SqlType<byte[]> BYTEA = new SqlType<>("bytea", Types.BINARY,
        PreparedStatement::setBytes,
        ResultSet::getBytes,
        null, null);

    public static final SqlType<List<String>> TEXT_ARRAY = new SqlType<>("text[]", Types.ARRAY,
        (statement, index, value) -> statement.setArray(index, statement.getConnection().createArrayOf("text", value.toArray())),
        (rows, index) -> {
            Array array = rows.getArray(index);
            return array == null ? null : Arrays.asList((String[]) array.getArray());
        },
        null, null);

    private final String name;
    private final int jdbcType;
    private final ValueWriter<T> writer;
    private final ValueReader<T> reader;
    private final String elementName;
    private final Function<? super T, ?> element;

    private SqlType(String name, int jdbcType, ValueWriter<T> writer, ValueReader<T> reader, String elementName, Function<? super T, ?> element) {
        this.name = Objects.requireNonNull(name, "name");
        this.jdbcType = jdbcType;
        this.writer = Objects.requireNonNull(writer, "writer");
        this.reader = Objects.requireNonNull(reader, "reader");
        this.elementName = elementName;
        this.element = element;
    }

    public static <T> SqlType<T> of(String name, int jdbcType, ValueWriter<T> writer, ValueReader<T> reader) {
        return new SqlType<>(name, jdbcType, writer, reader, null, null);
    }

    public static <E extends Enum<E>> SqlType<E> enumByName(Class<E> type) {
        return TEXT.map(value -> Enum.valueOf(type, value), Enum::name);
    }

    public <U> SqlType<U> map(Function<? super T, ? extends U> fromDatabase, Function<? super U, ? extends T> toDatabase) {
        return new SqlType<U>(name, jdbcType,
            (statement, index, value) -> write(statement, index, toDatabase.apply(value)),
            (rows, index) -> {
                T value = read(rows, index);
                return value == null ? null : fromDatabase.apply(value);
            },
            elementName,
            element == null ? null : value -> element.apply(toDatabase.apply(value)));
    }

    public SqlType<Optional<T>> optional() {
        return new SqlType<Optional<T>>(name, jdbcType,
            (statement, index, value) -> write(statement, index, value.orElse(null)),
            (rows, index) -> Optional.ofNullable(read(rows, index)),
            null, null);
    }

    public String name() {
        return name;
    }

    public void write(PreparedStatement statement, int index, T value) throws SQLException {
        if (value == null) {
            statement.setNull(index, jdbcType);
        } else {
            writer.write(statement, index, value);
        }
    }

    public T read(ResultSet rows, int index) throws SQLException {
        return reader.read(rows, index);
    }

    public void writeArray(PreparedStatement statement, int index, Collection<? extends T> values) throws SQLException {
        if (element == null) {
            throw new UnsupportedOperationException("Values of type " + name + " cannot be matched with ANY");
        }
        Object[] elements = values.stream().map(element).toArray();
        statement.setArray(index, statement.getConnection().createArrayOf(elementName, elements));
    }

    @Override
    public String toString() {
        return name;
    }

    private static <V> V nullable(ResultSet rows, V value) throws SQLException {
        return rows.wasNull() ? null : value;
    }
}
