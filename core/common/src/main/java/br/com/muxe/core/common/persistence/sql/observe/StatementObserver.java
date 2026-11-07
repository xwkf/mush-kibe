package br.com.muxe.core.common.persistence.sql.observe;

@FunctionalInterface
public interface StatementObserver {

    StatementObserver NONE = (statement, nanos, success) -> {
    };

    void executed(String statement, long nanos, boolean success);
}
