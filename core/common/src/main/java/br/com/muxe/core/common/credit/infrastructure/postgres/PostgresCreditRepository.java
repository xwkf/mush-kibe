package br.com.muxe.core.common.credit.infrastructure.postgres;

import br.com.muxe.core.common.credit.domain.CreditRepository;
import br.com.muxe.core.common.persistence.sql.Column;
import br.com.muxe.core.common.persistence.sql.Select;
import br.com.muxe.core.common.persistence.sql.Sql;
import br.com.muxe.core.common.persistence.sql.Table;
import br.com.muxe.core.common.persistence.sql.Write;
import br.com.muxe.core.common.persistence.sql.type.SqlType;
import java.time.Instant;
import java.util.Optional;
import java.util.OptionalLong;
import java.util.UUID;

public final class PostgresCreditRepository implements CreditRepository {

    private static final Table CREDITS = Table.of("core", "credits");
    private static final Column<UUID> PLAYER_ID = CREDITS.column("player_id", SqlType.UUID);
    private static final Column<Long> BALANCE = CREDITS.column("balance", SqlType.BIGINT);
    private static final Column<Instant> UPDATED_AT = CREDITS.column("updated_at", SqlType.TIMESTAMPTZ);

    private static final Table CHANGES = Table.of("core", "credit_changes");
    private static final Column<UUID> CHANGED_PLAYER = CHANGES.column("player_id", SqlType.UUID);
    private static final Column<Long> AMOUNT = CHANGES.column("amount", SqlType.BIGINT);
    private static final Column<String> REASON = CHANGES.column("reason", SqlType.TEXT);
    private static final Column<Instant> CHANGED_AT = CHANGES.column("changed_at", SqlType.TIMESTAMPTZ);

    private static final Column<Long> SPENT = Sql.parameter("spent", SqlType.BIGINT);

    private static final Select<Long> FIND_BALANCE = Sql.select(BALANCE).from(CREDITS)
        .where(PLAYER_ID.eq())
        .map(row -> row.get(BALANCE))
        .named("credits.balance");

    private static final Select<Long> ADD = Sql.query("""
            INSERT INTO core.credits (player_id, balance, updated_at) VALUES (?, ?, ?)
            ON CONFLICT (player_id) DO UPDATE SET balance = core.credits.balance + EXCLUDED.balance, updated_at = EXCLUDED.updated_at
            RETURNING balance
            """)
        .parameters(PLAYER_ID, BALANCE, UPDATED_AT)
        .columns(BALANCE)
        .map(row -> row.get(BALANCE))
        .named("credits.add");

    private static final Select<Long> SPEND = Sql.query("""
            UPDATE core.credits SET balance = balance - ?, updated_at = ?
            WHERE player_id = ? AND balance >= ?
            RETURNING balance
            """)
        .parameters(SPENT, UPDATED_AT, PLAYER_ID, SPENT)
        .columns(BALANCE)
        .map(row -> row.get(BALANCE))
        .named("credits.spend");

    private static final Write<Void> LOG = Sql.<Void>insertInto(CHANGES)
        .value(CHANGED_PLAYER)
        .value(AMOUNT)
        .value(REASON)
        .value(CHANGED_AT)
        .named("credits.log");

    @Override
    public long balance(UUID playerId) {
        return FIND_BALANCE.first(PLAYER_ID.is(playerId)).orElse(0L);
    }

    @Override
    public long add(UUID playerId, long amount, String reason, Instant at) {
        long balance = ADD.first(PLAYER_ID.is(playerId), BALANCE.is(amount), UPDATED_AT.is(at)).orElseThrow();
        log(playerId, amount, reason, at);
        return balance;
    }

    @Override
    public OptionalLong spend(UUID playerId, long amount, String reason, Instant at) {
        Optional<Long> balance = SPEND.first(SPENT.is(amount), UPDATED_AT.is(at), PLAYER_ID.is(playerId));
        if (balance.isEmpty()) {
            return OptionalLong.empty();
        }
        log(playerId, -amount, reason, at);
        return OptionalLong.of(balance.get());
    }

    private static void log(UUID playerId, long amount, String reason, Instant at) {
        LOG.execute(CHANGED_PLAYER.is(playerId), AMOUNT.is(amount), REASON.is(reason), CHANGED_AT.is(at));
    }
}
