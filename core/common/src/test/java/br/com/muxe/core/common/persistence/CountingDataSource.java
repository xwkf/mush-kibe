package br.com.muxe.core.common.persistence;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.concurrent.atomic.AtomicInteger;
import javax.sql.DataSource;

final class CountingDataSource {

    private final AtomicInteger roundTrips = new AtomicInteger();
    private final DataSource dataSource;

    CountingDataSource(DataSource target) {
        this.dataSource = proxy(DataSource.class, target, (result, method) -> method.equals("getConnection") ? connection((Connection) result) : result);
    }

    DataSource dataSource() {
        return dataSource;
    }

    int roundTrips() {
        return roundTrips.get();
    }

    void reset() {
        roundTrips.set(0);
    }

    private Object connection(Connection target) {
        return proxy(Connection.class, target, (result, method) -> result instanceof Statement statement ? statement(statement) : result);
    }

    private Object statement(Statement target) {
        Class<?> type = target instanceof CallableStatement ? CallableStatement.class
            : target instanceof PreparedStatement ? PreparedStatement.class
            : Statement.class;
        return proxy(type, target, (result, method) -> {
            if (method.startsWith("execute")) {
                roundTrips.incrementAndGet();
            }
            return result;
        });
    }

    @SuppressWarnings("unchecked")
    private static <T> T proxy(Class<?> type, Object target, Wrapper wrapper) {
        InvocationHandler handler = (ignored, method, arguments) -> {
            try {
                return wrapper.wrap(method.invoke(target, arguments), method.getName());
            } catch (InvocationTargetException exception) {
                throw exception.getCause();
            }
        };
        return (T) Proxy.newProxyInstance(CountingDataSource.class.getClassLoader(), new Class<?>[] {type}, handler);
    }

    @FunctionalInterface
    private interface Wrapper {
        Object wrap(Object result, String method) throws Exception;
    }
}
