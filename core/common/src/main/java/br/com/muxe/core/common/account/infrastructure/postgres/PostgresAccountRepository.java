package br.com.muxe.core.common.account.infrastructure.postgres;

import static br.com.muxe.core.common.account.infrastructure.postgres.AccountTables.ACCOUNTS;
import static br.com.muxe.core.common.account.infrastructure.postgres.AccountTables.ACCOUNT_COLUMNS;
import static br.com.muxe.core.common.account.infrastructure.postgres.AccountTables.FIRST_SEEN_AT;
import static br.com.muxe.core.common.account.infrastructure.postgres.AccountTables.ID;
import static br.com.muxe.core.common.account.infrastructure.postgres.AccountTables.LAST_SEEN_AT;
import static br.com.muxe.core.common.account.infrastructure.postgres.AccountTables.PREMIUM;
import static br.com.muxe.core.common.account.infrastructure.postgres.AccountTables.USERNAME;

import br.com.muxe.core.common.account.domain.Account;
import br.com.muxe.core.common.account.domain.AccountRepository;
import br.com.muxe.core.common.persistence.sql.Select;
import br.com.muxe.core.common.persistence.sql.Sql;
import br.com.muxe.core.common.persistence.sql.Write;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public final class PostgresAccountRepository implements AccountRepository {

    private static final Select<Account> FIND_BY_ID = Sql.select(ACCOUNT_COLUMNS).from(ACCOUNTS)
        .where(ID.eq())
        .map(AccountTables::account)
        .named("account.find_by_id");

    private static final Select<Account> FIND_BY_IDS = Sql.select(ACCOUNT_COLUMNS).from(ACCOUNTS)
        .where(ID.in())
        .map(AccountTables::account)
        .named("account.find_by_ids");

    private static final Select<Account> FIND_BY_USERNAME = Sql.select(ACCOUNT_COLUMNS).from(ACCOUNTS)
        .where(USERNAME.eqIgnoreCase())
        .map(AccountTables::account)
        .named("account.find_by_username");

    private static final Write<Account> SAVE = Sql.<Account>insertInto(ACCOUNTS)
        .value(ID, Account::id)
        .value(USERNAME, Account::username)
        .value(PREMIUM, Account::premium)
        .value(FIRST_SEEN_AT, Account::firstSeenAt)
        .value(LAST_SEEN_AT, Account::lastSeenAt)
        .onConflict(ID).doUpdate(USERNAME, LAST_SEEN_AT)
        .named("account.save");

    @Override
    public Optional<Account> findById(UUID id) {
        return FIND_BY_ID.first(ID.is(id));
    }

    @Override
    public List<Account> findByIds(Collection<UUID> ids) {
        return ids.isEmpty() ? List.of() : FIND_BY_IDS.list(ID.in(ids));
    }

    @Override
    public Optional<Account> findByUsername(String username) {
        return FIND_BY_USERNAME.first(USERNAME.is(username));
    }

    @Override
    public void save(Account account) {
        SAVE.execute(account);
    }
}
