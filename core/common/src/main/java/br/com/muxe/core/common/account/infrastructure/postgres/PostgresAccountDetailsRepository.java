package br.com.muxe.core.common.account.infrastructure.postgres;

import static br.com.muxe.core.common.account.infrastructure.postgres.AccountTables.ACCOUNTS;
import static br.com.muxe.core.common.account.infrastructure.postgres.AccountTables.ASN;
import static br.com.muxe.core.common.account.infrastructure.postgres.AccountTables.CITY;
import static br.com.muxe.core.common.account.infrastructure.postgres.AccountTables.COUNTRY;
import static br.com.muxe.core.common.account.infrastructure.postgres.AccountTables.FIRST_SEEN_AT;
import static br.com.muxe.core.common.account.infrastructure.postgres.AccountTables.ID;
import static br.com.muxe.core.common.account.infrastructure.postgres.AccountTables.LAST_SEEN_AT;
import static br.com.muxe.core.common.account.infrastructure.postgres.AccountTables.LOCATED;
import static br.com.muxe.core.common.account.infrastructure.postgres.AccountTables.LOCATIONS;
import static br.com.muxe.core.common.account.infrastructure.postgres.AccountTables.LOCATION_UPDATED_AT;
import static br.com.muxe.core.common.account.infrastructure.postgres.AccountTables.NUMBER;
import static br.com.muxe.core.common.account.infrastructure.postgres.AccountTables.PREMIUM;
import static br.com.muxe.core.common.account.infrastructure.postgres.AccountTables.REGION;
import static br.com.muxe.core.common.account.infrastructure.postgres.AccountTables.USERNAME;

import br.com.muxe.core.common.account.domain.AccountDetails;
import br.com.muxe.core.common.account.domain.AccountDetailsRepository;
import br.com.muxe.core.common.account.domain.AccountLocation;
import br.com.muxe.core.common.persistence.sql.Condition;
import br.com.muxe.core.common.persistence.sql.Row;
import br.com.muxe.core.common.persistence.sql.Select;
import br.com.muxe.core.common.persistence.sql.Sql;
import br.com.muxe.core.common.persistence.sql.Write;
import java.sql.SQLException;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public final class PostgresAccountDetailsRepository implements AccountDetailsRepository {

    private static final Select<AccountDetails> FIND_BY_ID = details(ID.eq(), "account.find_details_by_id");

    private static final Select<AccountDetails> FIND_BY_USERNAME = details(USERNAME.eqIgnoreCase(), "account.find_details_by_username");

    private static final Write<Void> SAVE_LOCATION = Sql.<Void>insertInto(LOCATIONS)
        .value(LOCATED)
        .value(COUNTRY)
        .value(REGION)
        .value(CITY)
        .value(ASN)
        .value(LOCATION_UPDATED_AT)
        .onConflict(LOCATED).doUpdate(COUNTRY, REGION, CITY, ASN, LOCATION_UPDATED_AT)
        .named("account.save_location");

    @Override
    public Optional<AccountDetails> findById(UUID id) {
        return FIND_BY_ID.first(ID.is(id));
    }

    @Override
    public Optional<AccountDetails> findByUsername(String username) {
        return FIND_BY_USERNAME.first(USERNAME.is(username));
    }

    @Override
    public void saveLocation(UUID id, AccountLocation location, Instant at) {
        SAVE_LOCATION.execute(
            LOCATED.is(id),
            COUNTRY.is(location.country()),
            REGION.is(location.region()),
            CITY.is(location.city()),
            ASN.is(location.asn()),
            LOCATION_UPDATED_AT.is(at)
        );
    }

    private static Select<AccountDetails> details(Condition condition, String name) {
        return Sql.select(ID, USERNAME, PREMIUM, FIRST_SEEN_AT, LAST_SEEN_AT, NUMBER, LOCATED, COUNTRY, REGION, CITY, ASN)
            .from(ACCOUNTS)
            .leftJoin(LOCATIONS, LOCATED.eq(ID))
            .where(condition)
            .map(PostgresAccountDetailsRepository::details)
            .named(name);
    }

    private static AccountDetails details(Row row) throws SQLException {
        Optional<AccountLocation> location = row.isNull(LOCATED)
            ? Optional.empty()
            : Optional.of(new AccountLocation(row.get(COUNTRY), row.get(REGION), row.get(CITY), row.get(ASN)));
        return new AccountDetails(AccountTables.account(row), row.get(NUMBER), location);
    }
}
