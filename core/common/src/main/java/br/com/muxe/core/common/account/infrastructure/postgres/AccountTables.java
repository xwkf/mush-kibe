package br.com.muxe.core.common.account.infrastructure.postgres;

import br.com.muxe.core.common.account.domain.Account;
import br.com.muxe.core.common.persistence.sql.Column;
import br.com.muxe.core.common.persistence.sql.Row;
import br.com.muxe.core.common.persistence.sql.Table;
import br.com.muxe.core.common.persistence.sql.type.SqlType;
import java.sql.SQLException;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

final class AccountTables {

    static final Table ACCOUNTS = Table.of("core", "accounts");
    static final Column<UUID> ID = ACCOUNTS.column("id", SqlType.UUID);
    static final Column<String> USERNAME = ACCOUNTS.column("username", SqlType.TEXT);
    static final Column<Boolean> PREMIUM = ACCOUNTS.column("premium", SqlType.BOOLEAN);
    static final Column<Instant> FIRST_SEEN_AT = ACCOUNTS.column("first_seen_at", SqlType.TIMESTAMPTZ);
    static final Column<Instant> LAST_SEEN_AT = ACCOUNTS.column("last_seen_at", SqlType.TIMESTAMPTZ);
    static final Column<Long> NUMBER = ACCOUNTS.column("number", SqlType.BIGINT);

    static final Table LOCATIONS = Table.of("core", "account_locations");
    static final Column<UUID> LOCATED = LOCATIONS.column("player_id", SqlType.UUID);
    static final Column<Optional<String>> COUNTRY = LOCATIONS.column("country", SqlType.TEXT.optional());
    static final Column<Optional<String>> REGION = LOCATIONS.column("region", SqlType.TEXT.optional());
    static final Column<Optional<String>> CITY = LOCATIONS.column("city", SqlType.TEXT.optional());
    static final Column<Optional<String>> ASN = LOCATIONS.column("asn", SqlType.TEXT.optional());
    static final Column<Instant> LOCATION_UPDATED_AT = LOCATIONS.column("updated_at", SqlType.TIMESTAMPTZ);

    static final List<Column<?>> ACCOUNT_COLUMNS = List.of(ID, USERNAME, PREMIUM, FIRST_SEEN_AT, LAST_SEEN_AT);

    private AccountTables() {
    }

    static Account account(Row row) throws SQLException {
        return new Account(row.get(ID), row.get(USERNAME), row.get(PREMIUM), row.get(FIRST_SEEN_AT), row.get(LAST_SEEN_AT));
    }
}
