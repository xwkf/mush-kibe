package br.com.muxe.core.common.persistence.transaction;

import br.com.muxe.core.common.persistence.sql.observe.StatementObserver;
import java.sql.Connection;

public final class TransactionContext {

    private static final ScopedValue<Scope> SCOPE = ScopedValue.newInstance();

    private TransactionContext() {
    }

    public static Connection connection() {
        if (!SCOPE.isBound()) {
            throw new IllegalStateException("No active transaction. Run repository calls inside Transactions.inTransaction or readOnly.");
        }
        return SCOPE.get().connection();
    }

    public static StatementObserver observer() {
        return SCOPE.isBound() ? SCOPE.get().observer() : StatementObserver.NONE;
    }

    public static boolean isActive() {
        return SCOPE.isBound();
    }

    static <T> T bind(Connection connection, StatementObserver observer, TransactionalWork<T> work) throws Exception {
        return ScopedValue.where(SCOPE, new Scope(connection, observer)).call(work::execute);
    }

    private record Scope(Connection connection, StatementObserver observer) {
    }
}
